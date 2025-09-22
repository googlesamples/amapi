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
package com.amapi.extensibility.demo.customapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amapi.extensibility.demo.util.AppIdlingResource
import com.amapi.extensibility.demo.commands.InMemoryCommandRepository
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.ErrorType
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.ErrorType.COMMAND_EXECUTION_FAILED
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.ErrorType.DOWNLOAD_FAILED
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.ErrorType.EMPTY_PACKAGE_NAME
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage
import com.amapi.extensibility.demo.data.CustomAppRepository
import com.amapi.extensibility.demo.data.CustomAppRepositoryImpl
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class CustomAppViewModel(private val customAppRepository: CustomAppRepository) : ViewModel() {
  private val _uiState =
    MutableStateFlow(
      UiState(Stage.START, filePaths = customAppRepository.getDownloadedFilesPaths())
    )

  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  init {
    InMemoryCommandRepository.setValue("")
    InMemoryCommandRepository.commandResult
      .onEach { commandStatus ->
        viewModelScope.launch { _uiState.emit(_uiState.value.copy(commandStatus = commandStatus)) }
      }
      .launchIn(viewModelScope)
  }

  /**
   * Initiates the download of an app with the [packageName] from the provided [downloadUrl] and
   * stores it in internal storage.
   */
  fun downloadApk(packageName: String, downloadUrl: String) {
    viewModelScope.launch {
      if (downloadUrl.isEmpty() || packageName.isEmpty()) {
        _uiState.emit(
          UiState(
            uiStage = Stage.FAILURE,
            errorType = ErrorType.EMPTY_DOWNLOAD_URL_OR_PACKAGE_NAME,
            filePaths = customAppRepository.getDownloadedFilesPaths(),
          )
        )
        return@launch
      }
      _uiState.emit(
        UiState(Stage.DOWNLOADING_FILE, filePaths = customAppRepository.getDownloadedFilesPaths())
      )

      customAppRepository
        .downloadAppToInternalStorage(packageName, downloadUrl)
        .onSuccess {
          Log.i(TAG, "Successfully downloaded file: $downloadUrl")
          _uiState.emit(
            UiState(
              Stage.FILE_DOWNLOADED,
              filePaths = customAppRepository.getDownloadedFilesPaths(),
            )
          )
        }
        .onFailure { exception ->
          Log.e(TAG, "Failed to download file: $downloadUrl. ${exception.localizedMessage}")
          _uiState.emit(
            UiState(
              Stage.FAILURE,
              errorType = DOWNLOAD_FAILED,
              errorMessage = exception.localizedMessage,
              filePaths = customAppRepository.getDownloadedFilesPaths(),
            )
          )
        }
    }
  }

  /**
   * Sends install app command to CloudDPC to initiate installation of the app with the
   * [packageName].
   */
  fun installApp(packageName: String, handleFileProvider: Boolean) {
    AppIdlingResource.increment()
    viewModelScope.launch {
      if (packageName.isEmpty()) {
        Log.e(TAG, "Package name is empty")
        _uiState.emit(
          UiState(
            Stage.FAILURE,
            errorType = EMPTY_PACKAGE_NAME,
            filePaths = customAppRepository.getDownloadedFilesPaths(),
          )
        )
        AppIdlingResource.decrement()
      } else {
        if (handleFileProvider) {
          customAppRepository.grantFilePermissionToDeviceManager(packageName)
        }
        customAppRepository
          .sendInstallAppCommandToDeviceManager(packageName, handleFileProvider)
          .onSuccess {
            _uiState.emit(
              UiState(Stage.COMMAND_SENT, filePaths = customAppRepository.getDownloadedFilesPaths())
            )
            AppIdlingResource.decrement()
          }
          .onFailure { exception ->
            Log.e(TAG, "onFailure: ")
            _uiState.emit(
              UiState(
                Stage.FAILURE,
                errorType = COMMAND_EXECUTION_FAILED,
                errorMessage = exception.localizedMessage,
                filePaths = customAppRepository.getDownloadedFilesPaths(),
              )
            )
            AppIdlingResource.decrement()
          }
      }
    }
  }

  fun uninstallApp(packageName: String) {
    AppIdlingResource.increment()
    viewModelScope.launch {
      if (packageName.isEmpty()) {
        Log.e(TAG, "Package name is empty")
        _uiState.emit(
          UiState(
            Stage.FAILURE,
            errorType = EMPTY_PACKAGE_NAME,
            filePaths = customAppRepository.getDownloadedFilesPaths(),
          )
        )
        AppIdlingResource.decrement()
      } else {
        customAppRepository
          .sendUninstallAppCommandToDeviceManager(packageName)
          .onSuccess {
            _uiState.emit(
              UiState(Stage.COMMAND_SENT, filePaths = customAppRepository.getDownloadedFilesPaths())
            )
            AppIdlingResource.decrement()
          }
          .onFailure { exception ->
            _uiState.emit(
              UiState(
                Stage.FAILURE,
                errorType = COMMAND_EXECUTION_FAILED,
                errorMessage = exception.localizedMessage,
                filePaths = customAppRepository.getDownloadedFilesPaths(),
              )
            )
            AppIdlingResource.decrement()
          }
      }
    }
  }

  /** Deletes all downloaded apps from the internal storage. */
  fun resetDownloadedApps() {
    customAppRepository.resetDownloadedFiles()
    viewModelScope.launch {
      _uiState.emit(
        UiState(_uiState.value.uiStage, filePaths = customAppRepository.getDownloadedFilesPaths())
      )
    }
  }

  private companion object {
    const val TAG = "CustomInstallerVM"
  }

  data class UiState(
    val uiStage: Stage,
    val errorType: ErrorType? = null,
    val errorMessage: String? = null,
    val filePaths: List<String> = listOf(),
    val commandStatus: String? = "",
  ) {
    enum class Stage {
      START,
      DOWNLOADING_FILE,
      FILE_DOWNLOADED,
      COMMAND_SENT,
      FAILURE,
    }

    enum class ErrorType {
      EMPTY_PACKAGE_NAME,
      EMPTY_DOWNLOAD_URL_OR_PACKAGE_NAME,
      DOWNLOAD_FAILED,
      COMMAND_EXECUTION_FAILED,
    }
  }

  class CustomAppViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      val repository: CustomAppRepository =
        CustomAppRepositoryImpl(
          application.applicationContext,
          Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
        )
      return CustomAppViewModel(repository) as? T
        ?: throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
