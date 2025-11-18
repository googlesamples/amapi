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
package com.amapi.extensibility.demo.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.amapi.extensibility.demo.data.provider.CustomAppProvider
import com.amapi.extensibility.demo.util.CommandUtils.parseForPrettyPrint
import com.google.android.managementapi.commands.LocalCommandClient
import com.google.android.managementapi.commands.LocalCommandClientFactory
import com.google.android.managementapi.commands.model.IssueCommandRequest
import java.io.File
import java.lang.Exception
import java.net.URL
import java.nio.file.Paths
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext

/** Repository for Custom App (Package Installer) related operations. */
interface CustomAppRepository {
  /**
   * Downloads the app with the [packageName] from the given [downloadUrl] to the internal storage.
   */
  suspend fun downloadAppToInternalStorage(packageName: String, downloadUrl: String): Result<Unit>

  /** Grants Android Device Policy file permission of the apk file of the provided [packageName]. */
  fun grantFilePermissionToDeviceManager(packageName: String)

  /**
   * Sends an INSTALL_APP command to Android Device Policy to install the app with the provided
   * [packageName].
   *
   * @param packageName The package name of the app to be installed.
   * @param handleFileProvider Indicates whether to handle the creation of a content URI within the
   *   app before sending the command to AMAPI SDK. If true, a content URI will be created using
   *   [CustomAppProvider]. If false, a file URI will be constructed directly from the file system
   *   path.
   * @return A [Result] object indicating the success or failure of the operation.
   * @throws IllegalStateException If `handleFileProvider` is true but the required URI permissions
   *   have not been granted to Android Device Policy.
   */
  suspend fun sendInstallAppCommandToDeviceManager(
    packageName: String,
    handleFileProvider: Boolean,
  ): Result<Unit>

  /** Gets the list of paths of all downloaded files. */
  fun getDownloadedFilesPaths(): List<String>

  /** Deletes all existing downloaded files. */
  fun resetDownloadedFiles()

  /**
   * Sends an UNINSTALL_APP command to Android Device Policy to uninstall the app with the provided
   * [packageName].
   */
  suspend fun sendUninstallAppCommandToDeviceManager(packageName: String): Result<Unit>
}

class CustomAppRepositoryImpl(
  private val context: Context,
  coroutineDispatcher: CoroutineDispatcher,
) : CustomAppRepository {

  // A dedicated dispatcher for I/O
  private val ioDispatcher: CoroutineDispatcher = coroutineDispatcher
  private val packageNameToUri = mutableMapOf<String, Uri>()
  private val localCommandClient: LocalCommandClient by lazy {
    LocalCommandClientFactory.create(context)
  }

  private val customAppsDir: File
    get() {
      val client = localCommandClient
      val installCustomAppCommandHelper = client.getInstallCustomAppCommandHelper()
      val customAppsDir = installCustomAppCommandHelper.customApksStorageDirectory
      customAppsDir.mkdirs()
      return customAppsDir
    }

  override suspend fun downloadAppToInternalStorage(
    packageName: String,
    downloadUrl: String,
  ): Result<Unit> {
    try {
      withContext(ioDispatcher) {
        val url = URL(downloadUrl)
        val connection = url.openConnection()
        connection.connect()

        val file = File(customAppsDir, "${packageName}.apk")
        val fileOutputStream = file.outputStream()

        val connectionInputStream = connection.inputStream

        val buffer = ByteArray(BYTE_SIZE)
        var bytesRead = connectionInputStream.read(buffer)
        while (bytesRead != -1) {
          fileOutputStream.write(buffer, 0, bytesRead)
          bytesRead = connectionInputStream.read(buffer)
        }

        fileOutputStream.close()
        connectionInputStream.close()
      }
    } catch (exception: Exception) {
      return Result.failure(exception)
    }
    return Result.success(Unit)
  }

  override fun getDownloadedFilesPaths(): List<String> {
    val files: Array<out File>? = customAppsDir.listFiles()
    return files?.map { file -> file.absolutePath } ?: listOf()
  }

  override fun resetDownloadedFiles() {
    customAppsDir.listFiles()?.forEach { it.delete() }
    packageNameToUri.clear()
  }

  override fun grantFilePermissionToDeviceManager(packageName: String) {
    val uri = CustomAppProvider.getContentUriOfApp(context, packageName)
    CustomAppProvider.grantUriPermissionToDeviceManager(context, uri)
    packageNameToUri[packageName] = uri
  }

  override suspend fun sendInstallAppCommandToDeviceManager(
    packageName: String,
    handleFileProvider: Boolean,
  ): Result<Unit> {
    if (handleFileProvider && !packageNameToUri.containsKey(packageName)) {
      return Result.failure(
        IllegalStateException("Invalid state, Android Device Policy should be granted file permissions first.")
      )
    }

    val packageUri =
      when (handleFileProvider) {
        true -> packageNameToUri[packageName].toString()
        false -> {
          val file = File(customAppsDir, "${packageName}.apk")

          file.toUri().toString()
        }
      }

    Log.i(TAG, "Package uri: $packageUri")

    val command =
      try {
        withContext(ioDispatcher) {
          LocalCommandClientFactory.create(context = context)
            .issueCommand(
              IssueCommandRequest.builder()
                .setInstallCustomApp(
                  IssueCommandRequest.InstallCustomApp.builder()
                    .setPackageName(packageName)
                    .setPackageUri(packageUri)
                    .build()
                )
                .build()
            )
            .await()
        }
      } catch (exception: Exception) {
        Log.e(TAG, "onFailure", exception)
        return Result.failure(exception)
      }

    val parsedCommandString = command.parseForPrettyPrint()
    Log.i(TAG, "Successfully issued command: $parsedCommandString")
    return Result.success(Unit)
  }

  override suspend fun sendUninstallAppCommandToDeviceManager(packageName: String): Result<Unit> {
    val command =
      try {
        withContext(ioDispatcher) {
          LocalCommandClientFactory.create(context = context)
            .issueCommand(
              IssueCommandRequest.builder()
                .setUninstallCustomApp(
                  IssueCommandRequest.UninstallCustomApp.builder()
                    .setPackageName(packageName)
                    .build()
                )
                .build()
            )
            .await()
        }
      } catch (exception: Exception) {
        Log.e(TAG, "onFailure", exception)
        return Result.failure(exception)
      }

    val parsedCommandString = command.parseForPrettyPrint()
    Log.i(TAG, "Successfully issued command: $parsedCommandString")
    return Result.success(Unit)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun getContentUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.$AUTHORITY_SUFFIX"

    val cacheDir =
      Paths.get(
          context.cacheDir.absolutePath,
          MANAGEMENT_API_DIR_NAME,
          CUSTOM_APP_DIR_NAME,
          APKS_DIR_NAME,
        )
        .toFile()
    if (!cacheDir.exists()) {
      cacheDir.mkdirs()
    }
    val cachedFile = File(cacheDir, file.name)
    file.copyTo(cachedFile, overwrite = true)

    Log.i(TAG, "Cached file path: $cachedFile")
    return FileProvider.getUriForFile(context, authority, cachedFile)
  }

  private companion object {
    const val TAG = "CustomAppRepository"
    const val AUTHORITY_SUFFIX = "AmapiCustomAppProvider"
    const val BYTE_SIZE = 1024
    const val MANAGEMENT_API_DIR_NAME = "com.google.android.managementapi"
    const val CUSTOM_APP_DIR_NAME = "customapp"
    const val APKS_DIR_NAME = "apks"
  }
}
