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
package com.amapi.extensibility.demo.commands

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.commands.LocalCommandClientFactory
import com.google.android.managementapi.commands.model.GetCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest.ClearAppsData
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommandViewModel(application: Application) : AndroidViewModel(application) {

  private val localCommandClient = LocalCommandClientFactory.create(application)

  private val _commandResult = MutableStateFlow("Issue Command result")
  val commandResult: StateFlow<String> = _commandResult.asStateFlow()

  init {
    viewModelScope.launch {
      // Init the command result with the value from the repository, and keep it updated.
      InMemoryCommandRepository.commandResult.collect { commandStr ->
        if (commandStr.isNotEmpty()) {
          _commandResult.value = commandStr
        }
      }
    }
  }

  /**
   * Issues a local command request to clear app data for [packageName].
   *
   * [commandResult] will be updated asynchronously of the response and in case of any error.
   */
  fun issueClearAppDataCommand(packageName: String) {
    if (packageName.isEmpty()) {
      _commandResult.value = "Specify a package name"
      return
    }

    viewModelScope.launch {
      try {
        val command =
          localCommandClient.issueCommandAwait(
            IssueCommandRequest.builder()
              .setClearAppsData(
                ClearAppsData.builder().setPackageNames(ImmutableList.of(packageName))
              )
              .build()
          )
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        Log.i(TAG, "Successfully issued command: $parsedCommandString")
        _commandResult.value = parsedCommandString
      } catch (exception: Exception) {
        Log.e(TAG, "onFailure", exception)
        val exceptionMessage = buildString {
          append("Failed to execute command\n")
          append(exception.message)
        }
        _commandResult.value = exceptionMessage
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
      _commandResult.value = "Command ID not specified"
      return
    }
    viewModelScope.launch {
      try {
        val command =
          localCommandClient.getCommandAwait(
            GetCommandRequest.builder().setCommandId(commandId).build()
          )
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        Log.i(TAG, "Successfully issued command: $parsedCommandString")
        _commandResult.value = parsedCommandString
      } catch (exception: Exception) {
        Log.e(TAG, "Failed issuing command", exception)
        val exceptionMessage = buildString {
          append("Failed to get command\n")
          append(exception.message)
        }
        _commandResult.value = exceptionMessage
      }
    }
  }
}
