/* Copyright 2020 Google LLC
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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amapi.extensibility.demo.databinding.ActivityCommandBinding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class CommandActivity : AppCompatActivity() {
  private lateinit var commandViewModel: CommandViewModel
  private lateinit var binding: ActivityCommandBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityCommandBinding.inflate(layoutInflater)
    setContentView(binding.root)

    commandViewModel = ViewModelProvider(this)[CommandViewModel::class.java]
    commandViewModel.commandResultLiveData.observe(this) { results ->
      binding.commandResultTextview.text = results
    }

    setupListeners()
  }

  private fun setupListeners() {
    binding.getCommandButton.setOnClickListener {
      commandViewModel.getCommand(binding.commandIdEdittext.text.toString())
      }
    binding.clearAppCommandButton.setOnClickListener {
      commandViewModel.issueClearAppDataCommand(binding.clearAppPackageEdittext.text.toString())
      }
    binding.licenseButton.setOnClickListener {
      startActivity(Intent(this, OssLicensesMenuActivity::class.java))
      }
  }
}
