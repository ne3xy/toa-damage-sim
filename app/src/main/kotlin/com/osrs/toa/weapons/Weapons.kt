package com.osrs.toa.weapons

import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.CombatEntity
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// Predefined weapons
object Weapons {

    val TumekensShadow: Weapon = object: Weapon {
        override val name = "Tumeken's Shadow"
        override val attackSpeed = 5
        override fun attack(target: CombatEntity): Int {
            val hitChance = .8858
            val maxHit = 80
            val hitSuccesful = Random.nextDouble() <= hitChance
            val damageRoll = Random.nextInt(1, maxHit + 1)
            return if (hitSuccesful) max(1, damageRoll - 1) else 0
        }
    }

    val MagussShadow: Weapon = object: Weapon {
        override val name = "Tumeken's Shadow"
        override val attackSpeed = 5
        override fun attack(target: CombatEntity): Int {
            val hitChance = .8953
            val maxHit = 84
            val hitSuccesful = Random.nextDouble() <= hitChance
            val damageRoll = Random.nextInt(1, maxHit + 1)
            return if (hitSuccesful) max(1, damageRoll - 1) else 0
        }
    }

    //salted, takeoffs
    val ZaryteCrossbow: SpecWeapon = object: SpecWeapon, Weapon by BaseWeapon(
        name = "Zaryte Crossbow",
        attackSpeed = 5,
        attackStyle = AttackStyle.RANGED_HEAVY,
        attackRoll = 48190,
        hitDamage = { target ->
            val damage = min(110, (target.health.value * .22).toInt())
            damage
        }
    ) {
        override val specialAttackCost = 75
    }

    
    fun calculateFangDamageRange(trueMax: Int): Pair<Int, Int> {
        // Fang deals between 15% and 85% of max hit when it hits
        val minDamage = kotlin.math.floor(trueMax * 0.15).toInt()
        val maxDamage = kotlin.math.ceil(trueMax * 0.85).toInt()
        return Pair(minDamage, maxDamage)
    }

    fun calculateFangDamage(trueMax: Int): Int {
        val (minDamage, maxDamage) = calculateFangDamageRange(trueMax)
        return Random.nextInt(minDamage, maxDamage)
    }

    //SCB
    val UltorFang: Weapon = BaseWeapon(
        name = "Osmumten's Fang (Ultor+SCB)",
        attackSpeed = 5,
        attackStyle = AttackStyle.MELEE_STAB,
        attackRoll = 36654,
        hitDamage = { target ->
            val trueMax = 59
            calculateFangDamage(trueMax)
        },
        hitRollProvider = { hitChance ->
            AccuracyCalculator.doesAttackHit(hitChance) || AccuracyCalculator.doesAttackHit(hitChance)
        }
    )

    //LB+SCB
    val LightbearerFang: Weapon = BaseWeapon(
        name = "Osmumten's Fang (LB+SCB)",
        attackSpeed = 5,
        attackStyle = AttackStyle.MELEE_STAB,
        attackRoll = 36654,
        hitDamage = { target ->
            val trueMax = 57
            calculateFangDamage(trueMax)
        },
        hitRollProvider = { hitChance ->
            AccuracyCalculator.doesAttackHit(hitChance) || AccuracyCalculator.doesAttackHit(hitChance)
        }
    )

    val Zebak6WayTwistedBow: Weapon = NormalDamageBaseWeapon(
        name = "Twisted Bow",
        attackSpeed = 5,
        attackStyle = AttackStyle.RANGED,
        attackRoll = 56105,
        maxHit = 81
    )

    private val BandosGodswordBaseWeapon = NormalDamageBaseWeapon(
        name = "Bandos Godsword",
        attackSpeed = 6,
        attackStyle = AttackStyle.MELEE_SLASH,
        attackRoll = 39123, 
        maxHit = 80
    )

    private val LightbearerBandosGodswordBaseWeapon = NormalDamageBaseWeapon(
        name = "Bandos Godsword",
        attackSpeed = 6,
        attackStyle = AttackStyle.MELEE_SLASH,
        attackRoll = 39123, 
        maxHit = 77
    )

    private fun BaseBandosGodsword(baseWeapon: Weapon): SpecWeapon = object: SpecWeapon, Weapon by baseWeapon {
        override val specialAttackCost = 50
        override fun attack(target: CombatEntity): Int {
            val damage = baseWeapon.attack(target)
            target.combatStats.drainDefenceLevel(damage)
            return damage
        }
    }
    val BandosGodsword = BaseBandosGodsword(BandosGodswordBaseWeapon)
    val LightbearerBandosGodsword = BaseBandosGodsword(LightbearerBandosGodswordBaseWeapon)

    val Voidwaker: SpecWeapon = object: SpecWeapon {
        override val specialAttackCost = 50
        override val name = "Voidwaker"
        override val attackSpeed = 5
        override fun attack(target: CombatEntity): Int {
            val accuracy = .85
            val maxHit = 50
            val hitSuccesful = Random.nextDouble() <= accuracy
            val damageRoll = Random.nextInt(1, maxHit + 1)
            return if (hitSuccesful) max(1, damageRoll - 1) else 0
        }
    }
}