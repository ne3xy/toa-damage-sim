package com.osrs.toa.simulations

import com.osrs.toa.*
import com.osrs.toa.actors.*
import com.osrs.toa.sims.Zebak
import com.osrs.toa.sims.ZebakBoss
import com.osrs.toa.sims.ZebakConstants
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ZebakAppTest {
    
    @Test
    fun `test Zebak with Lightbearer and surge pots`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = true,
            useSurgePots = true,
            useLiquidAdrenaline = false
        )
        println("Zebak Lightbearer + Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak with Lightbearer, no surge pots`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = true,
            useSurgePots = false,
            useLiquidAdrenaline = false
        )
        println("Zebak Lightbearer + No Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak with Lightbearer, surge pots, and liquid adrenaline`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = true,
            useSurgePots = true,
            useLiquidAdrenaline = true
        )
        println("Zebak Lightbearer + Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak with Lightbearer, no surge pots, but with liquid adrenaline`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = true,
            useSurgePots = false,
            useLiquidAdrenaline = true
        )
        println("Zebak Lightbearer + No Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak without Lightbearer, with surge pots`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = false,
            useSurgePots = true,
            useLiquidAdrenaline = false
        )
        println("Zebak No Lightbearer + Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak without Lightbearer, no surge pots`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = false,
            useSurgePots = false,
            useLiquidAdrenaline = false
        )
        println("Zebak No Lightbearer + No Surge Pots: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak without Lightbearer, surge pots, and liquid adrenaline`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = false,
            useSurgePots = true,
            useLiquidAdrenaline = true
        )
        println("Zebak No Lightbearer + Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Zebak without Lightbearer, no surge pots, but with liquid adrenaline`() {
        val results = simulateZebakFights(
            iterations = 1000,
            hasLightbearer = false,
            useSurgePots = false,
            useLiquidAdrenaline = true
        )
        println("Zebak No Lightbearer + No Surge Pots + Liquid Adrenaline: ${results.averageTicks} ticks")
        assertTrue(results.averageTicks > 0)
    }
    
    private data class SimulationResults(
        val totalTicks: Int,
        val iterations: Int,
        val averageTicks: Int
    )
    
    private fun simulateZebakFights(
        iterations: Int,
        hasLightbearer: Boolean,
        useSurgePots: Boolean,
        useLiquidAdrenaline: Boolean
    ): SimulationResults {
        var totalTicks = 0
        
        repeat(iterations) { iteration ->
            val player = createPlayer(hasLightbearer, useSurgePots, useLiquidAdrenaline)
            val zebakBoss = createZebakBoss()
            val loadout = createLoadout(player, zebakBoss)
            val monster = Zebak(loadout, zebakBoss)
            val simulator = CombatSimulator(player, monster)
            
            val fightLength = simulator.runSimulation()
            totalTicks += fightLength.value
        }
        
        return SimulationResults(
            totalTicks = totalTicks,
            iterations = iterations,
            averageTicks = totalTicks / iterations
        )
    }
    
    private fun createPlayer(hasLightbearer: Boolean, useSurgePots: Boolean, useLiquidAdrenaline: Boolean): Player {
        return Player(
            GenericCombatEntity(
                health = Health(99),
                name = "Player",
                hasLightbearer = hasLightbearer
            ),
            useSurgePots = useSurgePots,
            useLiquidAdrenaline = useLiquidAdrenaline
        )
    }
    
    private fun createZebakBoss(): ZebakBoss {
        return ZebakBoss(ToaCombatEntity(
            name = "Zebak",
            baseHp = ZebakConstants.BASE_HP,
            invocationLevel = 500,
            pathLevel = 3,
            baseCombatStats = DefenceDrainCappedCombatStats(DefaultCombatStats(
                defenceLevel = 70,
                magicLevel = 100,
                meleeSlashDefenceBonus = 160,
                rangedDefenceBonus = 110,
                magicDefenceBonus = 200
            ), drainCap = 20)
        ))
    }
    
    private fun createLoadout(player: Player, zebakBoss: ZebakBoss): PlayerLoadout {
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.Zebak6WayTwistedBow
            override val strategy = ZebakMainFightStrategy(zebakBoss)
        }
    }
} 