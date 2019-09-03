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
enum class Severity (val value: Int){
    /**
     * Severity off
     */
    Off(0),
    /**
     * Error severity
     */
    Error(6),
    /**
     * Warning severity
     */
    Warning(5),
    /**
     * Info severity
     */
    Info(4),
    /**
     * Debug severity
     */
    Debug(3),
    /**
     * Verbose severity
     */
    Verbose(2)
}