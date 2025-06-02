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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.amapi.extensibility.demo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for CommandActivity.
 */
@RunWith(AndroidJUnit4::class)
class CommandTest {
  /**
   *  Replaced ActivityTestRule with ActivityScenarioRule to better manage the Activity's lifecycle.
   */
  @get:Rule
  val activityRule = ActivityScenarioRule(CommandActivity::class.java)

  /**
   * Tests retrieving a command by ID.
   */
  @Test
  fun getCommandById_displaysUnimplementedMessage() {
    // Arrange: Input a command ID.
    onView(withId(R.id.command_id_edittext)).perform(replaceText("1"))

    // Act: Click the button to retrieve the command.
    onView(withId(R.id.get_command_button)).perform(click())

    // Assert: Verify the result text view displays the expected "UNIMPLEMENTED" message.
    onView(withId(R.id.command_result_textview))
      .check(ViewAssertions.matches(withSubstring("UNIMPLEMENTED")))
  }

  @Test
  fun testClearAppDataCommand_editTextIsPresent(){
    onView(withId(R.id.clear_app_package_edittext))
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
  }

  @Test
  fun testClearAppDataCommand_editTextCanBeFilled(){
    onView(withId(R.id.clear_app_package_edittext)).perform(replaceText(TEST_PACKAGE_NAME))
    onView(withId(R.id.clear_app_package_edittext))
      .check(ViewAssertions.matches(ViewMatchers.withText(TEST_PACKAGE_NAME)))
  }

  @Test
  fun testClearAppDataCommand_buttonIsPresent(){
    onView(withId(R.id.clear_app_command_button))
      .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
  }

  /**
   * Verifies that attempting to clear an app's data via the UI results in the correct "UNIMPLEMENTED"
   * message being displayed. This checks that the command is received, but the action is not supported yet.
   */
  @Test
  fun testClearAppDataCommand_displaysUnimplementedMessage() {
    onView(withId(R.id.clear_app_package_edittext)).perform(replaceText(TEST_PACKAGE_NAME))
    onView(withId(R.id.clear_app_command_button)).perform(click())
    onView(withId(R.id.command_result_textview))
      .check(ViewAssertions.matches(withSubstring("UNIMPLEMENTED")))
  }

  companion object {
    private const val TEST_PACKAGE_NAME = "com.android.settings"
  }
}