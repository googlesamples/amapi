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
package com.amapi.extensibility.demo.commands

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amapi.extensibility.demo.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class CommandActivity : AppCompatActivity() {
  private lateinit var commandViewModel: CommandViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_command)
    commandViewModel = ViewModelProvider(this).get(CommandViewModel::class.java)
    commandViewModel.commandResultLiveData.observe(this) { results ->
      findViewById<TextView>(R.id.command_result_textview).apply {
        text = results
      }
    }
    findViewById<Button>(R.id.get_command_button).setOnClickListener {
      commandViewModel.getCommand(findViewById<EditText>(R.id.command_id_edittext).text.toString())
    }
    findViewById<Button>(R.id.clear_app_command_button).setOnClickListener {
      commandViewModel.issueClearAppDataCommand(
        findViewById<EditText>(R.id.clear_app_package_edittext).text.toString()
      )
    }
    findViewById<Button>(R.id.license_button).setOnClickListener {
      startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }
  }
}
