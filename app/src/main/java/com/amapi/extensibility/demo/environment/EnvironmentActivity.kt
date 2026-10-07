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
package com.amapi.extensibility.demo.environment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.amapi.extensibility.demo.R
import com.google.android.managementapi.common.model.Role
import kotlinx.coroutines.launch

/** Screen for environment feature. */
class EnvironmentActivity : ComponentActivity() {
  private lateinit var viewModel: EnvironmentViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel = ViewModelProvider(this)[EnvironmentViewModel::class.java]
    setContent {
      val uiState by viewModel.environmentResult.collectAsState()
      val snackbarHostState = remember { SnackbarHostState() }
      val scope = rememberCoroutineScope()

      Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        EnvironmentView(
          environmentResponse = uiState,
          onShowSnackbar = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
          modifier = Modifier.padding(innerPadding),
        )
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun EnvironmentView(
    environmentResponse: String,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
  ) {
    var selectedRoles by remember {
      mutableStateOf<Set<Role.RoleType>>(setOf(Role.RoleType.IDENTITY_PROVIDER))
    }

    val roleOptions = Role.RoleType.entries.filter { it != Role.RoleType.ROLE_TYPE_UNSPECIFIED }
    var expanded by remember { mutableStateOf(false) }

    fun getRoleLabel(role: Role.RoleType): String {
      return role.name.split('_').joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
      }
    }

    @Composable
    fun getSummaryLabel(roles: Set<Role.RoleType>): String {
      if (roles.isEmpty()) return stringResource(id = R.string.role_none)
      if (roles.size == 1) return getRoleLabel(roles.first())
      return "${roles.size} roles selected"
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
      ) {
        OutlinedTextField(
          value = getSummaryLabel(selectedRoles),
          onValueChange = {},
          readOnly = true,
          label = { Text("Select Roles") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
          modifier =
            Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
              .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          for (role in roleOptions) {
            DropdownMenuItem(
              text = { Text(getRoleLabel(role)) },
              onClick = {
                selectedRoles =
                  if (role in selectedRoles) {
                    selectedRoles - role
                  } else {
                    selectedRoles + role
                  }
              },
              trailingIcon = { Checkbox(checked = role in selectedRoles, onCheckedChange = null) },
              contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
          }
        }
      }

      val gettingEnvMessage = stringResource(id = R.string.getting_environment)
      Button(
        onClick = {
          onShowSnackbar(gettingEnvMessage)
          viewModel.getEnvironment(selectedRoles)
        }
      ) {
        Text(text = stringResource(id = R.string.get_environment))
      }
      val preparingEnvMessage = stringResource(id = R.string.preparing_environment)
      Button(
        onClick = {
          onShowSnackbar(preparingEnvMessage)
          viewModel.prepareEnvironment(selectedRoles)
        }
      ) {
        Text(text = stringResource(id = R.string.prepare_environment))
      }

      val scrollState = rememberScrollState()
      Text(
        text = environmentResponse,
        style = TextStyle(background = MaterialTheme.colorScheme.surface),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f).verticalScroll(scrollState),
      )
    }
  }
}
