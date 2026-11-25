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
package com.amapi.extensibility.demo.policy

import com.google.android.managementapi.policy.PolicyClient
import com.google.android.managementapi.policy.model.GetPolicyRequest
import com.google.android.managementapi.policy.model.Policy
import com.google.android.managementapi.policy.model.UpdatePolicyRequest
import kotlin.coroutines.cancellation.CancellationException

/** Wraps [PolicyClient] calls and converts failures into [Result]. */
class PolicyRepository(private val client: PolicyClient) {

  suspend fun getPolicy(): Result<Policy> = runSdkCall { client.getPolicy(getPolicyRequest()) }

  /**
   * Reads the current policy and writes it back unchanged (read-then-write-back).
   *
   * [UpdatePolicyRequest] replaces the whole policy, so round-tripping the current one avoids
   * clearing the enforced policy on a managed device.
   */
  suspend fun updatePolicy(): Result<Policy> = runSdkCall {
    val current = client.getPolicy(getPolicyRequest())
    client.updatePolicy(updatePolicyRequest(current))
  }

  /** Replaces the enforced policy with [policy]. */
  suspend fun updatePolicy(policy: Policy): Result<Policy> = runSdkCall {
    client.updatePolicy(updatePolicyRequest(policy))
  }

  private fun getPolicyRequest(): GetPolicyRequest = GetPolicyRequest.getDefaultInstance()

  private fun updatePolicyRequest(policy: Policy): UpdatePolicyRequest =
    UpdatePolicyRequest.builder().setPolicy(policy).build()

  private suspend fun runSdkCall(call: suspend () -> Policy): Result<Policy> =
    try {
      Result.success(call())
    } catch (e: CancellationException) {
      // Keep coroutine cancellation cooperative.
      throw e
    } catch (e: Exception) {
      Result.failure(e)
    }
}
