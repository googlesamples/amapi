/* Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amapi.extensibility.demo.requestdeviceinfo

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.commands.CommandUtils
import com.amapi.extensibility.demo.commands.InMemoryCommandRepository
import com.amapi.extensibility.demo.util.AppIdlingResource
import com.google.android.managementapi.commands.LocalCommandClientFactory
import com.google.android.managementapi.commands.model.GetCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class RequestDeviceInfoViewModel(private val application: Application) : ViewModel() {

  private val _uiState = MutableStateFlow<CommandUiState>(CommandUiState.Idle)
  val uiState: StateFlow<CommandUiState> = _uiState

  val commandResult: StateFlow<String>
    get() = InMemoryCommandRepository.commandResult

  init {
    InMemoryCommandRepository.setValue("")
  }

  /**
   * Issues a local command request to request device info.
   *
   * [commandResult] will be updated asynchronously of the response and in case of any error.
   */
  fun issueRequestDeviceInfoCommand(deviceInfo: String) {

    if (deviceInfo.isEmpty() || !deviceInfo.equals(EID, ignoreCase = true)) {
      _uiState.value = CommandUiState.InvalidDeviceInfo("Specify a valid device info (e.g: EID)")
      return
    }

    // Increment Idling Resource before starting async work
    AppIdlingResource.increment()

    viewModelScope.launch {
      try {
        val issueCommandRequest =
          IssueCommandRequest.builder()
            .setRequestDeviceInfo(
              IssueCommandRequest.RequestDeviceInfo.builder()
                .setDeviceInfo(IssueCommandRequest.RequestDeviceInfo.DeviceInfo.EID)
            )
            .build()
        val command =
          LocalCommandClientFactory.create(application.applicationContext)
            .issueCommand(issueCommandRequest)

        command.await()
        _uiState.value = CommandUiState.Success("Command successful: $command")
        // Command result is populated by NotificationReceiverService
      } catch (exception: Exception) {
        Log.e(TAG, "onFailure", exception)
        _uiState.value = CommandUiState.Error("Failed to execute command: ${exception.message}")
      } finally {
        // Decrement Idling Resource when async work is done
        AppIdlingResource.decrement()
      }
    }
  }

  /**
   * Issues a request to get the current status of previously issued local command with [commandId].
   *
   * [commandResult] will be updated asynchronously with the command status, or if the request
   * failed.
   *
   * @param commandId
   * - ID of the previously issued command
   */
  fun getCommand(commandId: String) {
    if (commandId.isEmpty()) {
      _uiState.value = CommandUiState.Error("Command ID not specified")
      return
    }
    viewModelScope.launch {
      try {
        val command =
          LocalCommandClientFactory.create(application.applicationContext)
            .getCommand(GetCommandRequest.builder().setCommandId(commandId).build())
            .await()
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        _uiState.value = CommandUiState.Success("Get command result: \n$parsedCommandString")
      } catch (exception: Exception) {
        Log.e(TAG, "Failed issuing command", exception)
        _uiState.value = CommandUiState.Error("Failed to get command: ${exception.message}")
      }
    }
  }

  private companion object {
    const val TAG = "RequestDeviceInfoVM"
    const val EID = "EID"
  }

  /** Factory for creating RequestDeviceInfoViewModel instances. */
  class RequestDeviceInfoViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(RequestDeviceInfoViewModel::class.java)) {
        return RequestDeviceInfoViewModel(application) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }

  sealed class CommandUiState {
    object Idle : CommandUiState()

    data class Error(val message: String) : CommandUiState()

    data class Success(val message: String) : CommandUiState()

    data class InvalidDeviceInfo(val message: String) : CommandUiState()
  }
}
