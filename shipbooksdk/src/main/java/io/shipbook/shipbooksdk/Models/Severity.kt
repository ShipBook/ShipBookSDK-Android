package io.shipbook.shipbooksdk.Models

/*
 *
 * Created by Elisha Sterngold on 11/02/2018.
 * Copyright © 2018 ShipBook Ltd. All rights reserved.
 *
 */

/**
 * enum of severity
 */
enum class Severity (val value: Int, val identifier: String) {
    /**
     * Severity off
     */
    Off(0, "Off"),
    /**
     * Error severity
     */
    Error(6, "Error"),
    /**
     * Warning severity
     */
    Warning(5, "Warning"),
    /**
     * Info severity
     */
    Info(4, "Info"),
    /**
     * Debug severity
     */
    Debug(3, "Debug"),
    /**
     * Verbose severity
     */
    Verbose(2, "Verbose");

    companion object {
        private val map = values().associateBy(Severity::value)
        private val identifierMap = values().associateBy(Severity::identifier)
        fun fromInt(type: Int) = map[type] ?: Verbose
        fun fromIdentifier(id: String) = identifierMap[id] ?: Verbose // don't use valueOf because gets broken in obfuscation
    }
}
