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
package com.amapi.extensibility.demo.notification

import android.util.Log
import com.amapi.extensibility.demo.commands.CommandUtils
import com.amapi.extensibility.demo.commands.InMemoryCommandRepository
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.commands.CommandListener
import com.google.android.managementapi.commands.model.Command
import com.google.android.managementapi.notification.NotificationReceiverService

class NotificationReceiverService : NotificationReceiverService() {

  override fun getCommandListener(): CommandListener {
    return object : CommandListener {
      override fun onCommandStatusChanged(command: Command) {
        Log.i(TAG, "onCommandStatusChanged: ${CommandUtils.parseCommandForPrettyPrint(command)}")
        InMemoryCommandRepository.onCommandStatusChanged(command)
      }
    }
  }
}
