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

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.amapi.extensibility.demo.R

/** Shared UI components for OEM System Update screens. */
@Composable
fun StatusSection(
  uiState: OemSystemUpdateViewModel.UiState,
  fontFamily: FontFamily,
  statusLabel: String,
) {
  Text(text = statusLabel, fontFamily = fontFamily, fontWeight = FontWeight.Bold)
  SelectionContainer {
    val color =
      if (uiState.state == OemSystemUpdateViewModel.UiState.State.ERROR) {
        Color.Red
      } else if (uiState.state == OemSystemUpdateViewModel.UiState.State.SUCCESS) {
        colorResource(id = R.color.success_green)
      } else if (uiState.statusMessage.isNotEmpty()) {
        colorResource(id = R.color.status_yellow)
      } else {
        Color.Unspecified
      }
    Text(
      text = uiState.statusMessage,
      fontFamily = fontFamily,
      color = color,
    )
  }
}
