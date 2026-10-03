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
package com.amapi.extensibility.demo.accountsetup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.amapi.extensibility.demo.R
import kotlinx.coroutines.launch

/** Activity for Account setup related operations. */
class AccountSetupActivity : ComponentActivity() {

  private lateinit var viewModel: AccountSetupViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val factory =
      AccountSetupViewModel.AccountSetupViewModelFactory(application, activityResultRegistry)
    viewModel = ViewModelProvider(this, factory)[AccountSetupViewModel::class.java]
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        setContent {
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val accountSetupResult by viewModel.accountSetupResult.collectAsState()
            AccountSetupScreen(accountSetupResult)
          }
        }
      }
    }
  }

  @Composable
  @OptIn(ExperimentalMaterial3Api::class)
  private fun AccountSetupScreen(accountSetupResult: String?) {
    Scaffold(
      topBar = {
        TopAppBar(
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              titleContentColor = MaterialTheme.colorScheme.primary,
            ),
          title = { Text(text = stringResource(R.string.account_setup)) },
        )
      }
    ) { innerPadding ->
      AccountSetupScreenContent(accountSetupResult, Modifier.padding(innerPadding))
    }
  }

  @Composable
  private fun AccountSetupScreenContent(
    accountSetupResult: String?,
    modifier: Modifier = Modifier,
  ) {
    val scrollState = rememberScrollState()

    Column(
      modifier = modifier.then(Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val buttonModifier = Modifier.fillMaxWidth()

      Text(
        text = accountSetupResult ?: stringResource(R.string.account_setup_result),
        modifier = Modifier.padding(bottom = 16.dp),
        color = MaterialTheme.colorScheme.onSurface,
      )

      var enrollmentToken by remember { mutableStateOf("TOKEN") }

      TextField(
        value = enrollmentToken,
        onValueChange = { enrollmentToken = it },
        label = { Text("Enrollment Token") },
        modifier = Modifier.fillMaxWidth(),
      )

      Button(
        onClick = { viewModel.startAccountSetup(enrollmentToken) },
        modifier = buttonModifier,
      ) {
        Text(text = stringResource(R.string.start_account_setup_button))
      }

      Button(onClick = viewModel::launchAuthenticationActivity, modifier = buttonModifier) {
        Text(text = stringResource(R.string.launch_auth_activity_button))
      }

      Button(onClick = viewModel::listAccountSetupAttempts, modifier = buttonModifier) {
        Text(text = stringResource(R.string.list_account_setup_attempts_button))
      }

      Button(onClick = viewModel::cancelAccountSetupAttempt, modifier = buttonModifier) {
        Text(text = stringResource(R.string.cancel_account_setup_attempt_button))
      }
    }
  }
}
