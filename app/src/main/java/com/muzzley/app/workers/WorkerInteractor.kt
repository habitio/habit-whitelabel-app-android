package com.muzzley.app.workers

import com.muzzley.model.workers.RuleUnitResponse
import com.muzzley.model.workers.Worker

class WorkerInteractor {

    fun buildWorkerToSubmit(userId: String, workerLabel: String?, ruleTrigger: List<RuleUnitResponse>, ruleAction: List<RuleUnitResponse>, ruleState: List<RuleUnitResponse>): Worker =
         Worker().apply {
            user = userId
            label = workerLabel ?: ""
            triggers = ruleTrigger.flatMap { it.ruleUnit.rules }
            actions = ruleAction.flatMap { it.ruleUnit.rules }
            states = ruleState.flatMap { it.ruleUnit.rules }
            execute = null
            enabled = true
        }
}