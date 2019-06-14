package com.muzzley.model.stores

import java.util.HashMap

import javax.inject.Inject
import javax.inject.Singleton

class InterfacesStore {

    private val interfacesMap: MutableMap<String, InterfaceData> = HashMap()

    fun addInterface(uuid: String, path: String, etag: String) {
        interfacesMap[uuid] = InterfaceData(path, etag)
    }

    fun containsInterface(uuid: String): Boolean =
            interfacesMap.containsKey(uuid)


    fun getPath(uuid: String): String? =
            interfacesMap[uuid]?.path

    fun isNewEtag(uuid: String, etagNew: String): Boolean =
            interfacesMap[uuid]?.etag != null && interfacesMap[uuid]?.etag != etagNew

}
