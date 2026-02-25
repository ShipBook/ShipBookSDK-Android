package io.shipbook.shipbooksdk

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/*
 *
 * Created by Elisha Sterngold on 25/02/2026.
 * Copyright © 2026 ShipBook Ltd. All rights reserved.
 *
 */

internal sealed class SessionEvent {
    object Connected : SessionEvent()
    object UserChange : SessionEvent()
}

internal object InternalEventBus {
    private val _configChange = MutableSharedFlow<Unit>(replay = 1)
    val configChange = _configChange.asSharedFlow()

    private val _sessionEvent = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 1)
    val sessionEvent = _sessionEvent.asSharedFlow()

    suspend fun emitConfigChange() { _configChange.emit(Unit) }
    suspend fun emitSessionEvent(event: SessionEvent) { _sessionEvent.emit(event) }
}
