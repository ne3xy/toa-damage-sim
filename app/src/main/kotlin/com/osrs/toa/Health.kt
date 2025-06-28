package com.osrs.toa

import kotlin.math.max

class Health(
        health: Int
) {
    init {
        require(health >= 0) { "Current HP cannot be negative" }
    }
    var value: Int = health
        private set

    fun takeDamage(damage: Int): Health {
        require(damage >= 0) { "Damage cannot be negative" }
        value = max(0, value - damage)
        return this
    }
//    fun heal(amount: Int): Health {
//        require(amount >= 0) { "Heal amount cannot be negative" }
//        val maxHealth = 99 + amount
//        value = min(maxHealth, value + amount)
//        return this
//    }
} 