package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats

class Zebak(
        private val player: Player
): BossFight {
    //hardcode 530 level3
    val zebak = ZebakBoss(GenericCombatEntity(
            name = "530 Level 3 Zebak",
            health = Health(2130),
            combatStats = DefaultCombatStats(
                defenceLevel = 200, // Level 3 Zebak defence level
                meleeStabDefenceBonus = 100,
                rangedDefenceBonus = 100,
                magicDefenceBonus = 100
            )
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