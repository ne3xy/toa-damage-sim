package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.CombatStats
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.ToaMonsterCombatStats
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.PlayerLoadout
import kotlin.math.min
import com.osrs.toa.actors.ToaCombatEntity

object AkkhaConstants {
    const val BASE_HP = 400
    const val SHADOW_BASE_HP = 70
}

class Akkha(
        private val loadout: PlayerLoadout,
        specStrategy: SpecStrategy? = null,
        private val invocationLevel: Int,
        private val pathLevel: Int
): BossFight {
    // Calculate scaled HP based on invocation and path level
    private val scaledHp = ToaCombatEntity.calculateScaledHp(AkkhaConstants.BASE_HP, invocationLevel, pathLevel)
    
    val akkha = AkkhaBoss(
        GenericCombatEntity(
            name = "$invocationLevel Level $pathLevel Akkha",
            health = Health(scaledHp),
            combatStats = ToaMonsterCombatStats(DefaultCombatStats(
                defenceLevel = 80, // Level 3 Akkha defence level
                magicLevel = 100, // Level 3 Akkha magic level
                rangedDefenceBonus = 60,
                rangedHeavyDefenceBonus = 60,
                magicDefenceBonus = 10
            ), invocationLevel = invocationLevel)
        ),
        pathLevel = pathLevel,
        invocationLevel = invocationLevel
    )
    
    // Use provided strategy or default to AkkhaMainFightStrategy
    private val specStrategy = specStrategy ?: AkkhaMainFightStrategy(akkha)

    override fun onTick(tick: Tick) {
        if (akkha.isAttackable(tick)) {
            if (loadout.player.specialAttackEnergy.energy < 50) {
                loadout.player.drinkSurgePot(tick)
            }
            val (normalWeapon, specWeapon, shouldSpec) = specStrategy.selectWeapons(tick, loadout.mainWeapon)
            if (shouldSpec && tick.value != 0) {
                loadout.player.drinkLiquidAdrenaline(tick)
            }
            loadout.player.attack(tick, akkha, normalWeapon, specWeapon, shouldSpec)
            akkha.maybeProcShadow(tick)
        } else if (akkha.shadow?.isAttackable(tick) == true) {
            loadout.player.attack(tick, akkha.shadow!!, loadout.mainWeapon, null, false)
            if (!akkha.shadow!!.isAlive) {
                loadout.player.specialAttackEnergy.regenerate(15)
            }
        }
    }

    override fun isFightOver() = !akkha.isAlive
}

class AkkhaMainFightStrategy(private val akkha: AkkhaBoss) : SpecStrategy {
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        val normalWeapon = mainWeapon
        val specWeapon = Weapons.ZaryteCrossbow
        val shouldSpec = tick.value != 0 && akkha.shouldZcbSpec()
        
        return Triple(normalWeapon, specWeapon, shouldSpec)
    }
}

class AkkhaBoss(
    private val combatEntity: CombatEntity,
    private val pathLevel: Int,
    private val invocationLevel: Int
): CombatEntity by combatEntity {
    private val maxHealth = combatEntity.health.value
    private val phaseSize = maxHealth / 5
    private var lastMemoryEnded = Tick(0)
    private lateinit var thisMemoryEnds: Tick

    var shadow: AkkhaShadow? = null
        private set

    private val maxDamageToCap: Int
        get() = if (hpIsTopOfThePhase()) phaseSize else health.value % phaseSize

    fun isAttackable(tick: Tick): Boolean {
        return shadow?.isAlive != true && !isInMemory(tick)
    }

    private fun isInMemory(tick: Tick): Boolean {
        // Note: Path level is currently hardcoded to 3 but will be made configurable in a future update
        // Memory phase length: numberOfMemories * 4 ticks + 1 tick
        // Each memory consists of: 2 ticks per symbol + 2 ticks per explosion = 4 ticks total
        // For path level 3: (4 + 3/2) * 4 + 1 = 5 * 4 + 1 = 21 ticks
        val numberOfMemories = 4 + (pathLevel / 2)
        val nextMemory = lastMemoryEnded + Tick(101)
        thisMemoryEnds = nextMemory + Tick(numberOfMemories * 4)
        val inMemory = tick > nextMemory && tick < nextMemory + Tick((numberOfMemories * 4) + 1)
        if (inMemory) println("in memory on tick $tick")
        if (thisMemoryEnds < tick) {
            lastMemoryEnded = thisMemoryEnds
        }
        return inMemory
    }

    override fun takeDamage(damage: Int): CombatEntity {

        return combatEntity.takeDamage(min(maxDamageToCap, damage))
    }

    private fun hpIsTopOfThePhase() = (shadow?.bossHpProccedAt ?: maxHealth) == health.value

    fun maybeProcShadow(tick: Tick) {
        if (isAlive && health.value % phaseSize == 0 && !hpIsTopOfThePhase()) {
            val scaledShadowHp = ToaCombatEntity.calculateScaledHp(AkkhaConstants.SHADOW_BASE_HP, invocationLevel, pathLevel)
            shadow = AkkhaShadow(
                    GenericCombatEntity(
                            name = "$invocationLevel Level $pathLevel Akkha's Shadow",
                            health = Health(scaledShadowHp),
                            combatStats = ToaMonsterCombatStats(DefaultCombatStats(
                                defenceLevel = 30, // Shadow defence level
                                magicLevel = 100, // Shadow magic level
                                rangedDefenceBonus = 60,
                                rangedHeavyDefenceBonus = 60,
                                magicDefenceBonus = 10
                            ), invocationLevel = invocationLevel)
                    ),
                    attackableOn = Tick(tick.value + 6),
                    bossHpProccedAt = health.value
            )
        }
    }

    fun shouldZcbSpec(): Boolean {
        if (health.value >= 500) {
            // maybe fine tune this idk about wasted death charge, overcapping spec during shadows or memories
            if (maxDamageToCap > 110) return true
        } else {
            // kindof arbitrary cutoff, fang specs give 7 damage in enrage so say need 21 more damage than shadow expected hit to zcb spec
            // at 320 hp expected hit is 57, expected shadow hit is 35.4 (6way range, no magus, 530 akkha) 🤷‍♀️
            return health.value > 320
        }
        return false
    }
}

class AkkhaShadow(combatEntity: CombatEntity, private val attackableOn: Tick, val bossHpProccedAt: Number): CombatEntity by combatEntity {
    fun isAttackable(tick: Tick): Boolean {
        return tick >= attackableOn && isAlive
    }
}
