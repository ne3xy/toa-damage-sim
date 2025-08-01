package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import kotlin.random.Random
import kotlin.math.max

// Weapon system
interface Weapon {
    val name: String
    val attackSpeed: Int
    fun attack(target: CombatEntity): Int
}

//TODO: Should specweapon have a base attack? maybe cleaner if eg zcb spec & zcb mainhand are separate weapons
interface SpecWeapon: Weapon {
    val specialAttackCost: Int
}

class NormalDamageBaseWeapon(
    name: String,
    attackSpeed: Int,
    attackStyle: AttackStyle,
    private val attackRoll: Int,
    maxHit: Int,
    private val hitRollProvider: (Double) -> Boolean = { hitChance ->
        AccuracyCalculator.doesAttackHit(hitChance)
    }
) : Weapon by BaseWeapon(
    name, 
    attackSpeed, 
    attackStyle, 
    attackRoll, 
    { _ -> 
        require(maxHit >= 1) { "maxHit must be at least 1" }
        val damageRoll = Random.nextInt(1, maxHit + 1)
        max(1, damageRoll - 1)
    },
    hitRollProvider
) {
    init {
        require(maxHit >= 1) { "maxHit must be at least 1" }
    }
}

class BaseWeapon(
    override val name: String,
    override val attackSpeed: Int,
    private val attackStyle: AttackStyle,
    private val attackRoll: Int,
    val hitDamage: (CombatEntity) -> Int,
    private val hitRollProvider: (Double) -> Boolean = { hitChance ->
        AccuracyCalculator.doesAttackHit(hitChance)
    }
) : Weapon {
    
    override fun attack(target: CombatEntity): Int {
        val defenceRoll = target.combatStats.getDefenceRoll(attackStyle)
        val hitChance = AccuracyCalculator.calculateHitChance(attackRoll, defenceRoll)
        
        return if (hitRollProvider(hitChance)) {
            hitDamage(target)
        } else {
            0
        }
    }
}