/* Copyright 2020 Google LLC
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

/**
 * Utility methods for working with [Command] objects.
 */
object CommandUtils {
  /**
   * Generates a human-readable string representation of a [Command] for logging or UI display.
   *
   * The output includes the command's ID, creation time, completion time, and state.
   * If the command's status is `CLEAR_APPS_DATA_STATUS`, it will also include a detailed breakdown
   * of the status of each app, mapping app identifiers to their clear status.
   *
   * @param command The [Command] to parse and format.
   * @return A formatted string representation of the command.
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
