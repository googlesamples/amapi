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

import android.content.Context
import com.google.android.managementapi.oemsystemupdate.OemSystemUpdateClientFactory
import com.google.android.managementapi.oemsystemupdate.model.CreateSystemUpdateRequest
import com.google.android.managementapi.oemsystemupdate.model.GetSystemUpdateRequest
import com.google.android.managementapi.oemsystemupdate.model.ListSystemUpdatesRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateAbortedRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateAppliedRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateAppliedWithOverrideRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateApplyPreventedRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateDownloadPreventedRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateDownloadedRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateDownloadedWithOverrideRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateDownloadingRequest
import com.google.android.managementapi.oemsystemupdate.model.ReportSystemUpdateDownloadingWithOverrideRequest
import com.google.android.managementapi.oemsystemupdate.model.SystemUpdate
import com.google.android.managementapi.oemsystemupdate.model.SystemUpdateView
import com.google.android.managementapi.oemsystemupdate.model.UpdateControlState
import com.google.android.managementapi.oemsystemupdate.model.UpdateUpdateControlStateRequest
import java.time.Instant
import kotlinx.coroutines.guava.await

/**
 * Implementation of [OemSystemUpdateRepository] that uses [OemSystemUpdateClient] for performing
 * system update operations.
 */
class InMemoryOemSystemUpdateRepository(private val context: Context) : OemSystemUpdateRepository {

  private val client = OemSystemUpdateClientFactory.create(context)

  override suspend fun createSystemUpdate(
    name: String,
    isApiLevelChange: Boolean,
  ): Result<SystemUpdate> =
    try {
      val apiLevelChange =
        if (isApiLevelChange) {
          SystemUpdate.ApiLevelChange.API_LEVEL_CHANGE_WITH_API_LEVEL_CHANGE
        } else {
          SystemUpdate.ApiLevelChange.API_LEVEL_CHANGE_WITHOUT_API_LEVEL_CHANGE
        }
      val request =
        CreateSystemUpdateRequest.builder()
          .setSystemUpdate(
            SystemUpdate.builder()
              .setName(name)
              .setApiLevelChange(apiLevelChange)
              .setSecurityPatchLevel("2026-06-15")
              .setReceivedTime(Instant.now())
              .build()
          )
          .build()
      val response = client.createSystemUpdateFuture(request).await()
      Result.success(response)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun getSystemUpdate(name: String): Result<SystemUpdate> =
    try {
      val request =
        GetSystemUpdateRequest.builder()
          .setName(name)
          .setView(SystemUpdateView.SYSTEM_UPDATE_VIEW_FULL)
          .build()
      val response = client.getSystemUpdateFuture(request).await()
      Result.success(response)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun listSystemUpdates(): Result<List<SystemUpdate>> =
    try {
      val request =
        ListSystemUpdatesRequest.builder().setView(SystemUpdateView.SYSTEM_UPDATE_VIEW_FULL).build()
      val response = client.listSystemUpdatesFuture(request).await()
      Result.success(response.systemUpdates)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateDownloading(name: String): Result<Unit> =
    try {
      val request = ReportSystemUpdateDownloadingRequest.builder().setName(name).build()
      client.reportSystemUpdateDownloadingFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateDownloaded(name: String): Result<Unit> =
    try {
      val request = ReportSystemUpdateDownloadedRequest.builder().setName(name).build()
      client.reportSystemUpdateDownloadedFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateApplied(name: String): Result<Unit> =
    try {
      val request = ReportSystemUpdateAppliedRequest.builder().setName(name).build()
      client.reportSystemUpdateAppliedFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateAborted(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.AbortReason.builder()
          .setAbortReasonType(
            SystemUpdate.SystemUpdateState.AbortReason.AbortReasonType.NEWER_UPDATE_AVAILABLE
          )
          .build()
      val request =
        ReportSystemUpdateAbortedRequest.builder().setName(name).setReason(reason).build()
      client.reportSystemUpdateAbortedFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateDownloadingWithOverride(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.OverrideReason.builder()
          .setReasonType(
            SystemUpdate.SystemUpdateState.OverrideReason.OverrideReasonType.EMERGENCY_FIX
          )
          .build()
      val request =
        ReportSystemUpdateDownloadingWithOverrideRequest.builder()
          .setName(name)
          .setReason(reason)
          .build()
      client.reportSystemUpdateDownloadingWithOverrideFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateDownloadedWithOverride(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.OverrideReason.builder()
          .setReasonType(
            SystemUpdate.SystemUpdateState.OverrideReason.OverrideReasonType.EMERGENCY_FIX
          )
          .build()
      val request =
        ReportSystemUpdateDownloadedWithOverrideRequest.builder()
          .setName(name)
          .setReason(reason)
          .build()
      client.reportSystemUpdateDownloadedWithOverrideFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateAppliedWithOverride(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.OverrideReason.builder()
          .setReasonType(
            SystemUpdate.SystemUpdateState.OverrideReason.OverrideReasonType.EMERGENCY_FIX
          )
          .build()
      val request =
        ReportSystemUpdateAppliedWithOverrideRequest.builder()
          .setName(name)
          .setReason(reason)
          .build()
      client.reportSystemUpdateAppliedWithOverrideFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateDownloadPrevented(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.PreventionReason.builder()
          .setPreventionReasonType(
            SystemUpdate.SystemUpdateState.PreventionReason.PreventionReasonType
              .INSUFFICIENT_STORAGE
          )
          .build()
      val request =
        ReportSystemUpdateDownloadPreventedRequest.builder().setName(name).setReason(reason).build()
      client.reportSystemUpdateDownloadPreventedFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun reportSystemUpdateApplyPrevented(name: String): Result<Unit> =
    try {
      val reason =
        SystemUpdate.SystemUpdateState.PreventionReason.builder()
          .setPreventionReasonType(
            SystemUpdate.SystemUpdateState.PreventionReason.PreventionReasonType
              .INSUFFICIENT_STORAGE
          )
          .build()
      val request =
        ReportSystemUpdateApplyPreventedRequest.builder().setName(name).setReason(reason).build()
      client.reportSystemUpdateApplyPreventedFuture(request).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }

  override suspend fun updateUpdateControlState(
    controlState: UpdateControlState.ControlState
  ): Result<UpdateControlState> =
    try {
      val request =
        UpdateUpdateControlStateRequest.builder()
          .setUpdateControlState(UpdateControlState.builder().setControlState(controlState).build())
          .build()
      val response = client.updateUpdateControlStateFuture(request).await()
      Result.success(response)
    } catch (e: Exception) {
      Result.failure(e)
    }
}
