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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.managementapi.commands.model.Command
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object InMemoryCommandRepository {
  private val _commandResult = MutableStateFlow("")
  val commandResult = _commandResult.asStateFlow()
  private val commandLiveData: MutableLiveData<Command> = MutableLiveData()

  fun onCommandStatusChanged(command: Command) {
    commandLiveData.postValue(command)
    _commandResult.value = CommandUtils.parseCommandForPrettyPrint(command)
  }

  fun getCommandLiveData(): LiveData<Command> {
    return commandLiveData
  }

  fun setValue(value: String) {
    _commandResult.value = value
  }
}
