package com.example.librarytest.utils

import android.content.Context
import com.example.librarytest.data.TreeNode
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import java.io.InputStreamReader
import java.lang.reflect.Type

object FileUtils {

    fun loadJsonFromAssets(context: Context, fileName: String, bookName: String): List<TreeNode> {
        val inputStream = context.assets.open(fileName)
        val reader = InputStreamReader(inputStream)

        val typeAdapterFactory: TypeAdapterFactory = RuntimeTypeAdapterFactory
            .of(TreeNode::class.java, "type")
            .registerSubtype(TreeNode.Entry::class.java, "Entry")
            .registerSubtype(TreeNode.Path::class.java, "Path")
            .registerSubtype(TreeNode.Dest::class.java, "Dest")

        val gson = GsonBuilder()
            .registerTypeAdapterFactory(typeAdapterFactory)
            .registerTypeAdapter(Quaternion::class.java, QuaternionAdapter())
            .registerTypeAdapter(Float3::class.java, Float3Adapter())
            .create()

        // JSON 전체 데이터 파싱
        val jsonObject = JsonParser.parseReader(reader).asJsonObject

        // 특정 책(Book)에 해당하는 데이터만 파싱
        val bookArray = jsonObject.getAsJsonArray(bookName) ?: throw IllegalArgumentException("Book not found: $bookName")
        val listType = object : TypeToken<List<TreeNode>>() {}.type

        return gson.fromJson(bookArray, listType)
    }

    class QuaternionAdapter : JsonDeserializer<Quaternion>, JsonSerializer<Quaternion> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext?
        ): Quaternion {
            val array = json.asJsonArray
            return Quaternion(array[0].asFloat, array[1].asFloat, array[2].asFloat, array[3].asFloat)
        }

        override fun serialize(src: Quaternion, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonArray().apply {
                add(src.x)
                add(src.y)
                add(src.z)
                add(src.w)
            }
        }
    }

    class Float3Adapter : JsonDeserializer<Float3>, JsonSerializer<Float3> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Float3 {
            val array = json.asJsonArray
            return Float3(array[0].asFloat, array[1].asFloat, array[2].asFloat)
        }

        override fun serialize(src: Float3, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonArray().apply {
                add(src.x)
                add(src.y)
                add(src.z)
            }
        }
    }
}