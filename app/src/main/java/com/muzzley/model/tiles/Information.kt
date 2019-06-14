package com.muzzley.model.tiles

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

class Information(
// icon-color, text-expression, text-unit, text?
    val id: String?,
    val type: String?,
    val componentType: String?,
    val property: String,
    val options: Options?,
    @Transient
    val lastValue: Subject<String>
)
{
//    private constructor(): this(null,null,null,"fixme stupid gson",null,BehaviorSubject.create<String>() )
    private constructor(): this(null,null,null,"fixme stupid gson",null,BehaviorSubject.createDefault("--" ))

    //FIXME: backend should send the following from service
    var componentId: String? = null
    var label: String? = null
    @Transient
    var unit: String? = null
//    @Transient
//    val lastValue: Subject<String> by lazy { BehaviorSubject.create<String>() }

}
