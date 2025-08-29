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

import com.google.android.managementapi.commands.model.Command

/** Contains command related utility methods. */
object CommandUtils {
  /**
   * Returns the properly formatted string representation of the given [Command] in a way that can
   * be logged and/or surfaced in UI.
   */
  fun parseCommandForPrettyPrint(command: Command): String {
    val message = buildString {
      appendLine("Id: ${command.commandId}")
      appendLine("Create time: ${command.createTime}")
      appendLine("Complete time: ${command.completeTime}")
      appendLine("State: ${command.state}")
      appendLine("Kind: ${command.status.kind}")

      when (command.status.kind) {
        Command.StatusCase.Kind.CLEAR_APPS_DATA_STATUS ->
          for ((key, value) in command.status.clearAppsDataStatus().statusMap) {
            append("\t")
            append(key)
            append(": ")
            append(value.clearStatus)
            appendLine()
          }
        Command.StatusCase.Kind.INSTALL_CUSTOM_APP_STATUS -> {
          val customAppOperationStatus = command.status.installCustomAppStatus()
          append("\t")
          appendLine("packageName: ${customAppOperationStatus.packageName}")
          append("\t")
          appendLine("operationStatus: ${customAppOperationStatus.operationStatus}")
          append("\t")
          appendLine("statusMessage: ${customAppOperationStatus.statusMessage}")
          append("\t")
          appendLine("otherPackageName: ${customAppOperationStatus.otherPackageName}")
          append("\t")
          appendLine("storagePath: ${customAppOperationStatus.storagePath}")
        }
        Command.StatusCase.Kind.UNINSTALL_CUSTOM_APP_STATUS -> {
          val customAppOperationStatus = command.status.uninstallCustomAppStatus()
          append("\t")
          appendLine("packageName: ${customAppOperationStatus.packageName}")
          append("\t")
          appendLine("operationStatus: ${customAppOperationStatus.operationStatus}")
          append("\t")
          appendLine("statusMessage: ${customAppOperationStatus.statusMessage}")
          append("\t")
          appendLine("otherPackageName: ${customAppOperationStatus.otherPackageName}")
          append("\t")
          appendLine("storagePath: ${customAppOperationStatus.storagePath}")
        }
        else -> {
          append("Status: ${command.status}")
        }
      }
    }
    return message
  }
}
