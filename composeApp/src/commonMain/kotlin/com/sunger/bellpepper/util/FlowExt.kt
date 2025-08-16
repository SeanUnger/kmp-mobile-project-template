package com.sunger.bellpepper.util

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlin.time.Duration


private val UNSET = Any()

/**
 * Allows for mapping from a StateFlow to a StateFlow, as opposed to the Flow.map() operator, which
 * maps from a Flow to a Flow.
 */
fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> {
    return DerivedStateFlow(
        getValue = { transform(value) },
        flow = map { transform(it) }
    )
}

/**
 * Tracks the previously emitted value and emits it along with the newly emitted value to combine().
 */
fun <T, R> Flow<T>.previousValueCombine(
    initial: suspend (T) -> R,
    combine: suspend (previous: T, new: T) -> R
): Flow<R> = flow {
    var previous: Any? = UNSET
    collect { value ->
        if (previous === UNSET) {
            emit(initial(value))
        } else {
            emit(combine(previous as T, value))
        }
        previous = value
    }
}

// see https://github.com/Kotlin/kotlinx.coroutines/issues/2631#issuecomment-870565860
// We have to implement this solution manually until it is supported officially by kotlin Flow
class DerivedStateFlow<T>(
    private val getValue: () -> T,
    private val flow: Flow<T>
) : StateFlow<T> {

    override val replayCache: List<T>
        get() = listOf(value)

    override val value: T
        get() = getValue()

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        coroutineScope {
            flow.distinctUntilChanged().stateIn(this).collect(collector)
        }
    }
}

fun <T> Flow<T>.filterWithin(duration: Duration): Flow<T> {
    var lastEmissionTime = kotlinx.datetime.Instant.DISTANT_PAST
    return filter {
        val now = Clock.System.now()
        if (lastEmissionTime + duration < now) {
            lastEmissionTime = now
            true
        } else {
            false
        }
    }
}
