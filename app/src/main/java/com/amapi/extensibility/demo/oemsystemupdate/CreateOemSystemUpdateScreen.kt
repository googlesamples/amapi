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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif as GoogleSansFamily
import androidx.compose.ui.unit.dp
import com.amapi.extensibility.demo.R

/** Screen for creating a new OEM system update. */
@Composable
fun CreateOemSystemUpdateScreen(
  initialName: String,
  uiState: OemSystemUpdateViewModel.UiState,
  onCreateClick: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var systemUpdateName by remember { mutableStateOf(initialName) }
  var isApiLevelChange by remember { mutableStateOf(false) }

  Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
    StatusSection(
      uiState = uiState,
      fontFamily = GoogleSansFamily,
      statusLabel = stringResource(R.string.status_label),
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = systemUpdateName,
      onValueChange = { systemUpdateName = it },
      label = {
        Text(stringResource(R.string.system_update_name_label), fontFamily = GoogleSansFamily)
      },
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(checked = isApiLevelChange, onCheckedChange = { isApiLevelChange = it })
      Text(text = stringResource(R.string.api_level_change), fontFamily = GoogleSansFamily)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = { onCreateClick(systemUpdateName, isApiLevelChange) },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = stringResource(R.string.create_button), fontFamily = GoogleSansFamily)
    }
  }
}
