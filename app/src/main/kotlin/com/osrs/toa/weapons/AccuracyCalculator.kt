package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatStats
import kotlin.math.max

object AccuracyCalculator {
    
    /**
     * Calculate hit chance based on attack roll and defence roll
     * 
     * @param attackRoll The attacker's attack roll (hardcoded for now)
     * @param defenceRoll The defender's defence roll calculated from their stats
     * @return Hit chance as a double between 0.0 and 1.0
     */
    fun calculateHitChance(attackRoll: Int, defenceRoll: Int): Double {
        return if (attackRoll > defenceRoll) {
            1.0 - (defenceRoll + 2) / (2.0 * (attackRoll + 1))
        } else {
            attackRoll.toDouble() / (2.0 * (defenceRoll + 1))
        }
    }
    
    /**
     * Determine if an attack hits based on calculated hit chance
     * 
     * @param hitChance The calculated hit chance (0.0 to 1.0)
     * @return True if the attack hits, false otherwise
     */
    fun doesAttackHit(hitChance: Double): Boolean {
        return kotlin.random.Random.nextDouble() <= hitChance
    }
} 