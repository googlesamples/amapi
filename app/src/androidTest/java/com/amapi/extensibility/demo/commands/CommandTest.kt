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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.amapi.extensibility.demo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests main activity from test app. */
@RunWith(AndroidJUnit4::class)
class CommandTest {
  @Rule
  @JvmField
  var mActivityRule: ActivityTestRule<CommandActivity> =
    ActivityTestRule(CommandActivity::class.java)

  @Test
  fun getCommandById() {
    onView(withId(R.id.command_id_edittext)).perform(replaceText("1"))
    onView(withId(R.id.get_command_button)).perform(click())
    onView(withId(R.id.command_result_textview))
      .check(ViewAssertions.matches(withSubstring("UNIMPLEMENTED")))
  }

  @Test
  fun issueCommand() {
    onView(withId(R.id.clear_app_package_edittext)).perform(replaceText("com.android.settings"))
    onView(withId(R.id.clear_app_command_button)).perform(click())
    onView(withId(R.id.command_result_textview))
      .check(ViewAssertions.matches(withSubstring("UNIMPLEMENTED")))
  }
}
