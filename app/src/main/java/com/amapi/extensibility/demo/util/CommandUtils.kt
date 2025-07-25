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
package com.amapi.extensibility.demo.util

import com.google.android.managementapi.commands.model.Command
import kotlin.collections.iterator

/** Contains command related utility methods. */
object CommandUtils {
  /**
   * Returns the properly formatted string representation of the given [Command] in a way that can
   * be logged and/or surfaced in UI.
   */
  fun Command.parseForPrettyPrint(): String {
    val message = buildString {
      appendLine("Id: ${this@parseForPrettyPrint.commandId}")
      appendLine("Create time: ${this@parseForPrettyPrint.createTime}")
      appendLine("Complete time: ${this@parseForPrettyPrint.completeTime}")
      appendLine("State: ${this@parseForPrettyPrint.state}")
      appendLine("Kind: ${this@parseForPrettyPrint.status.kind}")

      when (this@parseForPrettyPrint.status.kind) {
        Command.StatusCase.Kind.CLEAR_APPS_DATA_STATUS ->
          for ((key, value) in this@parseForPrettyPrint.status.clearAppsDataStatus().statusMap) {
            append("\t")
            append(key)
            append(": ")
            append(value.clearStatus)
            appendLine()
          }
        Command.StatusCase.Kind.INSTALL_CUSTOM_APP_STATUS -> {
          val customAppOperationStatus = this@parseForPrettyPrint.status.installCustomAppStatus()
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
          val customAppOperationStatus = this@parseForPrettyPrint.status.uninstallCustomAppStatus()
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
          append("Status: ${this@parseForPrettyPrint.status}")
        }
      }
    }
    return message
  }
}
