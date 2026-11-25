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

import android.util.Log
import com.google.android.managementapi.policy.model.Policy
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses a JSON representation of [Policy] into the SDK model.
 *
 * JSON keys (camelCase or snake_case) are mapped onto the setters of the [Policy] builder and its
 * nested builders. Keys without a matching setter in the linked SDK are logged and ignored.
 */
object PolicyJsonParser {
  private const val TAG = "PolicyJsonParser"

  private val methodsCache =
    java.util.concurrent.ConcurrentHashMap<Class<*>, Map<String, java.lang.reflect.Method>>()

  fun parsePolicyFromJson(jsonStr: String): Policy {
    val json = JSONObject(jsonStr)
    val builder = Policy.builder()
    populateBuilder(builder, Policy::class.java, json)
    return builder.build()
  }

  private fun populateBuilder(builder: Any, targetClass: Class<*>, json: JSONObject) {
    val builderClass = builder.javaClass
    val methodsMap =
      methodsCache.getOrPut(builderClass) {
        builderClass.methods.filter { it.parameterCount == 1 }.associateBy { it.name }
      }

    for (key in json.keys()) {
      val value = json.get(key)
      if (value == JSONObject.NULL) continue

      val pascalCaseKey = snakeToPascalCase(key)
      val expectedSetterName = "set" + pascalCaseKey

      val setter = methodsMap[expectedSetterName]
      if (setter == null) {
        Log.w(TAG, "Ignoring unknown key '$key' for ${targetClass.simpleName}")
        continue
      }
      val paramType = setter.parameterTypes[0]
      val genericParamType = setter.genericParameterTypes[0]

      val parsedValue = paramType.parseValueFrom(value, genericParamType)
      if (parsedValue != null) {
        setter.isAccessible = true
        setter.invoke(builder, parsedValue)
      }
    }
  }

  private fun Class<*>.parseValueFrom(value: Any, genericType: Type): Any? {
    if (value == JSONObject.NULL) return null

    return when {
      this == String::class.java -> value.toString()
      this == Long::class.javaObjectType || this == Long::class.javaPrimitiveType ->
        (value as? Number)?.toLong() ?: value.toString().toLongOrNull()
      this == Int::class.javaObjectType || this == Int::class.javaPrimitiveType ->
        (value as? Number)?.toInt() ?: value.toString().toIntOrNull()
      this == Boolean::class.javaObjectType || this == Boolean::class.javaPrimitiveType ->
        value as? Boolean ?: value.toString().toBoolean()
      this == Double::class.javaObjectType || this == Double::class.javaPrimitiveType ->
        (value as? Number)?.toDouble() ?: value.toString().toDoubleOrNull()
      this == LocalTime::class.java -> if (value is JSONObject) parseLocalTime(value) else null
      this == LocalDate::class.java ->
        if (value is JSONObject) parseLocalDate(value)
        else if (value is String) parseLocalDateString(value) else null
      this == Duration::class.java -> if (value is String) parseDuration(value) else null
      this.isEnum && value is String -> {
        val valueOfMethod = this.getMethod("valueOf", String::class.java)
        valueOfMethod.isAccessible = true
        valueOfMethod.invoke(null, value)
      }
      Iterable::class.java.isAssignableFrom(this) && value is JSONArray -> {
        val list = mutableListOf<Any>()
        val listElementType = (genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
        for (i in 0 until value.length()) {
          val elementVal = value.get(i)
          val parsedElement = listElementType.parseValueFrom(elementVal, listElementType)
          if (parsedElement != null) {
            list.add(parsedElement)
          }
        }
        list
      }
      value is JSONObject -> {
        try {
          val builderMethod = this.getMethod("builder")
          val builder = builderMethod.invoke(null) ?: return null
          populateBuilder(builder, this, value)
          val buildMethod = builder.javaClass.getMethod("build")
          buildMethod.isAccessible = true
          buildMethod.invoke(builder)
        } catch (e: NoSuchMethodException) {
          null
        }
      }
      else -> null
    }
  }

  private fun snakeToPascalCase(str: String): String =
    str.split("_").joinToString("") { it.replaceFirstChar { it.uppercase() } }

  private fun parseLocalTime(json: JSONObject): LocalTime {
    val hours = json.optInt("hours", 0)
    val minutes = json.optInt("minutes", 0)
    val seconds = json.optInt("seconds", 0)
    val nanos = json.optInt("nanos", 0)
    return LocalTime.of(hours, minutes, seconds, nanos)
  }

  private fun parseLocalDate(json: JSONObject): LocalDate {
    val year = json.optInt("year", 0)
    val month = json.optInt("month", 1)
    val day = json.optInt("day", 1)
    return LocalDate.of(year, month, day)
  }

  private fun parseLocalDateString(str: String): LocalDate {
    val parts = str.split("-")
    return if (parts.size == 2) {
      val month = parts[0].toInt()
      val day = parts[1].toInt()
      LocalDate.of(0, month, day)
    } else {
      val year = parts[0].toInt()
      val month = parts[1].toInt()
      val day = parts[2].toInt()
      LocalDate.of(year, month, day)
    }
  }

  private fun parseDuration(str: String): Duration {
    val cleanStr = str.trim()
    if (cleanStr.endsWith("s")) {
      val secondsStr = cleanStr.substring(0, cleanStr.length - 1)
      val secondsDouble = secondsStr.toDouble()
      val seconds = secondsDouble.toLong()
      val nanos = ((secondsDouble - seconds) * 1_000_000_000).toLong()
      return Duration.ofSeconds(seconds).plusNanos(nanos)
    }
    throw IllegalArgumentException("Invalid duration string: $str")
  }
}
