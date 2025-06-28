package com.osrs.toa.weapons

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
            // hardcode to 530 akkha rn, no magus
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
            // hardcode to 530 akkha rn, no magus
            val hitChance = .8953
            val maxHit = 84
            val hitSuccesful = Random.nextDouble() <= hitChance
            val damageRoll = Random.nextInt(1, maxHit + 1)
            return if (hitSuccesful) max(1, damageRoll - 1) else 0
        }
    }

    val ZaryteCrossbow: SpecWeapon = object: SpecWeapon {
        override val specialAttackCost = 75
        override fun spec(target: CombatEntity): Int {
            val accuracy = .7864
            val damage = min(110, (target.health.value * .22).toInt())
            val hitSuccesful = Random.nextDouble() <= accuracy
            return if (hitSuccesful) damage else 0
        }

        override val name = "Zaryte Crossbow"
        override val attackSpeed = 5
        override fun attack(target: CombatEntity): Int {
            TODO("Not yet implemented")
        }

    }
}