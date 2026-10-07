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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amapi.extensibility.demo.R
import kotlinx.coroutines.launch

/** Screen for Device posture feature. */
class DeviceTrustSignalsActivity : ComponentActivity() {
  private lateinit var viewModel: DeviceTrustSignalsViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = ViewModelProvider(this)[DeviceTrustSignalsViewModel::class.java]
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        setContent {
          val uiState = viewModel.deviceTrustResult.collectAsState()
          SignalsView(uiState.value)
        }
      }
    }
  }

  @Composable
  fun SignalsView(signals: String, modifier: Modifier = Modifier) {
    Column {
      Button(modifier = modifier, onClick = { viewModel.getDeviceTrust() }) {
        Text(text = getString(R.string.get_device_trust))
      }

      val scrollState = rememberScrollState()
      Text(
        text = signals,
        style = TextStyle(background = Color.White),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.verticalScroll(scrollState),
      )
    }
  }

  companion object {
    private const val TAG = "DevicePostureSignalsActivity"
  }
}
