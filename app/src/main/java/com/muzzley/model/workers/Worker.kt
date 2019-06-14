package com.muzzley.model.workers

import com.muzzley.app.workers.WorkerExecutionState

class Worker {

    var id: String? = null
    var user: String? = null
    var lastRun: String? = null
    var label: String? = null

    var triggers: List<WorkerUnit> = listOf()
    var actions: List<WorkerUnit> = listOf()
    var states: List<WorkerUnit> = listOf()
    var unsorted: List<WorkerUnit> = listOf()

    var execute: List<WorkerExecuteMessage>? = listOf()
    var enabled: Boolean = true
    var forceDisabled: Boolean = false
    var forceDisabledMessage: String? = null
    var allowdisable: Boolean = true
    var editable: Boolean = true
    var deletable: Boolean = true
    var invalid: Boolean = false
    var description: String? = null
    var category: String? = null
    var categoryColor: String? = null
    var devicesText: String? = null
    var clientImage: String? = null
    var categoryImage: String? = null

    @Transient var fence: Fence? = null
    @Transient var subtitle: String? = null
    @Transient var missingPermissions = setOf<String>()
    @Transient var missingCapabilities = setOf<String>()
    @Transient var requiredCapabilities = setOf<String>()
    @Transient var locationDisabled: Boolean = false
    @Transient var notificationsDisabled: Boolean = false
    @Transient var executionState: WorkerExecutionState = WorkerExecutionState.idle

}
