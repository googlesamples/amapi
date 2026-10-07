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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.managementapi.common.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EnvironmentViewModel(application: Application) : AndroidViewModel(application) {
  private val _environmentResult = MutableStateFlow("")
  val environmentResult: StateFlow<String> = _environmentResult.asStateFlow()
  private val environmentRepository = EnvironmentRepository(application)

  /**
   * Calls the AM API SDK's `getEnvironment` for the specified roles.
   *
   * @param roleTypes The collection of roles to query the environment for.
   */
  fun getEnvironment(roleTypes: Collection<Role.RoleType>) {
    viewModelScope.launch {
      _environmentResult.value = environmentRepository.getEnvironment(roleTypes)
    }
  }

  /**
   * Calls the AM API SDK's `prepareEnvironment` for the specified roles.
   *
   * @param roleTypes The collection of roles to prepare the environment for.
   */
  fun prepareEnvironment(roleTypes: Collection<Role.RoleType>) {
    viewModelScope.launch {
      _environmentResult.value = environmentRepository.prepareEnvironment(roleTypes)
    }
  }
}
