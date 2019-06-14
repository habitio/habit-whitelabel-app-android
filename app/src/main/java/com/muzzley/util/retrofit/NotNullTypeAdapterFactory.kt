package com.muzzley.util.retrofit

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
//import kotlinx.reflect.lite.ReflectionLite
import timber.log.Timber
import java.io.IOException


// field reflection nullability not working in
// org.jetbrains.kotlinx:kotlinx.reflect.lite:1.0.0
// FIXME: revisit this later
object NotNullTypeAdapterFactory: TypeAdapterFactory {
    override fun <T : Any> create(gson: Gson, type: TypeToken<T>?): TypeAdapter<T> {
        val delegate = gson.getDelegateAdapter(this, type)
        return object: TypeAdapter<T>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: T) {
                delegate.write(out, value)
            }

            @Throws(IOException::class)
            override fun read(jsonReader: JsonReader): T? {
                val x = delegate.read(jsonReader)
//                x?.let {
//                    val klass = it::class.java
//                    ReflectionLite.loadClassMetadata(klass)?.run {
//                        Timber.e("found kotlin class  ${klass.canonicalName}")
//                        klass.methods.forEach { method ->
//                            val functionMetadata = getFunction(method)
//                            functionMetadata?.parameters?.forEach {
//                                Timber.e("method ${method.name} ${it.name} ${it.type.isNullable}")
//                            }
//                        }
//                        klass.declaredFields.forEach {
//                            Timber.e("found field ${it.name}")
//                        }
//                    }
//
//                    val method = klass.methodByName("returnType")
//                    val methodMetadata = classMetadata.getFunction(method) ?: error("No function metadata found for $method")
//                    val returnType = methodMetadata.returnType

//                }
                return x
            }

        }
    }

}