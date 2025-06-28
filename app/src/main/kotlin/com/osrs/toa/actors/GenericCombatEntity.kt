package com.osrs.toa.actors

import com.osrs.toa.Health
import com.osrs.toa.SpecialAttackEnergy
import com.osrs.toa.Tick

interface CombatEntity {
    val name: String
    val specialAttackEnergy: SpecialAttackEnergy
    val health: Health
    val combatStats: CombatStats
    fun canAttack(currentTick: Tick): Boolean
    val isAlive: Boolean
    fun regenerateSpecialAttack(): CombatEntity
    fun takeDamage(damage: Int): CombatEntity
    fun setLastAttackTick(tick: Tick, weaponDelay: Int): CombatEntity
    fun setSpecRegenStartTick(tick: Tick?): CombatEntity
    var specRegenStartTick: Tick?
    val hasLightbearer: Boolean
}

class GenericCombatEntity(
        override val name: String,
        override val health: Health,
        override val specialAttackEnergy: SpecialAttackEnergy = SpecialAttackEnergy(),
        override val hasLightbearer: Boolean = false,
        override var specRegenStartTick: Tick? = null,
        override val combatStats: CombatStats = CombatStats(defenceLevel = 1)
) : CombatEntity {
    private var lastAttackTick = Tick(0)
        get() = field
        private set(value) {
            field = value
        }

    override val isAlive: Boolean
        get() = health.value > 0

    private var lastAttackDelay: Int = 0


    override fun canAttack(currentTick: Tick): Boolean {
        return currentTick >= lastAttackTick + Tick(lastAttackDelay) &&
               isAlive
    }
    
//    override fun useSpecialAttack(): Boolean {
//        val specialCost = weapon?.specialAttackCost ?: return false
//        if (specialAttackEnergy.canUseSpecial(specialCost)) {
//            return true
//        }
//        return false
//    }
    
    override fun regenerateSpecialAttack(): GenericCombatEntity {
        specialAttackEnergy.regenerate()
        return this
    }
    
    override fun takeDamage(damage: Int): GenericCombatEntity {
        require(damage >= 0) { "Damage cannot be negative" }
        health.takeDamage(damage)
        return this
    }
    
    override fun setLastAttackTick(tick: Tick, weaponDelay: Int): GenericCombatEntity {
        require(tick.value >= 0) { "Attack tick cannot be negative" }
        require(lastAttackDelay >= 0) { "Attack tick cannot be negative" }
        lastAttackTick = tick
        lastAttackDelay = weaponDelay
        return this
    }
    
    override fun setSpecRegenStartTick(tick: Tick?): GenericCombatEntity {
        if (tick != null) {
            require(tick.value >= 0) { "Regeneration start tick cannot be negative" }
        }
        specRegenStartTick = tick
        return this
    }
} 