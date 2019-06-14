package com.muzzley.app.recipes

import android.os.Looper
import com.muzzley.model.profiles.RecipeState
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

sealed class Action {
    class GetMeta(val recipeState: RecipeState) : Action()
    class OnStep(val recipeState: RecipeState) : Action()
    class QueryAction(val recipeState: RecipeState) : Action()
    class OnResult(val recipeState: RecipeState) : Action()
    class OnRequest(val recipeState: RecipeState) : Action()
    class SendRequest(val recipeState: RecipeState) : Action()
    class OnError(val throwable: Throwable, val action: Action?) : Action() // null means unrecoverable error
    class Finish(val recipeState: RecipeState) : Action()
}

sealed class State {

    object Idle : State()
    class FetchingMeta(val action: Action.GetMeta) : State()
    class Step(val action: Action.OnStep) : State()
    class FetchingResult(val action: Action.QueryAction) : State()
    class Result(val action: Action.OnResult) : State()
    class Request(val action: Action.OnRequest) : State()
    class SendingRequest(val action: Action.SendRequest) : State()
    class Finished(val action: Action.Finish) : State()
    class Error(val throwable: Throwable, val action: Action?) : State()
}

object StateMachineFactory {
//    private val _stateMachineStore = StateMachineStore(State.Idle)
//    val stateMachineStore
//        get() = _stateMachineStore
    @Deprecated("should not be a singleton")
    var stateMachineStore = StateMachineStore(State.Idle)

}


class StateMachineStore(initialState: State) {

    init{
        Timber.e("StateMachineStore being created")
    }
    private val actions = PublishSubject.create<Action>()

    private val xxx = BehaviorSubject.create<State>()

    private val reducer =
//            actions.scan(initialState, ::reducer).share()
//            actions.scan(initialState, ::reducer).replay(1).refCount()
    actions.scan(initialState, ::reducer).subscribe(xxx)

    private fun reducer(state: State, action: Action): State {
        fun log(): State {
            Timber.d("Unexpected combination ${state::class.java.simpleName}, ${action::class.java.simpleName}")
            return state
        }

        val newState = when (state) {
            is State.Idle ->
                when (action) {
                    is Action.GetMeta -> State.FetchingMeta(action)
                    else -> log()
                }
            is State.FetchingMeta ->
                when (action) {
                    is Action.OnStep -> State.Step(action)
                    is Action.OnError -> State.Error(action.throwable, action.action)
                    else -> log()
                }
            is State.Step ->
                when (action) {
                    is Action.QueryAction -> State.FetchingResult(action)
                    is Action.OnResult -> State.Result(action)
                    else -> log()
                }
            is State.FetchingResult ->
                when (action) {
                    is Action.OnResult -> State.Result(action)
                    is Action.OnError -> State.Error(action.throwable, action.action)
                    else -> log()
                }
            is State.Result ->
                when (action) {
                    is Action.OnRequest -> State.Request(action)
                    is Action.Finish -> State.Finished(action)
                    is Action.OnError -> State.Error(action.throwable, action.action) // could have unknown mandatory variables
                    else -> log()
                }
            is State.Request ->
                when (action) {
                    is Action.SendRequest -> State.SendingRequest(action)
                    else -> log()
                }
            is State.SendingRequest ->
                when (action) {
                    is Action.OnStep -> State.Step(action)
                    is Action.OnError -> State.Error(action.throwable, action.action) // could have unknown mandatory variables
                    else -> log()
                }
            is State.Finished -> state
            is State.Error ->
                when (action) {
                    is Action.GetMeta -> State.FetchingMeta(action)
                    is Action.QueryAction -> State.FetchingResult(action)
                    //TODO: error recovery for other actions
                    else -> log()
                }
        }
        val st = Thread.currentThread().stackTrace
//        Timber.d("${st.getOrNull(3)}")
        val stackTraceElement = st.getOrNull(11)
        Timber.d("${state::class.java.simpleName} + ${action::class.java.simpleName} = ${newState::class.java.simpleName} from(${stackTraceElement?.fileName}:${stackTraceElement?.lineNumber}) ; ${stackTraceElement?.className}")
        return newState
    }

    fun onAction(action: Action) {
        val st = Thread.currentThread().stackTrace
        Timber.d("${action::class.java.simpleName} from ${st.getOrNull(3)}")
        if (Looper.myLooper() == Looper.getMainLooper())
            actions.onNext(action)
        else
            Timber.e("Not on main thread. Ignoring action: ${action::class.java.simpleName}, ${Thread.currentThread().stackTrace}")
    }

//    fun listenState() = reducer
    fun listenState() = xxx

}
