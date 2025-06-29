package com.osrs.toa.actors

import kotlin.math.max

interface CombatStats {
    val defenceLevel: Int
    val magicLevel: Int
    fun getDefenceRoll(attackStyle: AttackStyle): Int
    fun drainDefenceLevel(amount: Int)
    fun drainMagicLevel(amount: Int)
}

class ToaMonsterCombatStats(
    private val combatStats: CombatStats,
    private val invocationLevel: Int
): CombatStats by combatStats {
    init {
        require(invocationLevel >= 0) { "Invocation level must be non-negative" }
        require(invocationLevel % 5 == 0) { "Invocation level must be divisible by 5" }
    }
    
    override val defenceLevel: Int
        get() = combatStats.defenceLevel
    
    override val magicLevel: Int
        get() = combatStats.magicLevel
    
    override fun getDefenceRoll(attackStyle: AttackStyle): Int {
        return (combatStats.getDefenceRoll(attackStyle) * (1 + (.02 * invocationLevel / 5))).toInt()
    }
    
    override fun drainDefenceLevel(amount: Int) {
        combatStats.drainDefenceLevel(amount)
    }
    
    override fun drainMagicLevel(amount: Int) {
        combatStats.drainMagicLevel(amount)
    }
}

class DefaultCombatStats(
    defenceLevel: Int,
    magicLevel: Int = 0,
    private val meleeStabDefenceBonus: Int = 0,
    private val meleeSlashDefenceBonus: Int = 0,
    private val meleeCrushDefenceBonus: Int = 0,
    private val rangedDefenceBonus: Int = 0,
    private val rangedLightDefenceBonus: Int = 0,
    private val rangedHeavyDefenceBonus: Int = 0,
    private val magicDefenceBonus: Int = 0
) : CombatStats {
    override var defenceLevel: Int = defenceLevel
        private set(value) {
            field = value
        }
    
    override var magicLevel: Int = magicLevel
        private set(value) {
            field = value
        }
    
    override fun getDefenceRoll(attackStyle: AttackStyle): Int {
        val styleDefenceBonus = when (attackStyle) {
            AttackStyle.MELEE_STAB -> meleeStabDefenceBonus
            AttackStyle.MELEE_SLASH -> meleeSlashDefenceBonus
            AttackStyle.MELEE_CRUSH -> meleeCrushDefenceBonus
            AttackStyle.RANGED -> rangedDefenceBonus
            AttackStyle.RANGED_LIGHT -> rangedLightDefenceBonus
            AttackStyle.RANGED_HEAVY -> rangedHeavyDefenceBonus
            AttackStyle.MAGIC -> magicDefenceBonus
        }
        
        return when (attackStyle) {
            AttackStyle.MAGIC -> (9 + magicLevel) * (styleDefenceBonus + 64)
            else -> (defenceLevel + 9) * (styleDefenceBonus + 64)
        }
    }
    
    //TODO: Implement stat regen
    override fun drainDefenceLevel(amount: Int) {
        require(amount >= 0) { "Defence reduction amount must be non-negative" }
        defenceLevel = max(0, defenceLevel - amount)
    }
    
    override fun drainMagicLevel(amount: Int) {
        require(amount >= 0) { "Magic reduction amount must be non-negative" }
        magicLevel = max(0, magicLevel - amount)
    }
}

class DefenceDrainCappedCombatStats(
    private val baseCombatStats: CombatStats,
    private val drainCap: Int
    ) : CombatStats by baseCombatStats {
        private val maxDefence = baseCombatStats.defenceLevel
    override fun drainDefenceLevel(amount: Int) {
        val currentDefence = baseCombatStats.defenceLevel
        val newDefence = max(maxDefence - drainCap, currentDefence - amount)
        val actualReduction = currentDefence - newDefence
        if (actualReduction > 0) {
            baseCombatStats.drainDefenceLevel(actualReduction)
        }
    }
}

enum class AttackStyle {
    MELEE_STAB,
    MELEE_SLASH,
    MELEE_CRUSH,
    RANGED,
    RANGED_LIGHT,
    RANGED_HEAVY,
    MAGIC
} 