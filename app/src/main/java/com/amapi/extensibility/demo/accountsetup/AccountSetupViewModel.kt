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
package com.amapi.extensibility.demo.accountsetup

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.notification.NotificationReceiverService
import com.amapi.extensibility.demo.receiver.DeviceAdminReceiver
import com.amapi.extensibility.demo.util.TAG
import com.google.android.managementapi.accountsetup.AccountSetupClient
import com.google.android.managementapi.accountsetup.AccountSetupClientFactory
import com.google.android.managementapi.accountsetup.model.CancelAccountSetupAttemptRequest
import com.google.android.managementapi.accountsetup.model.LaunchAuthenticationActivityRequest
import com.google.android.managementapi.accountsetup.model.StartAccountSetupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A ViewModel used to handle account setup related operations. */
class AccountSetupViewModel(
  application: Application,
  activityResultRegistry: ActivityResultRegistry,
) : AndroidViewModel(application) {

  private val _accountSetupResult = MutableStateFlow<String?>(null)
  val accountSetupResult: StateFlow<String?> = _accountSetupResult.asStateFlow()

  private val accountSetupClient: AccountSetupClient =
    AccountSetupClientFactory.create(getApplication(), activityResultRegistry)

  fun startAccountSetup(enrollmentToken: String) {
    val request =
      StartAccountSetupRequest.builder()
        .setEnrollmentToken(enrollmentToken)
        .setAdminComponentName(ComponentName(getApplication(), DeviceAdminReceiver::class.java))
        .setNotificationReceiverServiceComponentName(
          ComponentName(getApplication(), NotificationReceiverService::class.java)
        )
        .build()

    viewModelScope.launch {
      try {
        val result = accountSetupClient.startAccountSetup(request)
        Log.i(TAG, "Successfully started account setup: $result")
        _accountSetupResult.value = result.toString()
      } catch (t: Throwable) {
        Log.e(TAG, "Failed to start account setup", t)
        _accountSetupResult.value = t.message
      }
    }
  }

  fun launchAuthenticationActivity() {
    val request = LaunchAuthenticationActivityRequest.getDefaultInstance()

    viewModelScope.launch {
      try {
        val result = accountSetupClient.launchAuthenticationActivity(request)
        Log.i(TAG, "Successfully launched auth activity: $result")
        _accountSetupResult.value = result.toString()
      } catch (t: Throwable) {
        Log.e(TAG, "Failed to launch auth activity", t)
        _accountSetupResult.value = t.message
      }
    }
  }

  fun listAccountSetupAttempts() {
    viewModelScope.launch {
      try {
        val result = accountSetupClient.listAccountSetupAttempts()
        Log.i(TAG, "Successfully listed account setup attempts: $result")
        _accountSetupResult.value = result.toString()
      } catch (t: Throwable) {
        Log.e(TAG, "Failed to list account setup attempts", t)
        _accountSetupResult.value = t.message
      }
    }
  }

  fun cancelAccountSetupAttempt() {
    val request = CancelAccountSetupAttemptRequest.getDefaultInstance()
    viewModelScope.launch {
      try {
        val result = accountSetupClient.cancelAccountSetupAttempt(request)
        Log.i(TAG, "Successfully canceled account setup attempt: $result")
        _accountSetupResult.value = result.toString()
      } catch (t: Throwable) {
        Log.e(TAG, "Failed to cancel account setup attempt", t)
        _accountSetupResult.value = t.message
      }
    }
  }

  /** Factory for creating AccountSetupViewModel instances. */
  class AccountSetupViewModelFactory(
    private val application: Application,
    private val activityResultRegistry: ActivityResultRegistry,
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      AccountSetupViewModel(application, activityResultRegistry) as? T
        ?: throw IllegalArgumentException("Unknown ViewModel class")
  }
}
