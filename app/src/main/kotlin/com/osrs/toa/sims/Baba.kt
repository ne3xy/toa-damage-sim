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

object BabaConstants {
    const val BASE_HP = 380
}

class Baba(
    private val loadout: PlayerLoadout,
    private val babaBoss: BabaBoss
): BossFight {
    private val defenceReductionThreshold = 13
    private val healthThreshold = 0.5 // 50% health
    
    // Use provided strategy or default to BabaMainFightStrategy
    private val specStrategy = loadout.strategy
    
    // Capture initial values for comparison
    private val initialDefence = babaBoss.combatStats.defenceLevel
    private val initialHealth = babaBoss.health.value

    override fun onTick(tick: Tick) {
        // Spawn baboon throwers at tick 37
        babaBoss.maybeSpawnBaboonThrowers(tick)
        
        // Check if baboon throwers are alive and attack them first (even during boulder phases)
        if (babaBoss.getBaboonThrowers().any { it.isAlive }) {
            val aliveThrower = babaBoss.getBaboonThrowers().first { it.isAlive }
            // Attack baboon thrower with 1 tick delay and guaranteed kill
            if (loadout.player.canAttack(tick)) {
                loadout.player.setLastAttackTick(tick, 1) // 1 tick delay
                aliveThrower.takeDamage(1) // Guaranteed kill
                loadout.player.specialAttackEnergy.regenerate(15) // Restore 15 spec energy
                println("killed baboon thrower on tick ${tick.value}. restored 15 spec energy")
            }
        } else if (babaBoss.isAttackable(tick)) {
            // Only attack Baba if no baboon throwers are alive and Baba is not in boulder phase
            if (loadout.player.specialAttackEnergy.energy < 51) {
                loadout.player.drinkSurgePot(tick)
            }
            val (normalWeapon, specWeapon, shouldSpec) = specStrategy.selectWeapons(tick, loadout.mainWeapon)
            // No liquid adrenaline for Baba
            loadout.player.attack(tick, babaBoss, normalWeapon, specWeapon, shouldSpec)
        }
    }

    override fun isFightOver() = !babaBoss.isAlive
    
    // Expose the babaBoss for external access
    val baba: BabaBoss = babaBoss
    
    companion object {
        fun create(
            loadout: PlayerLoadout,
            invocationLevel: Int,
            pathLevel: Int
        ): Baba {
            // Move babaBaseStats here so it's fresh for each Baba
            val babaBaseStats = DefaultCombatStats(
                defenceLevel = 80, // Level 3 Baba defence level
                magicLevel = 100, // Level 3 Baba magic level
                meleeSlashDefenceBonus = 160,
                meleeStabDefenceBonus = 80,
                rangedDefenceBonus = 200,
                rangedHeavyDefenceBonus = 120,
                magicDefenceBonus = 280
            )
            
            val babaBoss = BabaBoss(ToaCombatEntity(
                name = "$invocationLevel Level $pathLevel Baba",
                baseHp = BabaConstants.BASE_HP,
                invocationLevel = invocationLevel,
                pathLevel = pathLevel,
                baseCombatStats = DefenceDrainCappedCombatStats(babaBaseStats, drainCap = 20)
            ))
            
            return Baba(loadout, babaBoss)
        }
    }
}

class BabaMainFightStrategy(private val baba: BabaBoss) : SpecStrategy {
    // Capture initial values for comparison
    private val initialDefence = baba.combatStats.defenceLevel
    private val initialHealth = baba.health.value
    
    // Cache thresholds to avoid recalculation
    private val defenceReductionThreshold = 13
    private val healthThreshold = 0.5
    private val voidwakerThreshold = 500 // Use Voidwaker when Baba is under 500 HP
    
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        val normalWeapon = mainWeapon
        
        val shouldUseBgs = shouldUseBgs()
        val shouldUseZcb = baba.shouldZcbSpec()
        val shouldUseVoidwaker = baba.shouldVoidwakerSpec()
        
        val specWeapon = when {
            shouldUseBgs -> Weapons.BandosGodsword
            shouldUseZcb -> Weapons.ZaryteCrossbow
            shouldUseVoidwaker -> Weapons.Voidwaker
            else -> null
        }
        
        val shouldSpec = (shouldUseBgs || shouldUseZcb || shouldUseVoidwaker)
        
        return Triple(normalWeapon, specWeapon, shouldSpec)
    }
    
    private fun shouldUseBgs(): Boolean {
        // Don't use BGS if we've reduced defence by at least defenceReductionThreshold
        val currentDefence = baba.combatStats.defenceLevel
        val defenceReduced = initialDefence - currentDefence
        if (defenceReduced >= defenceReductionThreshold) {
            return false
        }
        
        // Don't use BGS if health is below 50% threshold
        val healthPercentage = baba.health.value.toDouble() / initialHealth.toDouble()
        if (healthPercentage <= healthThreshold) {
            return false
        }
        
        return true
    }
}

// Future Baba strategies can be added here:
// class BabaShadowFightStrategy(private val baba: BabaBoss) : SpecStrategy { ... }
// class BabaSpeedRunStrategy(private val baba: BabaBoss) : SpecStrategy { ... }

class BabaBoss(private val combatEntity: CombatEntity): CombatEntity by combatEntity {
    private var baboonThrowers = mutableListOf<BaboonThrower>()
    private var boulderPhaseStartTick: Tick? = null
    private val boulderPhaseDuration = 21
    private var boulderPhasesTriggered = mutableSetOf<Int>() // Track which phases have been triggered
    private val initialScaledHp = health.value // Capture initial scaled HP
    
    fun isAttackable(tick: Tick): Boolean {
        // Check if we're in a boulder phase
        if (boulderPhaseStartTick != null) {
            val ticksInBoulderPhase = tick - boulderPhaseStartTick!!
            if (ticksInBoulderPhase.value < boulderPhaseDuration) {
                return false // Still in boulder phase
            } else {
                // Boulder phase ended
                boulderPhaseStartTick = null
            }
        }
        
        // Check for new boulder phase triggers using initial scaled HP
        val currentHpPercentage = (health.value.toDouble() / initialScaledHp.toDouble()) * 100
        
        // 66% threshold
        if (currentHpPercentage <= 66.0 && !boulderPhasesTriggered.contains(66)) {
            boulderPhaseStartTick = tick
            boulderPhasesTriggered.add(66)
            println("Baba entered boulder phase at 66% HP (${health.value}/${initialScaledHp}) on tick ${tick.value}")
            return false
        }
        
        // 33% threshold
        if (currentHpPercentage <= 33.0 && !boulderPhasesTriggered.contains(33)) {
            boulderPhaseStartTick = tick
            boulderPhasesTriggered.add(33)
            println("Baba entered boulder phase at 33% HP (${health.value}/${initialScaledHp}) on tick ${tick.value}")
            return false
        }
        
        return isAlive
    }
    
    fun shouldZcbSpec(): Boolean {
        // Use ZCB when health is >= 500 (including exactly 500)
        return health.value >= 500
    }
    
    fun shouldVoidwakerSpec(): Boolean {
        // Use Voidwaker when health is below 500 (not at 500)
        return health.value < 500
    }
    
    fun maybeSpawnBaboonThrowers(tick: Tick) {
        if (tick.value == 37) {
            // Do not clear existing throwers
            baboonThrowers.add(BaboonThrower())
            baboonThrowers.add(BaboonThrower())
            println("spawned 2 baboon throwers on tick ${tick.value}")
        }
    }
    
    fun getBaboonThrowers(): List<BaboonThrower> = baboonThrowers
}

class BaboonThrower: CombatEntity by GenericCombatEntity(
    name = "Baboon Thrower",
    health = Health(1), // 1 HP, will die from any damage
    combatStats = DefaultCombatStats(
        defenceLevel = 1,
        magicLevel = 1,
        meleeSlashDefenceBonus = 0,
        meleeStabDefenceBonus = 0,
        rangedDefenceBonus = 0,
        magicDefenceBonus = 0
    )
) 