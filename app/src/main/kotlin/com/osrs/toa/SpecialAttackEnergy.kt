package com.osrs.toa

import kotlin.math.min

class SpecialAttackEnergy(
    energy: Int = 100
) {
    init {
        require(energy >= 0) { "Special attack energy cannot be negative" }
        require(energy <= 100) { "Special attack energy cannot exceed 100" }
    }
    
    var energy: Int = energy
        get() = field
        private set(value) { 
            field = value
        }
    
    fun consume(amount: Int): SpecialAttackEnergy {
        require(amount >= 0) { "Consumption amount cannot be negative" }
        require(amount <= energy) { "Cannot consume more energy than available" }
        energy -= amount
        return this
    }
    
    fun regenerate(amount: Int = 10): SpecialAttackEnergy {
        require(amount >= 0) { "Regeneration amount cannot be negative" }
        energy = min(100, energy + amount)
        return this
    }

    fun isRegenerating(): Boolean {
        return energy < 100
    }
    
    fun canUseSpecial(cost: Int): Boolean {
        println("Trying to spec, current energy: $energy")
        return energy >= cost
    }
} 