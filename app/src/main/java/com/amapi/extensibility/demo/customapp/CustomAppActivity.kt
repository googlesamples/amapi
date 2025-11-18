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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amapi.extensibility.demo.R
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.ErrorType
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage.COMMAND_SENT
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage.DOWNLOADING_FILE
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage.FAILURE
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage.FILE_DOWNLOADED
import com.amapi.extensibility.demo.customapp.CustomAppViewModel.UiState.Stage.START
import kotlinx.coroutines.launch

/**
 * Screen that shows the end-to-end flow for downloading, exposing, and installing a custom app APK,
 * demonstrating key use cases of extensibility SDK install & uninstall commands.
 *
 * @constructor Creates a new instance of [CustomAppActivity].
 */
class CustomAppActivity : ComponentActivity() {

  private lateinit var viewModel: CustomAppViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel =
      ViewModelProvider(this, CustomAppViewModel.CustomAppViewModelFactory(application))
        .get(CustomAppViewModel::class.java) // Use the custom factory

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        setContent {
          val uiState = viewModel.uiState.collectAsState()
          CustomInstallerActivityScreen(uiState.value)
        }
      }
    }
  }

  @Composable
  @OptIn(ExperimentalComposeUiApi::class)
  private fun CustomInstallerActivityScreen(uiState: CustomAppViewModel.UiState) {
    val scrollState = rememberScrollState()

    Scaffold(
      modifier = Modifier.semantics { testTagsAsResourceId = true },
      topBar = { CustomInstallerAppBar() },
    ) { paddingValues ->
      Column(
        modifier =
          Modifier.padding(paddingValues)
            .verticalScroll(state = scrollState)
            .fillMaxWidth()
            .then(Modifier.padding(20.dp))
      ) {
        when (uiState.uiStage) {
          START -> {
            InformationCard(text = getString(R.string.ui_stage_start_message))
          }
          DOWNLOADING_FILE -> {
            InformationCard(text = getString(R.string.ui_stage_downloading_file))
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator(
              color = MaterialTheme.colorScheme.secondary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))
          }
          FILE_DOWNLOADED -> {
            InformationCard(text = getString(R.string.ui_stage_file_downloaded_message))
          }
          COMMAND_SENT -> {
            InformationCard(text = getString(R.string.ui_stage_command_sent_message))
          }
          FAILURE -> {
            InformationCard(
              contentColor = MaterialTheme.colorScheme.error,
              containerColor = MaterialTheme.colorScheme.errorContainer,
              text = getErrorMessage(uiState.errorType, uiState.errorMessage),
            )
          }
        }
        if (uiState.commandStatus?.isNotEmpty() == true) {
          Spacer(modifier = Modifier.height(20.dp))
          SelectionContainer {
            InformationCard(text = uiState.commandStatus, textAlign = TextAlign.Start)
          }
        }
        Spacer(modifier = Modifier.height(20.dp))
        CommandsBlock()
        Spacer(modifier = Modifier.height(20.dp))
        AppDownloaderBlock(Modifier.wrapContentHeight())
        Spacer(modifier = Modifier.height(20.dp))
        DownloadedAppsBlock(filePaths = uiState.filePaths)
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun CustomInstallerAppBar() {
    TopAppBar(
      colors =
        TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.primary,
        ),
      title = { Text(text = getString(R.string.custom_app_screen_title)) },
    )
  }

  @Composable
  @OptIn(ExperimentalComposeUiApi::class)
  private fun CommandsBlock(modifier: Modifier = Modifier) {
    var packageName by remember { mutableStateOf("") }
    var handleFileProvider by remember { mutableStateOf(false) }

    SectionCard(title = getString(R.string.send_command_title)) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          modifier = Modifier.testTag(OPERATION_PACKAGE_NAME_TEST_TAG),
          value = packageName,
          onValueChange = { packageName = it },
          label = { Text(getString(R.string.app_package_name)) },
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(text = getString(R.string.handle_file_provider), modifier = modifier.width(200.dp))
          Switch(checked = handleFileProvider, onCheckedChange = { handleFileProvider = it })
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            modifier = modifier,
            onClick = { viewModel.installApp(packageName, handleFileProvider) },
          ) {
            Text(text = getString(R.string.install_app))
          }
          Button(modifier = modifier, onClick = { viewModel.uninstallApp(packageName) }) {
            Text(text = getString(R.string.uninstall_app))
          }
        }
      }
    }
  }

  @Composable
  private fun AppDownloaderBlock(modifier: Modifier = Modifier) {
    var packageName by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }

    SectionCard(title = getString(R.string.download_apk_title)) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          modifier = Modifier.testTag(DOWNLOAD_PACKAGE_NAME_TEST_TAG),
          value = packageName,
          onValueChange = { packageName = it },
          label = { Text(getString(R.string.app_package_name)) },
        )
        OutlinedTextField(
          modifier = Modifier.testTag(DOWNLOAD_URL_TEST_TAG),
          value = downloadUrl,
          onValueChange = { downloadUrl = it },
          label = { Text(getString(R.string.download_url)) },
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            modifier = modifier,
            onClick = {
              downloadUrl = DEMO_APP_URL
              packageName = DEMO_PACKAGE_NAME
            },
          ) {
            Text(text = getString(R.string.use_example_apk))
          }
          Button(
            modifier = modifier,
            onClick = { viewModel.downloadApk(packageName, downloadUrl) },
          ) {
            Text(text = getString(R.string.download_apk))
          }
        }
      }
    }
  }

  @Composable
  private fun DownloadedAppsBlock(filePaths: List<String>) {
    SectionCard(title = getString(R.string.downloaded_apks_title)) {
      SelectionContainer {
        LazyColumn(
          modifier = Modifier.padding(10.dp).heightIn(max = 200.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(filePaths) { filePath -> Text(filePath) }
          item {
            TextButton(
              onClick = { viewModel.resetDownloadedApps() },
              colors =
                ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
              Text(getString(R.string.delete_apks))
            }
          }
        }
      }
    }
  }

  @Composable
  private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Text(text = title, style = MaterialTheme.typography.titleMedium)
    OutlinedCard(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, Color.LightGray),
    ) {
      content()
    }
  }

  @Composable
  private fun InformationCard(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surface,
  ) {
    ElevatedCard(
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      colors =
        CardDefaults.cardColors(contentColor = contentColor, containerColor = containerColor),
      modifier = Modifier.wrapContentHeight().fillMaxWidth(),
    ) {
      Text(text = text, modifier = Modifier.padding(16.dp), textAlign = textAlign)
    }
  }

  private fun getErrorMessage(errorType: ErrorType?, errorMessage: String?): String {
    val uiErrorMessage =
      when (errorType) {
        ErrorType.EMPTY_PACKAGE_NAME -> getString(R.string.error_package_name_not_specified)
        ErrorType.EMPTY_DOWNLOAD_URL_OR_PACKAGE_NAME ->
          getString(R.string.ui_stage_empty_url_or_package_name_message)
        ErrorType.DOWNLOAD_FAILED -> getString(R.string.error_download_failed)
        ErrorType.COMMAND_EXECUTION_FAILED -> getString(R.string.error_command_execution_failed)
        else -> getString(R.string.ui_stage_command_failed_message_no_error)
      }
    return if (uiErrorMessage.isNotBlank() && !errorMessage.isNullOrBlank()) {
      "$uiErrorMessage $errorMessage"
    } else {
      uiErrorMessage + (errorMessage ?: "")
    }
  }

  @Preview
  @Composable
  private fun PreviewCustomAppActivityScreen() {
    CustomInstallerActivityScreen(uiState = CustomAppViewModel.UiState(START, filePaths = listOf()))
  }

  private companion object {
    // URL and package name of an example app (Android Material Catalog), used to prefill the
    // Download URL text field
    // for ease of testing.
    const val DEMO_APP_URL =
      "https://storage.googleapis.com/extensibility-test-app-custom-install-demo/androidx_compose_material_catalog.apk"
    const val DEMO_PACKAGE_NAME = "androidx.compose.material.catalog"
    const val OPERATION_PACKAGE_NAME_TEST_TAG = "operation_package_name"
    const val DOWNLOAD_PACKAGE_NAME_TEST_TAG = "download_package_name"
    const val DOWNLOAD_URL_TEST_TAG = "download_url"
  }
}
