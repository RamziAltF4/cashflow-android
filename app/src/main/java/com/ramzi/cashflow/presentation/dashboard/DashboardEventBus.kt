package com.ramzi.cashflow.presentation.dashboard

import com.ramzi.cashflow.domain.model.TxType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DashboardEventBus {

    sealed interface Event {
        data class OpenAddTransaction(val type: TxType) : Event
        data class SwitchTab(val tab: DashboardTab) : Event
    }

    private val _events = MutableSharedFlow<Event>(
        replay = 0,
        extraBufferCapacity = 8
    )
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun send(event: Event) {
        _events.tryEmit(event)
    }
}