package com.muzzley.model.channels

class WorkerParams (
    val id: String,
    val label: String,
    val inputsLabel: String,
    val inputs: List<Input>,
    // for triggers and states
    val condition: String? = null,
    val predicateLabel: String? = null
)
class Input (val id: String, val controlInterfaceId: String, val path: List<Path>)
class Path (val source: String, val target: String)
