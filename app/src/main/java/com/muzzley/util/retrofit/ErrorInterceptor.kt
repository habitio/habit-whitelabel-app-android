package com.muzzley.util.retrofit

import com.muzzley.util.RepositoryException
import com.muzzley.util.parseJson
import okhttp3.Interceptor
import okhttp3.Response


class ErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        if (response.code() >= 400) {
            throwError(response)
            return response
        } else {
            return response
        }
    }

    private fun throwError(response: Response) {
        val code =
                try {
                    (response.header("X-Error")
                            ?: response.body()?.string().parseJson<Map<*, *>>()?.get("code") as? String)!!.toInt()
                } catch (t: Throwable) {
                    0
                }
        throw RepositoryException(null,code)
    }
}