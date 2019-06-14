package com.muzzley.model.discovery

import java.util.ArrayList

/**
 * Created by ruigoncalo on 08/07/14.
 */
open class Step(var context: String?, var step: Int, var title: String?, var resultUrl: String?, var actions: List<Action>? = ArrayList())
