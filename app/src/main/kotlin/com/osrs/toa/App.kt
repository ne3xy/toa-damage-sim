/*
 * This source file was generated by the Gradle 'init' task
 */
package com.osrs.toa

import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.ToaCombatEntity
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.DefenceDrainCappedCombatStats
import com.osrs.toa.actors.ToaMonsterCombatStats
import com.osrs.toa.Health
import com.osrs.toa.sims.Zebak
import com.osrs.toa.sims.ZebakBoss
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.sims.ZebakConstants
import com.osrs.toa.sims.Baba
import com.osrs.toa.sims.BabaBoss
import com.osrs.toa.sims.BabaMainFightStrategy
import com.osrs.toa.sims.BabaConstants
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.sims.Akkha
import com.osrs.toa.sims.AkkhaBoss
import com.osrs.toa.sims.AkkhaMainFightStrategy
import com.osrs.toa.sims.AkkhaConstants

fun main(args: Array<String>) {
    // Parse command line arguments
    val boss = args.getOrNull(0)?.lowercase()
    val iterations = args.getOrNull(1)?.toIntOrNull() ?: 1000
    
    println("Running simulations for $boss with $iterations iterations")
    println("Usage: <boss> <iterations>")
    println("Boss options: akkha, zebak, baba")
    println("Example: akkha 1000")
    println()
    
    when (boss) {
        "akkha" -> runAkkhaSimulations(iterations)
        "zebak" -> runZebakSimulations(iterations)
        "baba" -> runBabaSimulations(iterations)
        else -> {
            println("Unknown boss: $boss")
            println("Available bosses: akkha, zebak, baba")
        }
    }
}

private fun runAkkhaSimulations(iterations: Int) {
    println("=== AKKHA SIMULATIONS ===")
    
    // Lightbearer scenarios
    val lightbearerWithSurge = simulateAkkhaFights(iterations, true, true, false)
    val lightbearerNoSurge = simulateAkkhaFights(iterations, true, false, false)
    val lightbearerWithAdrenaline = simulateAkkhaFights(iterations, true, true, true)
    val lightbearerNoSurgeWithAdrenaline = simulateAkkhaFights(iterations, true, false, true)
    
    // Magus Ring scenarios
    val magusWithSurge = simulateAkkhaFights(iterations, false, true, false)
    val magusNoSurge = simulateAkkhaFights(iterations, false, false, false)
    val magusWithAdrenaline = simulateAkkhaFights(iterations, false, true, true)
    val magusNoSurgeWithAdrenaline = simulateAkkhaFights(iterations, false, false, true)
    
    println("Lightbearer + Surge Pots: ${lightbearerWithSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerWithSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerWithSurge.averageSpecsPerWeapon}")
    println("Lightbearer + No Surge Pots: ${lightbearerNoSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerNoSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerNoSurge.averageSpecsPerWeapon}")
    println("Lightbearer + Surge Pots + Liquid Adrenaline: ${lightbearerWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${lightbearerWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${lightbearerWithAdrenaline.averageSpecsPerWeapon}")
    println("Lightbearer + No Surge Pots + Liquid Adrenaline: ${lightbearerNoSurgeWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${lightbearerNoSurgeWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${lightbearerNoSurgeWithAdrenaline.averageSpecsPerWeapon}")
    println()
    println("Magus Ring + Surge Pots: ${magusWithSurge.averageTicks} ticks")
    println("  Spec usage: ${magusWithSurge.specCounts}")
    println("  Average specs per weapon: ${magusWithSurge.averageSpecsPerWeapon}")
    println("Magus Ring + No Surge Pots: ${magusNoSurge.averageTicks} ticks")
    println("  Spec usage: ${magusNoSurge.specCounts}")
    println("  Average specs per weapon: ${magusNoSurge.averageSpecsPerWeapon}")
    println("Magus Ring + Surge Pots + Liquid Adrenaline: ${magusWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${magusWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${magusWithAdrenaline.averageSpecsPerWeapon}")
    println("Magus Ring + No Surge Pots + Liquid Adrenaline: ${magusNoSurgeWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${magusNoSurgeWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${magusNoSurgeWithAdrenaline.averageSpecsPerWeapon}")
}

private fun runZebakSimulations(iterations: Int) {
    println("=== ZEBAK SIMULATIONS ===")
    
    // Lightbearer scenarios
    val lightbearerWithSurge = simulateZebakFights(iterations, true, true, false)
    val lightbearerNoSurge = simulateZebakFights(iterations, true, false, false)
    val lightbearerWithAdrenaline = simulateZebakFights(iterations, true, true, true)
    val lightbearerNoSurgeWithAdrenaline = simulateZebakFights(iterations, true, false, true)
    
    // No Lightbearer scenarios
    val noLightbearerWithSurge = simulateZebakFights(iterations, false, true, false)
    val noLightbearerNoSurge = simulateZebakFights(iterations, false, false, false)
    val noLightbearerWithAdrenaline = simulateZebakFights(iterations, false, true, true)
    val noLightbearerNoSurgeWithAdrenaline = simulateZebakFights(iterations, false, false, true)
    
    println("Lightbearer + Surge Pots: ${lightbearerWithSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerWithSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerWithSurge.averageSpecsPerWeapon}")
    println("Lightbearer + No Surge Pots: ${lightbearerNoSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerNoSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerNoSurge.averageSpecsPerWeapon}")
    println("Lightbearer + Surge Pots + Liquid Adrenaline: ${lightbearerWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${lightbearerWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${lightbearerWithAdrenaline.averageSpecsPerWeapon}")
    println("Lightbearer + No Surge Pots + Liquid Adrenaline: ${lightbearerNoSurgeWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${lightbearerNoSurgeWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${lightbearerNoSurgeWithAdrenaline.averageSpecsPerWeapon}")
    println()
    println("No Lightbearer + Surge Pots: ${noLightbearerWithSurge.averageTicks} ticks")
    println("  Spec usage: ${noLightbearerWithSurge.specCounts}")
    println("  Average specs per weapon: ${noLightbearerWithSurge.averageSpecsPerWeapon}")
    println("No Lightbearer + No Surge Pots: ${noLightbearerNoSurge.averageTicks} ticks")
    println("  Spec usage: ${noLightbearerNoSurge.specCounts}")
    println("  Average specs per weapon: ${noLightbearerNoSurge.averageSpecsPerWeapon}")
    println("No Lightbearer + Surge Pots + Liquid Adrenaline: ${noLightbearerWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${noLightbearerWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${noLightbearerWithAdrenaline.averageSpecsPerWeapon}")
    println("No Lightbearer + No Surge Pots + Liquid Adrenaline: ${noLightbearerNoSurgeWithAdrenaline.averageTicks} ticks")
    println("  Spec usage: ${noLightbearerNoSurgeWithAdrenaline.specCounts}")
    println("  Average specs per weapon: ${noLightbearerNoSurgeWithAdrenaline.averageSpecsPerWeapon}")
}

private fun runBabaSimulations(iterations: Int) {
    println("=== BABA SIMULATIONS ===")
    
    // Lightbearer scenarios
    val lightbearerWithSurge = simulateBabaFights(iterations, true, true)
    val lightbearerNoSurge = simulateBabaFights(iterations, true, false)
    
    // Ultor scenarios
    val ultorWithSurge = simulateBabaFights(iterations, false, true)
    val ultorNoSurge = simulateBabaFights(iterations, false, false)
    
    println("Lightbearer + Surge Pots: ${lightbearerWithSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerWithSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerWithSurge.averageSpecsPerWeapon}")
    println("Lightbearer + No Surge Pots: ${lightbearerNoSurge.averageTicks} ticks")
    println("  Spec usage: ${lightbearerNoSurge.specCounts}")
    println("  Average specs per weapon: ${lightbearerNoSurge.averageSpecsPerWeapon}")
    println("Ultor + Surge Pots: ${ultorWithSurge.averageTicks} ticks")
    println("  Spec usage: ${ultorWithSurge.specCounts}")
    println("  Average specs per weapon: ${ultorWithSurge.averageSpecsPerWeapon}")
    println("Ultor + No Surge Pots: ${ultorNoSurge.averageTicks} ticks")
    println("  Spec usage: ${ultorNoSurge.specCounts}")
    println("  Average specs per weapon: ${ultorNoSurge.averageSpecsPerWeapon}")
}

private data class SimulationResults(
    val totalTicks: Int,
    val iterations: Int,
    val averageTicks: Int,
    val specCounts: Map<String, Int>,
    val averageSpecsPerWeapon: Map<String, Double>
)

private fun simulateZebakFights(
    iterations: Int,
    hasLightbearer: Boolean,
    useSurgePots: Boolean,
    useLiquidAdrenaline: Boolean
): SimulationResults {
    var totalTicks = 0
    val specTracker = SpecTracker()
    
    repeat(iterations) { iteration ->
        val player = createPlayer(hasLightbearer, useSurgePots, useLiquidAdrenaline, specTracker)
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
        averageTicks = totalTicks / iterations,
        specCounts = specTracker.getAllSpecCounts(),
        averageSpecsPerWeapon = specTracker.getAverageSpecsPerWeapon(iterations)
    )
}

private fun simulateBabaFights(
    iterations: Int,
    hasLightbearer: Boolean,
    useSurgePots: Boolean
): SimulationResults {
    var totalTicks = 0
    val specTracker = SpecTracker()
    
    repeat(iterations) { iteration ->
        val player = createPlayer(hasLightbearer, useSurgePots, false, specTracker) // Baba doesn't use liquid adrenaline
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

private fun simulateAkkhaFights(
    iterations: Int,
    hasLightbearer: Boolean,
    useSurgePots: Boolean,
    useLiquidAdrenaline: Boolean
): SimulationResults {
    var totalTicks = 0
    val specTracker = SpecTracker()
    
    repeat(iterations) { iteration ->
        val player = createPlayer(hasLightbearer, useSurgePots, useLiquidAdrenaline, specTracker)
        val loadout = createAkkhaLoadout(player)
        val monster = Akkha(loadout, invocationLevel = 500, pathLevel = 2)
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

private fun createPlayer(hasLightbearer: Boolean, useSurgePots: Boolean, useLiquidAdrenaline: Boolean, specTracker: SpecTracker): Player {
    return Player(
        GenericCombatEntity(
            health = Health(99),
            name = "Player",
            hasLightbearer = hasLightbearer
        ),
        useSurgePots = useSurgePots,
        useLiquidAdrenaline = useLiquidAdrenaline,
        specTracker = specTracker
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

private fun createAkkhaBoss(): AkkhaBoss {
    return AkkhaBoss(ToaCombatEntity(
        name = "Akkha",
        baseHp = AkkhaConstants.BASE_HP,
        invocationLevel = 500,
        pathLevel = 2,
        baseCombatStats = DefaultCombatStats(
            defenceLevel = 80,
            magicLevel = 100,
            rangedDefenceBonus = 60,
            rangedHeavyDefenceBonus = 60,
            magicDefenceBonus = 10
        )
    ), pathLevel = 2, invocationLevel = 500)
}

private fun createAkkhaLoadout(player: Player): PlayerLoadout {
    return object : PlayerLoadout {
        override val player = player
        override val mainWeapon = if (player.hasLightbearer) Weapons.TumekensShadow else Weapons.MagussShadow
        override val strategy = AkkhaMainFightStrategy(createAkkhaBoss())
    }
}
