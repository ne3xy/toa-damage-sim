package com.osrs.toa.actors

data class CombatStats(
    val defenceLevel: Int,
    val meleeStabDefenceBonus: Int = 0,
    val meleeSlashDefenceBonus: Int = 0,
    val meleeCrushDefenceBonus: Int = 0,
    val rangedDefenceBonus: Int = 0,
    val rangedLightDefenceBonus: Int = 0,
    val rangedHeavyDefenceBonus: Int = 0,
    val magicDefenceBonus: Int = 0
) {
    fun getDefenceRoll(attackStyle: AttackStyle): Int {
        val styleDefenceBonus = when (attackStyle) {
            AttackStyle.MELEE_STAB -> meleeStabDefenceBonus
            AttackStyle.MELEE_SLASH -> meleeSlashDefenceBonus
            AttackStyle.MELEE_CRUSH -> meleeCrushDefenceBonus
            AttackStyle.RANGED -> rangedDefenceBonus
            AttackStyle.RANGED_LIGHT -> rangedLightDefenceBonus
            AttackStyle.RANGED_HEAVY -> rangedHeavyDefenceBonus
            AttackStyle.MAGIC -> magicDefenceBonus
        }
        return (defenceLevel + 9) * (styleDefenceBonus + 64)
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