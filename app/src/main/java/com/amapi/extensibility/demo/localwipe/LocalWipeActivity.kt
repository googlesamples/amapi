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
package com.amapi.extensibility.demo.localwipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.amapi.extensibility.demo.R

class LocalWipeActivity : ComponentActivity() {
  private lateinit var viewModel: LocalWipeViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = ViewModelProvider(this).get(LocalWipeViewModel::class.java)

    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
      ) {
        val commandResult by viewModel.commandResult.collectAsState()
        LocalWipeScreen(commandResult)
      }
    }
  }

  @Composable
  fun LocalWipeScreen(commandResult: String) {
    var commandId by remember { mutableStateOf("") }
    var wipeReason by remember { mutableStateOf("") }
    var preserveResetProtection by remember { mutableStateOf(false) }
    var wipeExternalStorage by remember { mutableStateOf(false) }
    var wipeEsims by remember { mutableStateOf(false) }

    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
      Text(
        text = commandResult,
      )

      OutlinedTextField(
        value = commandId,
        onValueChange = { commandId = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.command_id_hint)) },
        singleLine = true,
      )

      Button(
        onClick = { viewModel.getCommand(commandId) },
      ) {
        Text(stringResource(R.string.get_command_button))
      }

      OutlinedTextField(
        value = wipeReason,
        onValueChange = { wipeReason = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.wipe_reason_hint)) },
        singleLine = true,
      )

      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
          checked = preserveResetProtection,
          onCheckedChange = { preserveResetProtection = it },
        )
        Text(stringResource(R.string.preserve_reset_protection_label))
      }

      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
          checked = wipeExternalStorage,
          onCheckedChange = { wipeExternalStorage = it },
        )
        Text(stringResource(R.string.wipe_external_storage_label))
      }

      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
          checked = wipeEsims,
          onCheckedChange = { wipeEsims = it },
        )
        Text(stringResource(R.string.wipe_esims_label))
      }

      Button(
        onClick = {
          viewModel.issueWipeCommand(
            preserveResetProtection = preserveResetProtection,
            wipeExternalStorage = wipeExternalStorage,
            wipeEsims = wipeEsims,
            wipeReason = wipeReason,
          )
        },
      ) {
        Text(stringResource(R.string.issue_wipe_button))
      }
    }
  }
}
