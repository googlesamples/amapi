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
package com.amapi.extensibility.demo.approles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.amapi.extensibility.demo.R
import com.google.android.managementapi.approles.model.AppRolesSetRequest
import kotlinx.coroutines.flow.StateFlow

class AppRolesViewModel : ViewModel() {
  val appRolesSetRequest: StateFlow<AppRolesSetRequest?> =
    InMemoryAppRolesRepository.appRolesSetRequest
}

class AppRolesActivity : ComponentActivity() {
  private val viewModel: AppRolesViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Scaffold(
          topBar = {
            TopAppBar(
              colors =
                TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                  titleContentColor = MaterialTheme.colorScheme.primary,
                ),
              title = { Text(stringResource(R.string.app_roles_title)) },
            )
          }
        ) { innerPadding ->
          AppRolesScreen(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            viewModel = viewModel,
          )
        }
      }
    }
  }
}

@Composable
fun AppRolesScreen(modifier: Modifier = Modifier, viewModel: AppRolesViewModel) {
  val request by viewModel.appRolesSetRequest.collectAsState()

  Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
    Column(modifier = Modifier.padding(20.dp)) {
      Text(text = "App Roles and Capabilities:")
      Text(
        modifier = Modifier.padding(top = 10.dp),
        text = request?.roles?.toString() ?: "No roles received yet.",
      )
    }
  }
}
