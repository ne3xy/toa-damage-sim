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
import kotlin.math.max
import com.osrs.toa.actors.ToaCombatEntity

object ZebakConstants {
    const val BASE_HP = 580
}

class Zebak(
    private val loadout: PlayerLoadout,
    specStrategy: SpecStrategy? = null,
    private val invocationLevel: Int,
    private val pathLevel: Int
): BossFight {
    private val defenceReductionThreshold = 13
    private val healthThreshold = 0.5 // 50% health
    
    // Move zebakBaseStats here so it's fresh for each Zebak
    private val zebakBaseStats = DefaultCombatStats(
        defenceLevel = 70, // Level 3 Zebak defence level
        magicLevel = 100, // Level 3 Zebak magic level
        meleeSlashDefenceBonus = 160,
        rangedDefenceBonus = 110,
        magicDefenceBonus = 200
    )
    
    // Calculate scaled HP based on invocation and path level
    private val scaledHp = ToaCombatEntity.calculateScaledHp(ZebakConstants.BASE_HP, invocationLevel, pathLevel)
    
    val zebak = ZebakBoss(GenericCombatEntity(
        name = "$invocationLevel Level $pathLevel Zebak",
        health = Health(scaledHp),
        combatStats = DefenceDrainCappedCombatStats(ToaMonsterCombatStats(zebakBaseStats, invocationLevel = invocationLevel), drainCap = 20)
    ))
    
    // Use provided strategy or default to ZebakMainFightStrategy
    private val specStrategy = specStrategy ?: ZebakMainFightStrategy(zebak)
    
    // Capture initial values for comparison
    private val initialDefence = zebak.combatStats.defenceLevel
    private val initialHealth = zebak.health.value

    override fun onTick(tick: Tick) {
        if (zebak.isAttackable(tick)) {
            if (loadout.player.specialAttackEnergy.energy < 50) {
                loadout.player.drinkSurgePot(tick)
            }
            val (normalWeapon, specWeapon, shouldSpec) = specStrategy.selectWeapons(tick, loadout.mainWeapon)
            if (shouldSpec && tick.value != 0) {
                loadout.player.drinkLiquidAdrenaline(tick)
            }
            loadout.player.attack(tick, zebak, normalWeapon, specWeapon, shouldSpec)
        }
    }

    override fun isFightOver() = !zebak.isAlive
}

class ZebakMainFightStrategy(private val zebak: ZebakBoss) : SpecStrategy {
    // Capture initial values for comparison
    private val initialDefence = zebak.combatStats.defenceLevel
    private val initialHealth = zebak.health.value
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        val normalWeapon = mainWeapon
        
        val shouldUseBgs = shouldUseBgs()
        val shouldUseZcb = zebak.shouldZcbSpec()
        
        val specWeapon = if (shouldUseBgs) {
            Weapons.BandosGodsword
        } else if (shouldUseZcb) {
            Weapons.ZaryteCrossbow
        } else {
            null
        }
        
        val shouldSpec = tick.value != 0 && (shouldUseBgs || shouldUseZcb)
        
        return Triple(normalWeapon, specWeapon, shouldSpec)
    }
    
    private fun shouldUseBgs(): Boolean {
        // Strategy-specific parameters
        val defenceReductionThreshold = 13
        val healthThreshold = 0.5
        val initialDefence = initialDefence
        val initialHealth = initialHealth
        
        // Don't use BGS if we've reduced defence by at least defenceReductionThreshold
        val currentDefence = zebak.combatStats.defenceLevel
        val defenceReduced = initialDefence - currentDefence
        if (defenceReduced >= defenceReductionThreshold) {
            return false
        }
        
        // Don't use BGS if health is below threshold
        val healthPercentage = zebak.health.value.toDouble() / initialHealth.toDouble()
        if (healthPercentage <= healthThreshold) {
            return false
        }
        
        return true
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