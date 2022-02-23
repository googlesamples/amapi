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
import java.lang.StringBuilder

/** Contains command related utility methods. */
object CommandUtils {
  /**
   * Returns the properly formatted string representation of the given [Command] in a way that can
   * be logged and/or surfaced in UI.
   */
  fun parseCommandForPrettyPrint(command: Command): String {
    val stringBuilder =
      StringBuilder()
        .append("Id: ${command.commandId}\n")
        .append("Create time: ${command.createTime}\n")
        .append("Complete time: ${command.completeTime}\n")
        .append("State: ${command.state}\n")

    when (command.status.kind) {
      Command.StatusCase.Kind.CLEAR_APPS_DATA_STATUS ->
        for ((key, value) in command.status.clearAppsDataStatus().statusMap) {
          stringBuilder.append("\t").append(key).append(": ").append(value.clearStatus).append("\n")
        }
      else -> {}
    }
    return stringBuilder.toString()
  }
}
