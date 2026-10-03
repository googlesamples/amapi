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
package com.amapi.extensibility.demo.notification

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.amapi.extensibility.demo.approles.InMemoryAppRolesRepository
import com.amapi.extensibility.demo.commands.CommandUtils
import com.amapi.extensibility.demo.commands.InMemoryCommandRepository
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.approles.AppRolesListener
import com.google.android.managementapi.approles.model.AppRolesSetRequest
import com.google.android.managementapi.approles.model.AppRolesSetResponse
import com.google.android.managementapi.commands.CommandListener
import com.google.android.managementapi.commands.model.Command
import com.google.android.managementapi.notification.NotificationReceiverService

open class NotificationReceiverService : NotificationReceiverService() {

  override fun getCommandListener(): CommandListener {
    return object : CommandListener {
      override fun onCommandStatusChanged(command: Command) {
        Log.i(TAG, "onCommandStatusChanged: ${CommandUtils.parseCommandForPrettyPrint(command)}")
        InMemoryCommandRepository.onCommandStatusChanged(command)
      }
    }
  }

  override fun getAppRolesListener(): AppRolesListener {
    return object : AppRolesListener {
      override fun onAppRolesSet(request: AppRolesSetRequest): AppRolesSetResponse {
        Log.i(TAG, "App roles have changed. New roles payload: ${request.roles}")
        InMemoryAppRolesRepository.onAppRolesSet(request)
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(
              this@NotificationReceiverService,
              "App role has changed",
              Toast.LENGTH_LONG,
            )
            .show()
        }
        return AppRolesSetResponse.builder().build()
      }
    }
  }
}
