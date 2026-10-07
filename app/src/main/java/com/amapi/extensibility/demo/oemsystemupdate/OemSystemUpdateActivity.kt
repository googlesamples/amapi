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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif as GoogleSansFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.amapi.extensibility.demo.R
import com.google.android.managementapi.oemsystemupdate.model.UpdateControlState

/** Activity for demonstrating OEM system update operations. */
class OemSystemUpdateActivity : ComponentActivity() {

  private lateinit var viewModel: OemSystemUpdateViewModel

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel =
      ViewModelProvider(
        this,
        OemSystemUpdateViewModel.OemSystemUpdateViewModelFactory(application),
      )[OemSystemUpdateViewModel::class.java]

    setContent {
      val navController = rememberNavController()
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route

      val title =
        if (currentRoute?.startsWith("create") == true) {
          stringResource(R.string.create_oem_system_update_screen_title)
        } else {
          stringResource(R.string.oem_system_update_screen_title)
        }

      Scaffold(
        topBar = {
          TopAppBar(
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
              ),
            title = { Text(title, fontFamily = GoogleSansFamily) },
          )
        }
      ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        NavHost(
          navController = navController,
          startDestination = "overview",
          modifier = Modifier.padding(innerPadding),
        ) {
          composable("overview") {
            OemSystemUpdateScreen(
              uiState = uiState,
              onNavigateToCreate = { name -> navController.navigate("create?name=$name") },
            )
          }
          composable("create?name={name}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            CreateOemSystemUpdateScreen(
              initialName = name,
              uiState = uiState,
              onCreateClick = { updatedName, isApiChange ->
                viewModel.createSystemUpdate(updatedName, isApiChange)
              },
            )
          }
        }
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun OemSystemUpdateScreen(
    uiState: OemSystemUpdateViewModel.UiState,
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier,
  ) {
    var systemUpdateName by remember { mutableStateOf("systemUpdate/1") }
    val isUuc =
      uiState.currentControlState == UpdateControlState.ControlState.UNIFIED_UPDATE_CONTROL

    Column(modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
      StatusSection(
        uiState = uiState,
        fontFamily = GoogleSansFamily,
        statusLabel = stringResource(R.string.status_label),
      )

      Spacer(modifier = Modifier.height(16.dp))

      var expanded by remember { mutableStateOf(false) }

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
      ) {
        OutlinedTextField(
          value = uiState.currentControlStateLabel,
          onValueChange = {},
          readOnly = true,
          label = { Text("Update Control State", fontFamily = GoogleSansFamily) },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
          modifier =
            Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
              .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          for ((label, state) in uiState.controlStateOptions) {
            DropdownMenuItem(
              text = { Text(label, fontFamily = GoogleSansFamily) },
              onClick = {
                viewModel.updateUpdateControlState(state)
                expanded = false
              },
              contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = systemUpdateName,
        onValueChange = { systemUpdateName = it },
        label = {
          Text(stringResource(R.string.system_update_name_label), fontFamily = GoogleSansFamily)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isUuc,
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { viewModel.getSystemUpdate(systemUpdateName) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isUuc,
      ) {
        Text(text = stringResource(R.string.get_system_update), fontFamily = GoogleSansFamily)
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { onNavigateToCreate(systemUpdateName) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isUuc,
      ) {
        Text(text = stringResource(R.string.create_system_update), fontFamily = GoogleSansFamily)
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { viewModel.listSystemUpdates() },
        modifier = Modifier.fillMaxWidth(),
        enabled = isUuc,
      ) {
        Text(text = stringResource(R.string.list_system_updates), fontFamily = GoogleSansFamily)
      }

      Spacer(modifier = Modifier.height(16.dp))

      var showReportDialog by remember { mutableStateOf(false) }
      val reportTypes =
        listOf(
          "Downloading",
          "Downloaded",
          "Applied",
          "Aborted",
          "DownloadingWithOverride",
          "DownloadedWithOverride",
          "AppliedWithOverride",
          "DownloadPrevented",
          "ApplyPrevented",
        )

      Button(
        onClick = { showReportDialog = true },
        modifier = Modifier.fillMaxWidth(),
        enabled = isUuc,
      ) {
        Text(text = "Report System Update", fontFamily = GoogleSansFamily)
      }

      if (showReportDialog) {
        androidx.compose.material3.AlertDialog(
          onDismissRequest = { showReportDialog = false },
          title = { Text("Select Report Type", fontFamily = GoogleSansFamily) },
          text = {
            Column {
              for (type in reportTypes) {
                androidx.compose.material3.TextButton(
                  onClick = {
                    when (type) {
                      "Downloading" -> viewModel.reportSystemUpdateDownloading(systemUpdateName)
                      "Downloaded" -> viewModel.reportSystemUpdateDownloaded(systemUpdateName)
                      "Applied" -> viewModel.reportSystemUpdateApplied(systemUpdateName)
                      "Aborted" -> viewModel.reportSystemUpdateAborted(systemUpdateName)
                      "DownloadingWithOverride" ->
                        viewModel.reportSystemUpdateDownloadingWithOverride(systemUpdateName)
                      "DownloadedWithOverride" ->
                        viewModel.reportSystemUpdateDownloadedWithOverride(systemUpdateName)
                      "AppliedWithOverride" ->
                        viewModel.reportSystemUpdateAppliedWithOverride(systemUpdateName)
                      "DownloadPrevented" ->
                        viewModel.reportSystemUpdateDownloadPrevented(systemUpdateName)
                      "ApplyPrevented" ->
                        viewModel.reportSystemUpdateApplyPrevented(systemUpdateName)
                    }
                    showReportDialog = false
                  },
                ) {
                  Text(type, fontFamily = GoogleSansFamily)
                }
              }
            }
          },
          confirmButton = {
            androidx.compose.material3.TextButton(onClick = { showReportDialog = false }) {
              Text("Cancel", fontFamily = GoogleSansFamily)
            }
          },
        )
      }
    }
  }
}
