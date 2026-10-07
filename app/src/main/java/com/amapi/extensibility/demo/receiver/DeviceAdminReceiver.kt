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
package com.amapi.extensibility.demo.receiver

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.util.Log

/** Receiver that listens to device admin changes (e.g. PO or DO changed). */
class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED -> Log.d("DeviceAdminReceiver", "PO changed")
      DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED -> Log.d("DeviceAdminReceiver", "DO changed")
      else -> super.onReceive(context, intent)
    }
  }
}
