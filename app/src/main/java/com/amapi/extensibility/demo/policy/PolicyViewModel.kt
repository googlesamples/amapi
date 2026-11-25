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
package com.amapi.extensibility.demo.policy

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.managementapi.policy.model.Policy
import java.lang.reflect.InvocationTargetException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for [PolicyActivity]. */
class PolicyViewModel(private val repository: PolicyRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  fun getPolicy() {
    launchPolicyCall(operation = "getPolicy") { repository.getPolicy() }
  }

  fun updatePolicy() {
    launchPolicyCall(operation = "updatePolicy") { repository.updatePolicy() }
  }

  /** Parses [policyJson] with [PolicyJsonParser] and replaces the enforced policy with it. */
  fun applyPolicyJson(policyJson: String) {
    launchPolicyCall(operation = "applyPolicyJson") {
      parsePolicyJson(policyJson)
        .fold(onSuccess = { repository.updatePolicy(it) }, onFailure = { Result.failure(it) })
    }
  }

  private fun parsePolicyJson(policyJson: String): Result<Policy> =
    try {
      Result.success(PolicyJsonParser.parsePolicyFromJson(policyJson))
    } catch (e: InvocationTargetException) {
      // The parser calls builder methods reflectively; surface the underlying cause.
      Result.failure(e.targetException ?: e)
    } catch (e: Exception) {
      Result.failure(e)
    }

  private fun launchPolicyCall(operation: String, call: suspend () -> Result<Policy>) {
    _uiState.value = UiState(statusMessage = "Running $operation...", state = UiState.State.PENDING)
    viewModelScope.launch {
      call()
        .onSuccess {
          _uiState.value =
            UiState(
              statusMessage = "Success: $operation returned policy version ${it.version}",
              state = UiState.State.SUCCESS,
            )
        }
        .onFailure {
          Log.w(TAG, "$operation failed", it)
          _uiState.value =
            UiState(statusMessage = "Error: ${it.message}", state = UiState.State.ERROR)
        }
    }
  }

  /** UI state rendered by [PolicyActivity]. */
  data class UiState(val statusMessage: String = "", val state: State = State.IDLE) {
    enum class State {
      IDLE,
      PENDING,
      SUCCESS,
      ERROR,
    }
  }

  /** Factory that creates a [PolicyViewModel] backed by [PolicyClientProvider]. */
  class Factory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      PolicyViewModel(PolicyRepository(PolicyClientProvider.getClient(application))) as T
  }

  companion object {
    private const val TAG = "PolicyViewModel"
  }
}
