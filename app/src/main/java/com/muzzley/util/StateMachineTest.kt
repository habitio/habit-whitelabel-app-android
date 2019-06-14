package com.muzzley.util

import io.reactivex.subjects.PublishSubject

/*
Just a scrap for future implementation ideas
Inspired by Redux
 */
class StateMachineTest(initialState: State) {
    private val actions = PublishSubject.create<Action>()


    private val reducer =
            actions.scan(initialState,::reducer).share()

    private fun reducer(state: State, action: Action) =
            when(state) {
                is State.A -> if (action == Action.A1) // this could be a call to AReducer
                    State.B
                else
                    state
                is State.B -> when(action) {
                    is Action.A2 -> State.C(true)
                    else -> state
                }
                is State.C -> when(action) {
                    is Action.A3 -> if (action.i > 0) State.A else State.B
                    else -> state
                }

            }

    fun onAction(action: Action) =
            actions.onNext(action)

    fun listenState() = reducer

}

 fun redux1(state: State, action: Action): State =
         when (state) {
             State.A -> when (action) {
                 Action.A1 -> State.B
                 else -> state
             }
             else -> state
         }
fun redux2(state: State, action: Action): State =
        if (state == State.A && action == Action.A1)
            State.B
        else
            state

interface Reduxer {
    operator fun invoke(state: State, action: Action): State
}


fun test(){
    val stateMachine = StateMachineTest(State.A)
    val subscribe = stateMachine.listenState().filterIsInstance<State.A>().subscribe {
        //do something async
        stateMachine.onAction(Action.A3(10)) // firing new actions is optional
    }

    val subscribe1 = stateMachine.listenState().subscribe {
        when (it) {
            is State.A -> println("A") // does not fire new action
            is State.B -> { println("B") ; stateMachine.onAction(Action.A2)} // fires new state transition
            is State.C -> { println("C")
                // we should not do this because it would propagate errors and terminate de Rx reducer
//                channelService.channelTemplates.map { Action.A3(5) }.subscribe(stateMachine.actions)

            } // fires new state transition
        }
    }

    val reduxer = object : Reduxer {
        override fun invoke(state: State, action: Action): State {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
    val reducers: List<Reduxer> = listOf()

    var state: State = State.A
    for(r in reducers)
        state = r(state,Action.A1)

    state = reducers.fold(State.A as State) { st, red ->
        red(st, Action.A1)
    }


    reduxer(State.A,Action.A1)

}

/*
we could have substates that extend a top level state and have custom reducers that understand those substates
 */

sealed class State {
    object A : State()
    object B : State()
    data class C(val b: Boolean) : State()
}

sealed class Action {
    object A1 : Action()
    object A2 : Action()
    data class A3(val i: Int): Action()
}