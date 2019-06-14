package com.muzzley.model.realtime

import com.muzzley.model.channels.Address

data class RealtimeMessage(val address: Address , val payload: Payload) {


    enum class IO {
        r, w, iw, ir
    }

    fun isInfo() = payload.io == IO.iw || payload.io == IO.ir

    data class Payload (var io: IO? , var data: Any?)

    companion object {
        @JvmStatic
        @JvmOverloads
        fun read(addr: Address, data: Any? = null) =
                RealtimeMessage(addr,Payload(IO.r,data))


        @JvmStatic
        fun write(addr: Address, data: Any?) =
                RealtimeMessage(addr,Payload(IO.w,data))
    }
}
