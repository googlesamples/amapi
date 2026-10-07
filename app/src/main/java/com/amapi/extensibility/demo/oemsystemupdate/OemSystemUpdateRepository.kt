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

import com.google.android.managementapi.oemsystemupdate.model.SystemUpdate
import com.google.android.managementapi.oemsystemupdate.model.UpdateControlState

/** Repository for OEM system update operations. */
interface OemSystemUpdateRepository {
  /** Creates a new [SystemUpdate]. */
  suspend fun createSystemUpdate(name: String, isApiLevelChange: Boolean): Result<SystemUpdate>

  /** Gets an existing [SystemUpdate] by name. */
  suspend fun getSystemUpdate(name: String): Result<SystemUpdate>

  /** Lists all existing [SystemUpdate]s. */
  suspend fun listSystemUpdates(): Result<List<SystemUpdate>>

  /** Reports that the download of a system update has started. */
  suspend fun reportSystemUpdateDownloading(name: String): Result<Unit>

  /** Reports that a system update has been successfully downloaded. */
  suspend fun reportSystemUpdateDownloaded(name: String): Result<Unit>

  /** Reports that a system update has been successfully applied. */
  suspend fun reportSystemUpdateApplied(name: String): Result<Unit>

  /** Reports that a system update has been aborted. */
  suspend fun reportSystemUpdateAborted(name: String): Result<Unit>

  /** Reports that a system update download has started, overriding instructions. */
  suspend fun reportSystemUpdateDownloadingWithOverride(name: String): Result<Unit>

  /** Reports that a system update has been downloaded, overriding instructions. */
  suspend fun reportSystemUpdateDownloadedWithOverride(name: String): Result<Unit>

  /** Reports that a system update has been applied, overriding instructions. */
  suspend fun reportSystemUpdateAppliedWithOverride(name: String): Result<Unit>

  /** Reports that a system update download is prevented. */
  suspend fun reportSystemUpdateDownloadPrevented(name: String): Result<Unit>

  /** Reports that a system update apply is prevented. */
  suspend fun reportSystemUpdateApplyPrevented(name: String): Result<Unit>

  /** Updates the system update control state. */
  suspend fun updateUpdateControlState(
    controlState: UpdateControlState.ControlState
  ): Result<UpdateControlState>
}
