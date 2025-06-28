package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.ToaMonsterCombatStats
import kotlin.math.max

class Zebak(
        private val player: Player
): BossFight {
    //hardcode 530 level3
    val zebak = ZebakBoss(GenericCombatEntity(
            name = "530 Level 3 Zebak",
            health = Health(2130),
            combatStats = ZebakCombatStats()
    ))

    override fun onTick(tick: Tick) {
        if (zebak.isAttackable(tick)) {
            if (player.specialAttackEnergy.energy < 50) {
                player.drinkSurgePot(tick)
            }
            
            // Drink liquid adrenaline before first ZCB spec
            if (tick.value != 0 && zebak.shouldZcbSpec()) {
                player.drinkLiquidAdrenaline(tick)
            }
            
            // Disallow speccing on tick 0
            player.attack(tick, zebak, shouldSpec = { tick.value != 0 && zebak.shouldZcbSpec() })
        }
    }

    override fun isFightOver() = !zebak.isAlive
}

class ZebakBoss(private val combatEntity: CombatEntity): CombatEntity by combatEntity {
    fun isAttackable(tick: Tick): Boolean {
        return isAlive
    }
    
    fun shouldZcbSpec(): Boolean {
        // Simple logic: spec when health is high enough to make it worthwhile
        // You can adjust this threshold based on your preferences
        return health.value > 500
    }
} 

private val zebakBaseCombatStats = ToaMonsterCombatStats(DefaultCombatStats(
                defenceLevel = 70, // Level 3 Zebak defence level
                magicLevel = 100, // Level 3 Zebak magic level
                meleeSlashDefenceBonus = 160,
                rangedDefenceBonus = 110,
                magicDefenceBonus = 200
            ), invocationLevel = 530)

private object ZebakCombatStats: CombatStats by zebakBaseCombatStats {
    override fun reduceDefenceLevel(amount: Int) {
        // Defense can't be lowered past 50
        val currentDefence = zebakBaseCombatStats.defenceLevel
        val newDefence = max(50, currentDefence - amount)
        val actualReduction = currentDefence - newDefence
        
        if (actualReduction > 0) {
            zebakBaseCombatStats.reduceDefenceLevel(actualReduction)
        }
    }
}