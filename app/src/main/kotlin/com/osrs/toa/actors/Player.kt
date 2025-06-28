package com.osrs.toa.actors

import com.osrs.toa.Tick
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.Weapon

class Player(
        combatEntity: GenericCombatEntity,
        private val mainWeapon: Weapon,
        private  val specWeapon: SpecWeapon
) : CombatEntity by combatEntity {
    
    private var lastSurgePotTick: Tick? = null
    private val surgePotCooldown = 500
    
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
    
    fun drinkSurgePot(currentTick: Tick): Boolean {
        // Cannot drink if at full spec
        if (specialAttackEnergy.energy >= 100) {
            println("Cannot drink surge potion: already at full special attack energy")
            return false
        }
        
        // Cannot drink if still on cooldown
        if (lastSurgePotTick != null && currentTick < lastSurgePotTick!! + Tick(surgePotCooldown)) {
            val remainingCooldown = (lastSurgePotTick!! + Tick(surgePotCooldown) - currentTick).value
            println("Cannot drink surge potion: on cooldown for $remainingCooldown more ticks")
            return false
        }
        
        // Drink the potion
        lastSurgePotTick = currentTick
        specialAttackEnergy.regenerate(25)
        println("Drank surge potion on tick ${currentTick.value}. Special attack energy: ${specialAttackEnergy.energy}")
        return true
    }
}
