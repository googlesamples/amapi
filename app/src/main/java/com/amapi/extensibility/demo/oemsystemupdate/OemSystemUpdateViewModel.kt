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
package com.amapi.extensibility.demo.oemsystemupdate

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.managementapi.oemsystemupdate.model.UpdateControlState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for [OemSystemUpdateActivity]. */
class OemSystemUpdateViewModel(private val repository: OemSystemUpdateRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  fun createSystemUpdate(name: String, isApiLevelChange: Boolean) {
    _uiState.value =
      _uiState.value.copy(
        statusMessage = "Creating system update...",
        state = UiState.State.PENDING,
      )
    Log.d(TAG, "createSystemUpdate started")
    viewModelScope.launch {
      repository
        .createSystemUpdate(name, isApiLevelChange)
        .onSuccess {
          Log.d(TAG, "createSystemUpdate returned: $it")
          val message = "Success: Created system update ${it.name}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.SUCCESS,
            )
        }
        .onFailure {
          val message = "Error: ${it.message}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.ERROR,
            )
          Log.d(TAG, "createSystemUpdate failed: ${it.message}")
        }
    }
  }

  /** Gets a system update by name. */
  fun getSystemUpdate(name: String) {
    _uiState.value =
      _uiState.value.copy(statusMessage = "Getting system update...", state = UiState.State.PENDING)
    Log.d(TAG, "getSystemUpdate started")
    viewModelScope.launch {
      repository
        .getSystemUpdate(name)
        .onSuccess {
          Log.d(TAG, "getSystemUpdate returned: $it")
          val message = "Success: Received update $it"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.SUCCESS,
            )
        }
        .onFailure {
          val message = "Error: ${it.message}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.ERROR,
            )
          Log.d(TAG, "getSystemUpdate failed: ${it.message}")
        }
    }
  }

  /** Lists all system updates. */
  fun listSystemUpdates() {
    _uiState.value =
      _uiState.value.copy(
        statusMessage = "Listing system updates...",
        state = UiState.State.PENDING,
      )
    Log.d(TAG, "listSystemUpdates started")
    viewModelScope.launch {
      repository
        .listSystemUpdates()
        .onSuccess { updates ->
          val message =
            "Success: Received ${updates.size} updates.\n" +
              updates.joinToString("\n") { it.toString() }
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.SUCCESS,
            )
          Log.d(TAG, "listSystemUpdates returned: $updates")
        }
        .onFailure {
          val message = "Error: ${it.message}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.ERROR,
            )
          Log.d(TAG, "listSystemUpdates failed: ${it.message}")
        }
    }
  }

  fun reportSystemUpdateDownloading(name: String) {
    reportSystemUpdate("downloading", name) { repository.reportSystemUpdateDownloading(it) }
  }

  fun reportSystemUpdateDownloaded(name: String) {
    reportSystemUpdate("downloaded", name) { repository.reportSystemUpdateDownloaded(it) }
  }

  fun reportSystemUpdateApplied(name: String) {
    reportSystemUpdate("applied", name) { repository.reportSystemUpdateApplied(it) }
  }

  fun reportSystemUpdateAborted(name: String) {
    reportSystemUpdate("aborted", name) { repository.reportSystemUpdateAborted(it) }
  }

  fun reportSystemUpdateDownloadingWithOverride(name: String) {
    reportSystemUpdate("downloading with override", name) {
      repository.reportSystemUpdateDownloadingWithOverride(it)
    }
  }

  fun reportSystemUpdateDownloadedWithOverride(name: String) {
    reportSystemUpdate("downloaded with override", name) {
      repository.reportSystemUpdateDownloadedWithOverride(it)
    }
  }

  fun reportSystemUpdateAppliedWithOverride(name: String) {
    reportSystemUpdate("applied with override", name) {
      repository.reportSystemUpdateAppliedWithOverride(it)
    }
  }

  fun reportSystemUpdateDownloadPrevented(name: String) {
    reportSystemUpdate("download prevented", name) {
      repository.reportSystemUpdateDownloadPrevented(it)
    }
  }

  fun reportSystemUpdateApplyPrevented(name: String) {
    reportSystemUpdate("apply prevented", name) { repository.reportSystemUpdateApplyPrevented(it) }
  }

  fun updateUpdateControlState(controlState: UpdateControlState.ControlState) {
    _uiState.value =
      _uiState.value.copy(
        statusMessage = "Updating control state to $controlState...",
        state = UiState.State.PENDING,
      )
    Log.d(TAG, "updateUpdateControlState started")
    viewModelScope.launch {
      repository
        .updateUpdateControlState(controlState)
        .onSuccess {
          Log.d(TAG, "updateUpdateControlState returned: $it")
          val message = "Success: Updated control state to ${it.controlState}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              currentControlState = it.controlState,
              state = UiState.State.SUCCESS,
            )
        }
        .onFailure {
          val message = "Error: ${it.message}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.ERROR,
            )
          Log.d(TAG, "updateUpdateControlState failed: ${it.message}")
        }
    }
  }

  private fun reportSystemUpdate(
    status: String,
    name: String,
    reportCall: suspend (String) -> Result<Unit>,
  ) {
    _uiState.value =
      _uiState.value.copy(statusMessage = "Reporting $status...", state = UiState.State.PENDING)
    viewModelScope.launch {
      reportCall(name)
        .onSuccess {
          val message = "Success: Reported $status for $name"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.SUCCESS,
            )
        }
        .onFailure {
          val message = "Error: Failed to report $status for $name. ${it.message}"
          _uiState.value =
            _uiState.value.copy(
              statusMessage = message,
              systemUpdateLog = _uiState.value.systemUpdateLog + message,
              state = UiState.State.ERROR,
            )
        }
    }
  }

  data class UiState(
    val statusMessage: String = "",
    val systemUpdateLog: List<String> = emptyList(),
    val currentControlState: UpdateControlState.ControlState =
      UpdateControlState.ControlState.UNIFIED_UPDATE_CONTROL,
    val state: State = State.PENDING,
  ) {
    enum class State {
      PENDING,
      SUCCESS,
      ERROR,
    }

    val controlStateOptions =
      listOf(
        "UUC" to UpdateControlState.ControlState.UNIFIED_UPDATE_CONTROL,
        "SUP" to UpdateControlState.ControlState.SYSTEM_UPDATE_POLICY_CONTROL,
        "Custom OEM" to UpdateControlState.ControlState.OEM_CUSTOM_CONTROL,
      )

    val currentControlStateLabel: String
      get() =
        when (currentControlState) {
          UpdateControlState.ControlState.UNIFIED_UPDATE_CONTROL -> "UUC"
          UpdateControlState.ControlState.SYSTEM_UPDATE_POLICY_CONTROL -> "SUP"
          UpdateControlState.ControlState.OEM_CUSTOM_CONTROL -> "Custom OEM"
          else -> "UUC"
        }
  }

  companion object {
    private const val TAG = "OemSystemUpdateVM"
  }

  /** Factory for [OemSystemUpdateViewModel]. */
  class OemSystemUpdateViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      val repository = InMemoryOemSystemUpdateRepository(application.applicationContext)
      return OemSystemUpdateViewModel(repository) as T
    }
  }
}
