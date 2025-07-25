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
package com.amapi.extensibility.demo.data.provider

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.amapi.extensibility.demo.R
import java.io.File

object CustomAppProvider : FileProvider(R.xml.file_provider_paths) {
  /**
   * Returns the content URI of the custom app with the given package name.
   *
   * @param context the context of the application
   * @param packageName the package name of the custom app
   * @return the content URI of the custom app
   */
  fun getContentUriOfApp(context: Context, packageName: String): Uri {
    val filesPath = File(context.filesDir, context.getString(R.string.files_directory_name))
    val file = File(filesPath, "${packageName}.apk")
    return getUriForFile(context, context.getString(R.string.file_provider_authority), file)
  }

  /**
   * Sends an intent to CloudDPC to provide the URI of the custom app with the given filename.
   *
   * @param context the context of the application
   * @param filename the filename of the custom app
   */
  fun provideUriIntentToDeviceManager(context: Context, filename: String) {
    fun getString(resId: Int) = context.resources.getString(resId)

    val filesPath = File(context.filesDir, getString(R.string.files_directory_name))
    val file = File(filesPath, filename)
    val contentUri: Uri = getUriForFile(context, getString(R.string.file_provider_authority), file)
    val intent =
      Intent().apply {
        action = getString(R.string.clouddpc_provide_uri_action)
        data = contentUri
        clipData = ClipData.newRawUri("", contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        component =
          ComponentName(
            getString(R.string.clouddpc_package_name),
            getString(R.string.clouddpc_receiver_class_name),
          )
      }
    context.sendBroadcast(intent)
  }

  /**
   * Grants permission to CloudDPC to access the content URI of the provided file.
   *
   * @param context the context of the application
   * @param contentUri the content URI of the custom app
   */
  fun grantUriPermissionToDeviceManager(context: Context, contentUri: Uri) {
    context.grantUriPermission(
      context.getString(R.string.clouddpc_package_name),
      contentUri,
      Intent.FLAG_GRANT_READ_URI_PERMISSION,
    )
  }
}
