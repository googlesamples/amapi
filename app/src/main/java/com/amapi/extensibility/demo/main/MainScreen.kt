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
package com.amapi.extensibility.demo.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amapi.extensibility.demo.R
import com.amapi.extensibility.demo.accountsetup.AccountSetupActivity
import com.amapi.extensibility.demo.approles.AppRolesActivity
import com.amapi.extensibility.demo.commands.CommandActivity
import com.amapi.extensibility.demo.customapp.CustomAppActivity
import com.amapi.extensibility.demo.devicetrust.DeviceTrustSignalsActivity
import com.amapi.extensibility.demo.dpcmigration.DpcMigrationActivity
import com.amapi.extensibility.demo.environment.EnvironmentActivity
import com.amapi.extensibility.demo.oemsystemupdate.OemSystemUpdateActivity
import com.amapi.extensibility.demo.requestdeviceinfo.RequestDeviceInfoActivity

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainActivityContent(modifier: Modifier = Modifier, navItems: List<NavigationItem>) {
  Scaffold(
    topBar = {
      TopAppBar(
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
          ),
        title = { Text(stringResource(R.string.app_name)) },
      )
    }
  ) { innerPadding ->
    ExtensibilityDemoScreen(Modifier.fillMaxSize().padding(innerPadding), navItems)
  }
}

@Composable
fun ExtensibilityDemoScreen(modifier: Modifier = Modifier, navItems: List<NavigationItem>) {
  val context = LocalContext.current
  Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
    LazyColumn(
      modifier = Modifier.padding(all = 20.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(navItems) { item ->
        NavigationButton(
          name = stringResource(item.nameResId),
          onClick = { context.navigateTo(item.activity) },
          modifier = Modifier.wrapContentHeight(),
        )
      }
    }
  }
}

fun Context.navigateTo(activityClass: Class<out Activity>) =
  startActivity(Intent(this, activityClass))

@Composable
fun NavigationButton(name: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
  ElevatedButton(modifier = modifier, onClick = { onClick() }) { Text(text = name) }
}

data class NavigationItem(val nameResId: Int, val activity: Class<out Activity>)

val navigationItems =
  listOf(
    NavigationItem(R.string.local_commands, CommandActivity::class.java),
    NavigationItem(R.string.custom_app, CustomAppActivity::class.java),
    NavigationItem(R.string.request_device_info_title, RequestDeviceInfoActivity::class.java),
    NavigationItem(R.string.account_setup, AccountSetupActivity::class.java),
    NavigationItem(R.string.dpc_migration, DpcMigrationActivity::class.java),
    NavigationItem(R.string.device_trust, DeviceTrustSignalsActivity::class.java),
    NavigationItem(R.string.environment_title, EnvironmentActivity::class.java),
    NavigationItem(R.string.oem_system_update_screen_title, OemSystemUpdateActivity::class.java),
    NavigationItem(R.string.app_roles_title, AppRolesActivity::class.java),
    NavigationItem(R.string.license_button_txt, OssLicensesMenuActivity::class.java),
  )
