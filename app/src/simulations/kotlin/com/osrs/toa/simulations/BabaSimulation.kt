package com.osrs.toa.simulations

import com.osrs.toa.*
import com.osrs.toa.actors.*
import com.osrs.toa.sims.Baba
import com.osrs.toa.sims.BabaBoss
import com.osrs.toa.sims.BabaConstants
import com.osrs.toa.sims.BabaMainFightStrategy
import com.osrs.toa.weapons.Weapons
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.osrs.toa.simulations.DEFAULT_ITERATIONS

class BabaAppTest {
    
    @Test
    fun `test Baba with Lightbearer and surge pots`() {
        val results = simulateBabaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = true
        )
        println("Baba Lightbearer + Surge Pots: ${results.averageTicks} ticks")
        println("Spec usage: ${results.specCounts}")
        println("Average specs per weapon: ${results.averageSpecsPerWeapon}")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Baba with Lightbearer, no surge pots`() {
        val results = simulateBabaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = true,
            useSurgePots = false
        )
        println("Baba Lightbearer + No Surge Pots: ${results.averageTicks} ticks")
        println("Spec usage: ${results.specCounts}")
        println("Average specs per weapon: ${results.averageSpecsPerWeapon}")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Baba with Ultor and surge pots`() {
        val results = simulateBabaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = true
        )
        println("Baba Ultor + Surge Pots: ${results.averageTicks} ticks")
        println("Spec usage: ${results.specCounts}")
        println("Average specs per weapon: ${results.averageSpecsPerWeapon}")
        assertTrue(results.averageTicks > 0)
    }
    
    @Test
    fun `test Baba with Ultor, no surge pots`() {
        val results = simulateBabaFights(
            iterations = DEFAULT_ITERATIONS,
            hasLightbearer = false,
            useSurgePots = false
        )
        println("Baba Ultor + No Surge Pots: ${results.averageTicks} ticks")
        println("Spec usage: ${results.specCounts}")
        println("Average specs per weapon: ${results.averageSpecsPerWeapon}")
        assertTrue(results.averageTicks > 0)
    }
    
    private data class SimulationResults(
        val totalTicks: Int,
        val iterations: Int,
        val averageTicks: Int,
        val specCounts: Map<String, Int>,
        val averageSpecsPerWeapon: Map<String, Double>
    )
    
    private fun simulateBabaFights(
        iterations: Int,
        hasLightbearer: Boolean,
        useSurgePots: Boolean
    ): SimulationResults {
        var totalTicks = 0
        val specTracker = SpecTracker()
        
        repeat(iterations) { iteration ->
            val player = createPlayer(hasLightbearer, useSurgePots, specTracker)
            val babaBoss = createBabaBoss()
            val loadout = createBabaLoadout(player, babaBoss)
            val monster = Baba(loadout, babaBoss)
            val simulator = CombatSimulator(player, monster)
            
            val fightLength = simulator.runSimulation()
            totalTicks += fightLength.value
        }
        
        return SimulationResults(
            totalTicks = totalTicks,
            iterations = iterations,
            averageTicks = totalTicks / iterations,
            specCounts = specTracker.getAllSpecCounts(),
            averageSpecsPerWeapon = specTracker.getAverageSpecsPerWeapon(iterations)
        )
    }
    
    private fun createPlayer(hasLightbearer: Boolean, useSurgePots: Boolean, specTracker: SpecTracker): Player {
        return Player(
            GenericCombatEntity(
                health = Health(99),
                name = "Player",
                hasLightbearer = hasLightbearer
            ),
            useSurgePots = useSurgePots,
            useLiquidAdrenaline = false, // Baba doesn't use liquid adrenaline
            specTracker = specTracker
        )
    }
    
    private fun createBabaBoss(): BabaBoss {
        return BabaBoss(ToaCombatEntity(
            name = "Baba",
            baseHp = BabaConstants.BASE_HP,
            invocationLevel = 500,
            pathLevel = 0,
            baseCombatStats = DefenceDrainCappedCombatStats(DefaultCombatStats(
                defenceLevel = 70,
                magicLevel = 100,
                meleeSlashDefenceBonus = 160,
                rangedDefenceBonus = 110,
                magicDefenceBonus = 200
            ), drainCap = 20)
        ))
    }
    
    private fun createBabaLoadout(player: Player, babaBoss: BabaBoss): PlayerLoadout {
        val weapon = if (player.hasLightbearer) Weapons.LightbearerFang else Weapons.UltorFang
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = weapon
            override val strategy = BabaMainFightStrategy(babaBoss)
        }
    }
} 