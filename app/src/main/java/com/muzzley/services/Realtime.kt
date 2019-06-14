package com.muzzley.services

import com.muzzley.model.grants.GrantMessage
import com.muzzley.model.realtime.RealtimeMessage
import io.reactivex.Observable

interface Realtime {
    fun isConnected(): Boolean

    fun connect(user: String, password: String, host: String): Observable<Boolean>

    fun disconnect(): Observable<Boolean>

    fun subscribe(topic: String): Observable<Boolean>

    fun unsubscribe(topic: String): Observable<Boolean>

    fun send(msg: RealtimeMessage): Observable<Boolean>

    fun send(msg: GrantMessage): Observable<Boolean>

    fun listenToRTM(): Observable<RealtimeMessage>

    fun listenToGrants(): Observable<GrantMessage>

    class Exception(message: String, cause: Throwable?) : RuntimeException(message,cause)
}
