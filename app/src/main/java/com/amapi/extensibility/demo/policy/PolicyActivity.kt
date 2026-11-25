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

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amapi.extensibility.demo.R

/**
 * Demonstrates the `PolicyClient` API with one button per client method.
 *
 * Debuggable builds can also be scripted, for example from `adb shell am start`: when launched with
 * [EXTRA_POLICY_JSON] and [EXTRA_AUTO_APPLY] set to `true`, the activity replaces the enforced
 * policy with the parsed JSON (see [PolicyJsonParser]).
 */
class PolicyActivity : ComponentActivity() {

  private val viewModel: PolicyViewModel by viewModels { PolicyViewModel.Factory(application) }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) {
      val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
      policyJsonToApply(intent, isDebuggable)?.let(viewModel::applyPolicyJson)
    }

    setContent {
      Scaffold(
        topBar = {
          TopAppBar(
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
              ),
            title = { Text(stringResource(R.string.policy_screen_title)) },
          )
        }
      ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        PolicyScreen(
          uiState = uiState,
          onGetPolicy = viewModel::getPolicy,
          onUpdatePolicy = viewModel::updatePolicy,
          modifier = Modifier.padding(innerPadding),
        )
      }
    }
  }

  companion object {

    /** String extra with the policy JSON to apply. */
    const val EXTRA_POLICY_JSON = "extra_policy_json"

    /** Boolean extra that must be `true` for [EXTRA_POLICY_JSON] to be applied. */
    const val EXTRA_AUTO_APPLY = "extra_auto_apply"

    private const val TAG = "PolicyActivity"

    /**
     * Returns the policy JSON to apply from [intent], or `null` if there is none.
     *
     * The JSON is only applied when [EXTRA_AUTO_APPLY] is `true` and the app is debuggable, because
     * this activity is exported. Relaunches from Recents are ignored so they don't re-apply it.
     */
    fun policyJsonToApply(intent: Intent, isDebuggable: Boolean): String? {
      val policyJson = intent.getStringExtra(EXTRA_POLICY_JSON) ?: return null
      if (!intent.getBooleanExtra(EXTRA_AUTO_APPLY, /* defaultValue= */ false)) return null
      if ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) return null
      if (!isDebuggable) {
        Log.w(TAG, "Ignoring $EXTRA_POLICY_JSON: only debuggable builds apply scripted policies")
        return null
      }
      return policyJson
    }
  }
}

@Composable
private fun PolicyScreen(
  uiState: PolicyViewModel.UiState,
  onGetPolicy: () -> Unit,
  onUpdatePolicy: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
    Text(stringResource(R.string.status_label), fontWeight = FontWeight.Bold)
    PolicyStatusText(uiState)
    Spacer(Modifier.height(16.dp))
    PolicyButton(R.string.get_policy, onGetPolicy)
    PolicyButton(R.string.update_policy, onUpdatePolicy)
  }
}

@Composable
private fun PolicyStatusText(uiState: PolicyViewModel.UiState) {
  val color =
    when (uiState.state) {
      PolicyViewModel.UiState.State.ERROR -> Color.Red
      PolicyViewModel.UiState.State.SUCCESS -> colorResource(R.color.success_green)
      PolicyViewModel.UiState.State.PENDING -> colorResource(R.color.status_yellow)
      PolicyViewModel.UiState.State.IDLE -> Color.Unspecified
    }
  Text(
    text = uiState.statusMessage,
    color = color,
  )
}

@Composable
private fun PolicyButton(@StringRes textId: Int, onClick: () -> Unit) {
  Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Text(stringResource(textId))
  }
}
