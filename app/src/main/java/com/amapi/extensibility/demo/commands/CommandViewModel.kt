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
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.commands.LocalCommandClientFactory
import com.google.android.managementapi.commands.model.GetCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest.ClearAppsData
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class CommandViewModel(application: Application) : AndroidViewModel(application) {
  private val defaultScope = CoroutineScope(Dispatchers.Default)
  private val commandResult: MediatorLiveData<String> =
    MediatorLiveData<String>().apply {
      addSource(InMemoryCommandRepository.getCommandLiveData()) { command ->
        postValue(CommandUtils.parseCommandForPrettyPrint(command))
      }
    }

  val commandResultLiveData: LiveData<String>
    get() = commandResult

  /**
   * Issues a local command request to clear app data for [packageName].
   *
   * [commandResult] will be updated asynchronously of the response and in case of any error.
   */
  fun issueClearAppDataCommand(packageName: String) {
    if (TextUtils.isEmpty(packageName)) {
      commandResult.postValue("Specify a package name")
      return
    }

    defaultScope.launch {
      try {
        val command =
          LocalCommandClientFactory.create(getApplication())
            .issueCommand(
              IssueCommandRequest.builder()
                .setClearAppsData(
                  ClearAppsData.builder().setPackageNames(ImmutableList.of(packageName))
                )
                .build()
            )
            .await()
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        Log.i(TAG, "Successfully issued command: $parsedCommandString")
        commandResult.postValue(parsedCommandString)
      } catch (exception: Exception) {
        Log.e(TAG, "onFailure", exception)
        val stringBuilder =
          StringBuilder().apply {
            append("Failed to execute command\n")
            append(exception.message)
          }
        commandResult.postValue(stringBuilder.toString())
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
    if (TextUtils.isEmpty(commandId)) {
      commandResult.postValue("Command ID not specified")
      return
    }
    defaultScope.launch {
      try {
        val command =
          LocalCommandClientFactory.create(getApplication())
            .getCommand(GetCommandRequest.builder().setCommandId(commandId).build())
            .await()
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        Log.i(TAG, "Successfully issued command: $parsedCommandString")
        commandResult.postValue(parsedCommandString)
      } catch (exception: Exception) {
        Log.e(TAG, "Failed issuing command", exception)
        val stringBuilder =
          StringBuilder().apply {
            append("Failed to get command\n")
            append(exception.message)
          }
        commandResult.postValue(stringBuilder.toString())
      }
    }
  }
}
