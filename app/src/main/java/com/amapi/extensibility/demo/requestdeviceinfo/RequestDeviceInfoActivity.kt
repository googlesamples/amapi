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
package com.amapi.extensibility.demo.requestdeviceinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import kotlinx.coroutines.launch

/** Screen where request device info local commands can be issued and status can be obtained */
class RequestDeviceInfoActivity : ComponentActivity() {
  private lateinit var requestDeviceInfoViewModel: RequestDeviceInfoViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Instantiate ViewModel using the Factory
    val factory = RequestDeviceInfoViewModel.RequestDeviceInfoViewModelFactory(application)
    requestDeviceInfoViewModel =
      ViewModelProvider(this, factory)[RequestDeviceInfoViewModel::class.java]

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        setContent {
          val uiState by requestDeviceInfoViewModel.uiState.collectAsState()
          val commandResult by requestDeviceInfoViewModel.commandResult.collectAsState()
          RequestDeviceInfoActivityScreen(
            uiState = uiState,
            commandResult = commandResult,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }
    }
  }

  @Composable
  @OptIn(ExperimentalComposeUiApi::class)
  private fun RequestDeviceInfoActivityScreen(
    uiState: RequestDeviceInfoViewModel.CommandUiState,
    commandResult: String,
    modifier: Modifier,
  ) {
    val scrollState = rememberScrollState()
    Surface(
      modifier =
        modifier.fillMaxSize().semantics { testTagsAsResourceId = true }, // Fill the entire screen
      color = MaterialTheme.colorScheme.background,
    ) {
      Column(
        // Use padding for the entire screen's content
        modifier = Modifier.fillMaxSize().padding(all = 8.dp).verticalScroll(state = scrollState),
        // Center all children horizontally
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = getString(R.string.request_device_info_title),
          style = MaterialTheme.typography.headlineMedium,
          modifier = Modifier.padding(bottom = 10.dp),
        )

        GetCommand()
        IssueRequestDeviceInfoCommand()

        Spacer(modifier = Modifier.height(4.dp))

        when (uiState) {
          is RequestDeviceInfoViewModel.CommandUiState.Idle -> {
            // do nothing
          }
          is RequestDeviceInfoViewModel.CommandUiState.Error -> {
            InformationCard(
              text = uiState.message,
              contentColor = MaterialTheme.colorScheme.error,
              containerColor = MaterialTheme.colorScheme.errorContainer,
            )
          }
          is RequestDeviceInfoViewModel.CommandUiState.Success -> {
            InformationCard(
              text = uiState.message,
              contentColor = MaterialTheme.colorScheme.primary,
              containerColor = MaterialTheme.colorScheme.primaryContainer,
            )
          }
          is RequestDeviceInfoViewModel.CommandUiState.InvalidDeviceInfo -> {
            InformationCard(
              text = uiState.message,
              contentColor = MaterialTheme.colorScheme.error,
              containerColor = MaterialTheme.colorScheme.errorContainer,
            )
          }
        }
      }
    }
  }

  @Composable
  private fun GetCommand(modifier: Modifier = Modifier) {
    var commandId by remember { mutableStateOf("") }
    OutlinedTextField(
      value = commandId,
      onValueChange = { commandId = it },
      label = { Text(getString(R.string.command_id)) },
      modifier = Modifier.testTag(COMMAND_ID_TAG),
    )
    Spacer(modifier = Modifier.height(10.dp))
    ElevatedButton(onClick = { requestDeviceInfoViewModel.getCommand(commandId) }) {
      Text(text = getString(R.string.get_command))
    }
    Spacer(modifier = Modifier.height(10.dp))
  }

  @Composable
  private fun IssueRequestDeviceInfoCommand(modifier: Modifier = Modifier) {
    var deviceInfo by remember { mutableStateOf("") }
    OutlinedTextField(
      value = deviceInfo,
      onValueChange = { deviceInfo = it },
      label = { Text(getString(R.string.device_info)) },
      placeholder = { Text(getString(R.string.device_info_placeholder)) },
      modifier = Modifier.testTag(DEVICE_INFO_TAG),
    )
    ElevatedButton(
      onClick = { requestDeviceInfoViewModel.issueRequestDeviceInfoCommand(deviceInfo) }
    ) {
      Text(text = getString(R.string.issue_request_device_info_command))
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

  @Preview
  @Composable
  private fun PreviewLocalCommandActivityScreen() {
    RequestDeviceInfoActivityScreen(
      RequestDeviceInfoViewModel.CommandUiState.Idle,
      "Preview",
      modifier = Modifier.fillMaxSize(),
    )
  }

  companion object {
    const val COMMAND_ID_TAG = "command_id"
    const val DEVICE_INFO_TAG = "device_info"
  }
}
