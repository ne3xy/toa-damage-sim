package com.osrs.toa.actors

import com.osrs.toa.Tick
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon

class Player(
    combatEntity: GenericCombatEntity,
    private val useSurgePots: Boolean = true,
    private val useLiquidAdrenaline: Boolean = true
) : CombatEntity by combatEntity {
    
    private var lastSurgePotTick: Tick? = null
    private val surgePotCooldown = 500
    
    private var liquidAdrenalineActive = false
    private var liquidAdrenalineStartTick: Tick? = null
    private val liquidAdrenalineDuration = 250
    
    fun attack(currentTick: Tick, target: CombatEntity, normalWeapon: Weapon, specWeapon: SpecWeapon?, shouldSpec: Boolean) {
        if (canAttack(currentTick)) {
            if (shouldSpec && specWeapon != null && specialAttackEnergy.canUseSpecial(getSpecCost(specWeapon.specialAttackCost, currentTick))) {
                setLastAttackTick(currentTick, specWeapon.attackSpeed)
                val damage = specWeapon.attack(target)
                target.takeDamage(damage)
                specialAttackEnergy.consume(getSpecCost(specWeapon.specialAttackCost, currentTick))
                println("dealt $damage damage to ${target.name} with ${specWeapon.name} on tick ${currentTick.value}. it has ${target.health.value} health")
            } else {
                setLastAttackTick(currentTick, normalWeapon.attackSpeed)
                val damage = normalWeapon.attack(target)
                target.takeDamage(damage)
                println("dealt $damage damage to ${target.name} with ${normalWeapon.name} on tick ${currentTick.value}. it has ${target.health.value} health")
            }
        }
    }
    
    fun drinkSurgePot(currentTick: Tick): Boolean {
        // If surge pots are disabled, return false immediately
        if (!useSurgePots) {
            return false
        }
        
        // Cannot drink if at full spec
        if (specialAttackEnergy.energy >= 100) {
            println("Cannot drink surge potion: already at full special attack energy")
            return false
        }
        
        // Cannot drink if still on cooldown
        if (lastSurgePotTick != null && currentTick < lastSurgePotTick!! + Tick(surgePotCooldown)) {
            val remainingCooldown = (lastSurgePotTick!! + Tick(surgePotCooldown) - currentTick).value
            return false
        }
        
        // Drink the potion
        lastSurgePotTick = currentTick
        specialAttackEnergy.regenerate(25)
        println("Drank surge potion on tick ${currentTick.value}. Special attack energy: ${specialAttackEnergy.energy}")
        return true
    }
    
    fun drinkLiquidAdrenaline(currentTick: Tick): Boolean {
        // If liquid adrenaline is disabled, return false immediately
        if (!useLiquidAdrenaline) {
            return false
        }
        
        // Can only drink once ever (startTick is null if never drunk)
        if (liquidAdrenalineStartTick != null) {
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
