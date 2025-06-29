package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.ToaMonsterCombatStats
import com.osrs.toa.actors.DefenceDrainCappedCombatStats
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.PlayerLoadout
import com.osrs.toa.sims.BossFight
import kotlin.math.max
import com.osrs.toa.actors.ToaCombatEntity

object ZebakConstants {
    const val BASE_HP = 580
}

class Zebak(
    private val loadout: PlayerLoadout,
    private val zebakBoss: ZebakBoss
): BossFight {
    private val defenceReductionThreshold = 13
    private val healthThreshold = 0.5 // 50% health
    
    // Use provided strategy or default to ZebakMainFightStrategy
    private val specStrategy = loadout.strategy
    
    // Capture initial values for comparison
    private val initialDefence = zebakBoss.combatStats.defenceLevel
    private val initialHealth = zebakBoss.health.value

    override fun onTick(tick: Tick) {
        if (zebakBoss.isAttackable(tick)) {
            if (loadout.player.specialAttackEnergy.energy < 50) {
                loadout.player.drinkSurgePot(tick)
            }
            val (normalWeapon, specWeapon, shouldSpec) = specStrategy.selectWeapons(tick, loadout.mainWeapon)
            if (shouldSpec && tick.value != 0) {
                loadout.player.drinkLiquidAdrenaline(tick)
            }
            loadout.player.attack(tick, zebakBoss, normalWeapon, specWeapon, shouldSpec)
        }
    }

    override fun isFightOver() = !zebakBoss.isAlive
    
    // Expose the zebakBoss for external access
    val zebak: ZebakBoss = zebakBoss
    
    companion object {
        fun create(
            loadout: PlayerLoadout,
            invocationLevel: Int,
            pathLevel: Int
        ): Zebak {
            // Move zebakBaseStats here so it's fresh for each Zebak
            val zebakBaseStats = DefaultCombatStats(
                defenceLevel = 70, // Level 3 Zebak defence level
                magicLevel = 100, // Level 3 Zebak magic level
                meleeSlashDefenceBonus = 160,
                rangedDefenceBonus = 110,
                magicDefenceBonus = 200
            )
            
            val zebakBoss = ZebakBoss(ToaCombatEntity(
                name = "$invocationLevel Level $pathLevel Zebak",
                baseHp = ZebakConstants.BASE_HP,
                invocationLevel = invocationLevel,
                pathLevel = pathLevel,
                baseCombatStats = DefenceDrainCappedCombatStats(ToaMonsterCombatStats(zebakBaseStats, invocationLevel = invocationLevel), drainCap = 20)
            ))
            
            return Zebak(loadout, zebakBoss)
        }
    }
}

class ZebakMainFightStrategy(private val zebak: ZebakBoss) : SpecStrategy {
    // Capture initial values for comparison
    private val initialDefence = zebak.combatStats.defenceLevel
    private val initialHealth = zebak.health.value
    
    // Cache thresholds to avoid recalculation
    private val defenceReductionThreshold = 13
    private val healthThreshold = 0.5
    
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        val normalWeapon = mainWeapon
        
        val shouldUseBgs = shouldUseBgs()
        val shouldUseZcb = zebak.shouldZcbSpec()
        
        val specWeapon = when {
            shouldUseBgs -> Weapons.BandosGodsword
            shouldUseZcb -> Weapons.ZaryteCrossbow
            else -> null
        }
        
        val shouldSpec = tick.value != 0 && (shouldUseBgs || shouldUseZcb)
        
        return Triple(normalWeapon, specWeapon, shouldSpec)
    }
    
    private fun shouldUseBgs(): Boolean {
        // Don't use BGS if we've reduced defence by at least defenceReductionThreshold
        val currentDefence = zebak.combatStats.defenceLevel
        val defenceReduced = initialDefence - currentDefence
        if (defenceReduced >= defenceReductionThreshold) {
            return false
        }
        
        // Don't use BGS if health is below threshold
        val healthPercentage = zebak.health.value.toDouble() / initialHealth.toDouble()
        return healthPercentage > healthThreshold
    }
}

// Future Zebak strategies can be added here:
// class ZebakShadowFightStrategy(private val zebak: ZebakBoss) : SpecStrategy { ... }
// class ZebakSpeedRunStrategy(private val zebak: ZebakBoss) : SpecStrategy { ... }

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