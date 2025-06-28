package com.osrs.toa

import kotlin.math.min

class SpecialAttackEnergy(
    energy: Int = 100
) {
    init {
        require(energy >= 0) { "Special attack energy cannot be negative" }
        require(energy <= 100) { "Special attack energy cannot exceed 100" }
    }
    
    // Internal storage uses 0-1000 range for precision
    private var internalEnergy: Int = energy * 10
    
    val energy: Int
        get() = internalEnergy / 10
    
    fun consume(amount: Int): SpecialAttackEnergy {
        require(amount >= 0) { "Consumption amount cannot be negative" }
        require(amount * 10 <= internalEnergy) { "Cannot consume more energy than available" }
        internalEnergy -= amount * 10
        return this
    }
    
    fun consume(amount: Double): SpecialAttackEnergy {
        require(amount >= 0.0) { "Consumption amount cannot be negative" }
        val internalAmount = (amount * 10).toInt()
        require(internalAmount <= internalEnergy) { "Cannot consume more energy than available" }
        internalEnergy -= internalAmount
        return this
    }
    
    fun regenerate(amount: Int = 10): SpecialAttackEnergy {
        require(amount >= 0) { "Regeneration amount cannot be negative" }
        internalEnergy = min(1000, internalEnergy + (amount * 10))
        return this
    }

    fun isRegenerating(): Boolean {
        return energy < 100
    }
    
    fun canUseSpecial(cost: Int): Boolean {
        println("Trying to spec, current energy: $energy")
        return internalEnergy >= cost * 10
    }
    
    fun canUseSpecial(cost: Double): Boolean {
        println("Trying to spec, current energy: $energy")
        return internalEnergy >= (cost * 10).toInt()
    }
} 