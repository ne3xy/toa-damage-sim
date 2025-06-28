package com.osrs.toa

import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.sims.BossFight

// Combat simulation engine
class CombatSimulator(private val player: Player, private val boss: BossFight) {
    private var currentTick = Tick(0)
    
    fun simulateTick() {
        handleSpecialAttackEnergy(player)
        boss.onTick(currentTick)
        currentTick += Tick(1)
    }
    
    private fun handleSpecialAttackEnergy(entity: CombatEntity) {
        if (!entity.specialAttackEnergy.isRegenerating()) {
            // Reset regeneration start tick when at full energy
            entity.specRegenStartTick = null
            return
        } else if (entity.specRegenStartTick == null) {
            entity.specRegenStartTick = currentTick
            return
        }
        
        val regenerationInterval = if (entity.hasLightbearer) 25 else 50
        val ticksSinceRegenStart = currentTick.value - entity.specRegenStartTick!!.value
        
        println("Debug: currentTick=${currentTick.value}, specRegenStartTick=${entity.specRegenStartTick!!.value}, ticksSinceRegenStart=$ticksSinceRegenStart, regenerationInterval=$regenerationInterval, energy=${entity.specialAttackEnergy.energy}")
        
        if (ticksSinceRegenStart > 0 && ticksSinceRegenStart % regenerationInterval == 0) {
            println("Debug: Regenerating special attack energy!")
            entity.regenerateSpecialAttack()
        }
    }
    
    fun runSimulation(): Tick {
        while (!boss.isFightOver() && currentTick < Tick(700)) {
            simulateTick()
        }
        
        if (currentTick >= Tick(700)) {
            throw IllegalStateException("Simulation timed out after 700 ticks. Fight did not complete.")
        }
        
        println("Simulation complete on tick ${currentTick.value}!")
        return currentTick
    }
} 