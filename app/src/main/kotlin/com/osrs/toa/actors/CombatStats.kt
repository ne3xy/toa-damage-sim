package com.osrs.toa.actors

import kotlin.math.max

interface CombatStats {
    val defenceLevel: Int
    fun getDefenceRoll(attackStyle: AttackStyle): Int
    fun reduceDefenceLevel(amount: Int)
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
    
    override fun getDefenceRoll(attackStyle: AttackStyle): Int {
        return (combatStats.getDefenceRoll(attackStyle) * (1 + (.02 * invocationLevel / 5))).toInt()
    }
    
    override fun reduceDefenceLevel(amount: Int) {
        combatStats.reduceDefenceLevel(amount)
    }
}

class DefaultCombatStats(
    defenceLevel: Int,
    private val magicLevel: Int = 0,
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
    override fun reduceDefenceLevel(amount: Int) {
        require(amount >= 0) { "Defence reduction amount must be non-negative" }
        defenceLevel = max(0, defenceLevel - amount)
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