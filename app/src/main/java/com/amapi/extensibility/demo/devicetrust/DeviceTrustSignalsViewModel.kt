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
package com.amapi.extensibility.demo.devicetrust

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.devicetrust.DeviceTrustRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceTrustSignalsViewModel(application: Application) : AndroidViewModel(application) {
  private val _deviceTrustResult = MutableStateFlow("")
  val deviceTrustResult: StateFlow<String> = _deviceTrustResult.asStateFlow()
  private val devicePostureRepository = DeviceTrustRepository(application)

  fun getDeviceTrust() {
    viewModelScope.launch { _deviceTrustResult.value = devicePostureRepository.getDeviceTrust() }
  }
}
