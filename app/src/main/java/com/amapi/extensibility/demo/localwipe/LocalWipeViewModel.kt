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
package com.amapi.extensibility.demo.localwipe

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.commands.CommandUtils
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.commands.CommandException
import com.google.android.managementapi.commands.LocalCommandClientFactory
import com.google.android.managementapi.commands.model.GetCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest
import com.google.android.managementapi.commands.model.IssueCommandRequest.Wipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocalWipeViewModel(application: Application) : AndroidViewModel(application) {

  private val localCommandClient = LocalCommandClientFactory.create(application)

  private val _commandResult = MutableStateFlow("Issue Local Wipe Command result")
  val commandResult: StateFlow<String> = _commandResult.asStateFlow()

  fun issueWipeCommand(
    preserveResetProtection: Boolean,
    wipeExternalStorage: Boolean,
    wipeEsims: Boolean,
    wipeReason: String,
  ) {
    viewModelScope.launch {
      try {
        val command =
          localCommandClient.issueCommandAwait(
            buildIssueCommandRequest(
              preserveResetProtection,
              wipeExternalStorage,
              wipeEsims,
              wipeReason,
            )
          )
        val parsedCommandString = CommandUtils.parseCommandForPrettyPrint(command)
        Log.i(TAG, "Successfully issued wipe command: $parsedCommandString")
        _commandResult.value = parsedCommandString
      } catch (exception: CommandException) {
        Log.e(TAG, "onFailure", exception)
        val exceptionMessage = "Failed to execute wipe command\n${exception.message}"
        _commandResult.value = exceptionMessage
      }
    }
  }

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
        Log.i(TAG, "Successfully got command: $parsedCommandString")
        _commandResult.value = parsedCommandString
      } catch (exception: CommandException) {
        Log.e(TAG, "Failed getting command", exception)
        val exceptionMessage = "Failed to get command\n${exception.message}"
        _commandResult.value = exceptionMessage
      }
    }
  }

  private fun buildIssueCommandRequest(
    preserveResetProtection: Boolean,
    wipeExternalStorage: Boolean,
    wipeEsims: Boolean,
    wipeReason: String,
  ): IssueCommandRequest {
    val flags = mutableListOf<Wipe.WipeDataFlag>()
    if (preserveResetProtection) {
      flags.add(Wipe.WipeDataFlag.PRESERVE_RESET_PROTECTION_DATA)
    }
    if (wipeExternalStorage) {
      flags.add(Wipe.WipeDataFlag.WIPE_EXTERNAL_STORAGE)
    }
    if (wipeEsims) {
      flags.add(Wipe.WipeDataFlag.WIPE_ESIMS)
    }

    val wipeBuilder = Wipe.builder().setWipeDataFlags(flags)
    if (wipeReason.isNotEmpty()) {
      wipeBuilder.setWipeReason(wipeReason)
    }

    return IssueCommandRequest.builder().setWipe(wipeBuilder.build()).build()
  }
}
