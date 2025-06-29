package com.osrs.toa.actors

import com.osrs.toa.Health
import com.osrs.toa.SpecialAttackEnergy
import com.osrs.toa.Tick
import java.math.RoundingMode
import java.math.BigDecimal

/**
 * A specialized combat entity for TOA bosses that combines ToaCombatStats with HP scaling
 */
class ToaCombatEntity(
    override val name: String,
    baseHp: Int,
    invocationLevel: Int,
    pathLevel: Int = 0,
    override val specialAttackEnergy: SpecialAttackEnergy = SpecialAttackEnergy(),
    override val hasLightbearer: Boolean = false,
    override var specRegenStartTick: Tick? = null,
    baseCombatStats: CombatStats
) : CombatEntity {
    
    private val initialHp = calculateScaledHp(baseHp, invocationLevel, pathLevel)
    override val health: Health = Health(initialHp)
    override val combatStats: CombatStats = ToaMonsterCombatStats(baseCombatStats, invocationLevel)
    
    private var lastAttackTick = Tick(0)
    private var lastAttackDelay: Int = 0
    
    val scaledHp: Int
        get() = initialHp
    
    val baseHp: Int = baseHp
    val invocationLevel: Int = invocationLevel
    val pathLevel: Int = pathLevel
    
    init {
        require(invocationLevel >= 0) { "Invocation level must be non-negative" }
        require(invocationLevel % 5 == 0) { "Invocation level must be divisible by 5" }
        require(pathLevel in 0..6) { "Path level must be between 0 and 6" }
    }
    
    override val isAlive: Boolean
        get() = health.value > 0
    
    override fun canAttack(currentTick: Tick): Boolean {
        return currentTick >= lastAttackTick + Tick(lastAttackDelay) && isAlive
    }
    
    override fun regenerateSpecialAttack(): ToaCombatEntity {
        specialAttackEnergy.regenerate()
        return this
    }
    
    override fun takeDamage(damage: Int): ToaCombatEntity {
        require(damage >= 0) { "Damage cannot be negative" }
        health.takeDamage(damage)
        return this
    }
    
    override fun setLastAttackTick(tick: Tick, weaponDelay: Int): ToaCombatEntity {
        require(tick.value >= 0) { "Attack tick cannot be negative" }
        require(weaponDelay >= 0) { "Weapon delay cannot be negative" }
        lastAttackTick = tick
        lastAttackDelay = weaponDelay
        return this
    }
    
    override fun setSpecRegenStartTick(tick: Tick?): ToaCombatEntity {
        if (tick != null) {
            require(tick.value >= 0) { "Regeneration start tick cannot be negative" }
        }
        specRegenStartTick = tick
        return this
    }

    companion object {
        /**
         * Calculates scaled HP for TOA monsters based on invocation level and path level
         */
        fun calculateScaledHp(baseHp: Int, invocationLevel: Int, pathLevel: Int): Int {
            require(invocationLevel >= 0) { "Invocation level must be non-negative" }
            require(invocationLevel % 5 == 0) { "Invocation level must be divisible by 5" }
            require(pathLevel in 0..6) { "Path level must be between 0 and 6" }

            // Apply invocation scaling first and truncate
            val invocationMultiplier = 1.0 + (invocationLevel / 5) * 0.02
            var scaledHp = (baseHp * invocationMultiplier).toInt()

            // Apply path scaling and truncate
            val pathMultiplier = when (pathLevel) {
                0 -> 1.0
                1 -> 1.08
                else -> 1.08 + (pathLevel - 1) * 0.05
            }
            scaledHp = (scaledHp * pathMultiplier).toInt()

            // Round to nearest 10
            return ((scaledHp + 5) / 10) * 10
        }
    }
} 