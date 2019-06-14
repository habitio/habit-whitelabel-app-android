package com.muzzley.model.discovery

@Deprecated("NStep")
class NullStep private constructor() : Step(null, 0, null, null, null) {

    fun asBoolean(): Boolean {
        return false
    }

    companion object {

        @JvmStatic
        val instance: NullStep
            get() = NullStep()
    }

}
