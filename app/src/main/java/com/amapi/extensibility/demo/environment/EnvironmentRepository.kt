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
package com.amapi.extensibility.demo.environment

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.amapi.extensibility.demo.receiver.DeviceAdminReceiver
import com.google.android.managementapi.common.model.Role
import com.google.android.managementapi.environment.EnvironmentClient
import com.google.android.managementapi.environment.EnvironmentClientFactory
import com.google.android.managementapi.environment.model.GetEnvironmentRequest
import com.google.android.managementapi.environment.model.PrepareEnvironmentRequest

class EnvironmentRepository(context: Context) {

  private val appContext = context.applicationContext
  private val environmentClient: EnvironmentClient = EnvironmentClientFactory.create(appContext)

  suspend fun getEnvironment(roleTypes: Collection<Role.RoleType>): String {
    try {
      val environment =
        environmentClient.getEnvironment(
          GetEnvironmentRequest.builder()
            .setRoles(roleTypes.map { Role.builder().setRoleType(it).build() })
            .build()
        )
      return environment.toString()
    } catch (e: Exception) {
      Log.e(TAG, "Error getting environment", e)
      return e.toString()
    }
  }

  suspend fun prepareEnvironment(roleTypes: Collection<Role.RoleType>): String {
    try {
      val request =
        PrepareEnvironmentRequest.builder()
          .apply {
            setRoles(roleTypes.map { Role.builder().setRoleType(it).build() })
            if (Role.RoleType.DEVICE_POLICY_CONTROLLER in roleTypes) {
              setAdmin(ComponentName(appContext, DeviceAdminReceiver::class.java))
            }
          }
          .build()

      val prepareEnvironmentResponse =
        environmentClient.prepareEnvironment(
          request = request,
          notificationServiceComponentName = null,
        )
      return prepareEnvironmentResponse.toString()
    } catch (e: Exception) {
      Log.e(TAG, "Error preparing environment", e)
      return e.toString()
    }
  }

  companion object {
    private const val TAG = "EnvironmentRepository"
  }
}
