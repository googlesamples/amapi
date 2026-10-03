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
package com.amapi.extensibility.demo.dpcmigration

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.notification.NotificationReceiverService
import com.amapi.extensibility.demo.receiver.DeviceAdminReceiver
import com.google.android.managementapi.dpcmigration.DpcMigrationClient
import com.google.android.managementapi.dpcmigration.DpcMigrationClientFactory
import com.google.android.managementapi.dpcmigration.model.DpcMigrationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DpcMigrationViewModel(application: Application) : AndroidViewModel(application) {
  private val client: DpcMigrationClient = DpcMigrationClientFactory.create(getApplication())

  private val _dpcMigrationResult = MutableStateFlow<String?>(null)
  val dpcMigrationResult: StateFlow<String?> = _dpcMigrationResult.asStateFlow()

  fun startMigration(token: String) {
    val request = DpcMigrationRequest.builder().setMigrationToken(token).build()
    viewModelScope.launch {
      try {
        val result =
          client.migrateDeviceManagementToAndroidManagementApiAwait(
            getNotificationServiceComponentName(),
            getAdminComponent(),
            request,
          )
        _dpcMigrationResult.value = result.toString()
      } catch (e: Exception) {
        _dpcMigrationResult.value = e.message ?: "Unknown error occurred"
      }
    }
  }

  private fun getAdminComponent(): ComponentName {
    return ComponentName(getApplication(), DeviceAdminReceiver::class.java)
  }

  private fun getNotificationServiceComponentName(): ComponentName {
    return ComponentName(getApplication(), NotificationReceiverService::class.java)
  }
}
