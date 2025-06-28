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
    
    private var liquidAdrenalineActive = false
    private var liquidAdrenalineStartTick: Tick? = null
    private val liquidAdrenalineDuration = 250
    
    fun attack(currentTick: Tick, target: CombatEntity, shouldSpec: () -> Boolean = {true}) {
        if (canAttack(currentTick)) {
            if (shouldSpec() && specialAttackEnergy.canUseSpecial(getSpecCost(specWeapon.specialAttackCost, currentTick))) {
                setLastAttackTick(currentTick, specWeapon.attackSpeed)
                val damage = specWeapon.spec(target)
                target.takeDamage(damage)
                specialAttackEnergy.consume(getSpecCost(specWeapon.specialAttackCost, currentTick))
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
    
    fun drinkLiquidAdrenaline(currentTick: Tick): Boolean {
        // Can only drink once ever (startTick is null if never drunk)
        if (liquidAdrenalineStartTick != null) {
            println("Cannot drink liquid adrenaline: already used")
            return false
        }
        
        // Drink the potion
        liquidAdrenalineActive = true
        liquidAdrenalineStartTick = currentTick
        println("Drank liquid adrenaline on tick ${currentTick.value}. Special attack costs halved for $liquidAdrenalineDuration ticks")
        return true
    }
    
    private fun getSpecCost(baseCost: Int, currentTick: Tick): Double {
        if (liquidAdrenalineActive && liquidAdrenalineStartTick != null) {
            val ticksElapsed = currentTick - liquidAdrenalineStartTick!!
            if (ticksElapsed.value >= liquidAdrenalineDuration) {
                // Effect has expired
                liquidAdrenalineActive = false
                println("Liquid adrenaline effect expired on tick ${currentTick.value}")
                return baseCost.toDouble()
            }
            // Halve the cost with decimal precision
            return baseCost / 2.0
        }
        return baseCost.toDouble()
    }
}
