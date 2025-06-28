package com.osrs.toa

// Core tick-based system
data class Tick(val value: Int) {
    operator fun plus(other: Tick): Tick = Tick(value + other.value)
    operator fun minus(other: Tick): Tick = Tick(value - other.value)
    operator fun compareTo(other: Tick): Int = value.compareTo(other.value)
} 