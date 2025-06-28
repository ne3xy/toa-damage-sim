package com.osrs.toa.weapons

import com.osrs.toa.actors.CombatEntity

// Weapon system
interface Weapon {
    val name: String
    val attackSpeed: Int
    fun attack(target: CombatEntity): Int
}

interface SpecWeapon: Weapon {
    val specialAttackCost: Int
    fun spec(target: CombatEntity): Int
}