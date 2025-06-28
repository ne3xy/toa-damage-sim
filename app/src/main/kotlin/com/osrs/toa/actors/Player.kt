package com.osrs.toa.actors

import com.osrs.toa.Tick
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.Weapon

class Player(
        combatEntity: GenericCombatEntity,
        private val mainWeapon: Weapon,
        private  val specWeapon: SpecWeapon
) : CombatEntity by combatEntity {
    fun attack(currentTick: Tick, target: CombatEntity, shouldSpec: () -> Boolean = {true}) {
        if (canAttack(currentTick)) {
            if (shouldSpec() && specialAttackEnergy.canUseSpecial(specWeapon.specialAttackCost)) {
                setLastAttackTick(currentTick, specWeapon.attackSpeed)
                val damage = specWeapon.spec(target)
                target.takeDamage(damage)
                specialAttackEnergy.consume(specWeapon.specialAttackCost)
                println("dealt $damage damage to ${target.name} with ${specWeapon.name} on tick ${currentTick.value}. it has ${target.health.value} health")

            } else {
                setLastAttackTick(currentTick, mainWeapon.attackSpeed)
                val damage = mainWeapon.attack(target)
                target.takeDamage(damage)
                println("dealt $damage damage to ${target.name} with ${mainWeapon.name} on tick ${currentTick.value}. it has ${target.health.value} health")
            }
        }
    }
}
