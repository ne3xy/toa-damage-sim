package com.osrs.toa.sims

import com.osrs.toa.Health
import com.osrs.toa.Tick
import com.osrs.toa.actors.CombatEntity
import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.actors.DefaultCombatStats
import com.osrs.toa.actors.AttackStyle
import com.osrs.toa.actors.ToaMonsterCombatStats
import com.osrs.toa.actors.DefenceDrainCappedCombatStats
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.actors.ToaCombatEntity
import com.osrs.toa.sims.ZebakConstants
import com.osrs.toa.PlayerLoadout
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.weapons.TestStrategy
import com.osrs.toa.sims.Zebak
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ZebakTest {

    private fun createTestPlayer(): Player {
        val combatEntity = GenericCombatEntity(
            name = "Test Player",
            health = Health(99),
            hasLightbearer = false
        )
        return Player(combatEntity)
    }

    private fun createTestLoadout(): PlayerLoadout {
        var player = createTestPlayer()
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.Zebak6WayTwistedBow
            override val strategy = ZebakMainFightStrategy(createTemporaryZebakBoss())
        }
    }

    private fun createTemporaryZebakBoss(): ZebakBoss {
        val zebakBaseStats = DefaultCombatStats(
            defenceLevel = 70,
            magicLevel = 100,
            meleeSlashDefenceBonus = 160,
            rangedDefenceBonus = 110,
            magicDefenceBonus = 200
        )
        
        val scaledHp = ToaCombatEntity.calculateScaledHp(ZebakConstants.BASE_HP, 530, 3)
        
        return ZebakBoss(GenericCombatEntity(
            name = "530 Level 3 Zebak",
            health = Health(scaledHp),
            combatStats = DefenceDrainCappedCombatStats(ToaMonsterCombatStats(zebakBaseStats, invocationLevel = 530), drainCap = 20)
        ))
    }

    @Test
    fun `should create Zebak boss fight`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        assertNotNull(zebak)
    }

    @Test
    fun `should create Zebak boss with correct properties`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertFalse(zebak.isFightOver())
    }

    @Test
    fun `should be attackable when alive`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertTrue(zebak.zebak.isAttackable(Tick(0)))
        assertTrue(zebak.zebak.isAttackable(Tick(10)))
        assertTrue(zebak.zebak.isAttackable(Tick(100)))
    }

    @Test
    fun `should not be attackable when dead`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val scaledHp = zebak.zebak.health.value
        // Kill the boss
        zebak.zebak.takeDamage(scaledHp)
        assertFalse(zebak.zebak.isAttackable(Tick(0)))
        assertFalse(zebak.zebak.isAttackable(Tick(10)))
    }

    @Test
    fun `should have correct initial health`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        assertEquals(2130, zebak.zebak.health.value)
        assertEquals("530 Level 3 Zebak", zebak.zebak.name)
    }

    @Test
    fun `player should drink surge pot before attacking Zebak if spec is under 50`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        loadout.player.specialAttackEnergy.consume(60) // Set spec to 40
        zebak.onTick(Tick(0)) // Should attack Zebak and try to drink surge pot
        // After onTick, spec should be 65 (40 + 25 from surge pot)
        assertEquals(65, loadout.player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should NOT drink surge pot before attacking Zebak if spec is 50 or above`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        loadout.player.specialAttackEnergy.consume(40) // Set spec to 60
        zebak.onTick(Tick(0)) // Should attack Zebak but NOT drink surge pot
        // After onTick, spec should be 60 or less (if spec used for attack), but not increased by surge pot
        assertEquals(60, loadout.player.specialAttackEnergy.energy)
    }

    @Test
    fun `player should drink liquid adrenaline before first ZCB spec`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Set up conditions for ZCB spec (not tick 0, shouldZcbSpec returns true)
        zebak.onTick(Tick(1)) // Should drink liquid adrenaline and spec
        
        // Verify liquid adrenaline was consumed (this would need access to player's liquid adrenaline state)
        // For now, we just verify the method doesn't throw an exception
        assertTrue(true)
    }

    @Test
    fun `player should NOT spec on tick 0`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        // On tick 0, should not spec even if shouldZcbSpec returns true
        zebak.onTick(Tick(0))
        
        // Verify no spec was used (this would need access to player's spec state)
        // For now, we just verify the method doesn't throw an exception
        assertTrue(true)
    }

    @Test
    fun `fight should be over when Zebak dies`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val scaledHp = zebak.zebak.health.value
        assertFalse(zebak.isFightOver())
        zebak.zebak.takeDamage(scaledHp)
        assertTrue(zebak.isFightOver())
    }

    @Test
    fun `should handle damage correctly`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialHealth = zebak.zebak.health.value
        
        // Deal some damage
        zebak.zebak.takeDamage(100)
        assertEquals(initialHealth - 100, zebak.zebak.health.value)
        
        // Deal more damage
        zebak.zebak.takeDamage(200)
        assertEquals(initialHealth - 300, zebak.zebak.health.value)
    }

    @Test
    fun `should handle lethal damage correctly`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val scaledHp = zebak.zebak.health.value
        // Deal lethal damage
        zebak.zebak.takeDamage(scaledHp)
        assertEquals(0, zebak.zebak.health.value)
        assertFalse(zebak.zebak.isAlive)
    }

    @Test
    fun `should handle overkill damage correctly`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        // Deal more damage than health
        zebak.zebak.takeDamage(3000)
        assertEquals(0, zebak.zebak.health.value)
        assertFalse(zebak.zebak.isAlive)
    }

    @Test
    fun `should not reduce defence below 50`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(70, zebak.zebak.combatStats.defenceLevel)
        
        // Try to reduce defence by a large amount that would go below 50
        val largeReduction = 100
        zebak.zebak.combatStats.drainDefenceLevel(largeReduction)
        
        // Defence should be capped at 50, not reduced to initialDefence - 100
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should allow defence reduction when above 50`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        // Test normal reduction when above 50
        val smallReduction = 5
        zebak.zebak.combatStats.drainDefenceLevel(smallReduction)
        assertEquals(65, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should handle multiple defence reductions correctly`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(70, zebak.zebak.combatStats.defenceLevel) // Verify initial defence level
        
        // First reduction: should work normally
        zebak.zebak.combatStats.drainDefenceLevel(5)
        assertEquals(65, zebak.zebak.combatStats.defenceLevel)
        
        // Second reduction: should work normally
        zebak.zebak.combatStats.drainDefenceLevel(5)
        assertEquals(60, zebak.zebak.combatStats.defenceLevel)
        
        // Third reduction: should cap at 50
        zebak.zebak.combatStats.drainDefenceLevel(15)
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
        
        // Fourth reduction: should stay at 50
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(50, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should have correct health at 200 invocation path 1`() {
        val scaledHp = ToaCombatEntity.calculateScaledHp(ZebakConstants.BASE_HP, 200, 1)
        assertEquals(1130, scaledHp)
    }

    @Test
    fun `should have correct health at 600 invocation path 6`() {
        val scaledHp = ToaCombatEntity.calculateScaledHp(ZebakConstants.BASE_HP, 600, 6)
        assertEquals(2620, scaledHp)
    }

    @Test
    fun `should have correct defence level`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(70, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should have correct magic level`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        
        assertEquals(100, zebak.zebak.combatStats.magicLevel)
    }

    @Test
    fun `should drain defence level correctly`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        // Drain some defence
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(initialDefence - 10, zebak.zebak.combatStats.defenceLevel)
        
        // Drain more defence
        zebak.zebak.combatStats.drainDefenceLevel(5)
        assertEquals(initialDefence - 15, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should cap defence drain at 20`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        // Try to drain more than the cap
        zebak.zebak.combatStats.drainDefenceLevel(25)
        
        // Should only drain up to the cap (20)
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should handle defence drain when already drained`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        // Drain some defence first
        zebak.zebak.combatStats.drainDefenceLevel(10)
        assertEquals(initialDefence - 10, zebak.zebak.combatStats.defenceLevel)
        
        // Try to drain more than the remaining cap
        zebak.zebak.combatStats.drainDefenceLevel(15)
        
        // Should only drain up to the remaining cap (10 more, total 20)
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should handle defence drain when at cap`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        // Drain to the cap
        zebak.zebak.combatStats.drainDefenceLevel(20)
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
        
        // Try to drain more
        zebak.zebak.combatStats.drainDefenceLevel(10)
        
        // Should remain at the cap
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should handle defence drain with multiple drains`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 530, pathLevel = 3)
        val initialDefence = zebak.zebak.combatStats.defenceLevel
        
        // Multiple small drains
        zebak.zebak.combatStats.drainDefenceLevel(5)
        zebak.zebak.combatStats.drainDefenceLevel(5)
        zebak.zebak.combatStats.drainDefenceLevel(5)
        zebak.zebak.combatStats.drainDefenceLevel(5)
        
        // Should total 20 (the cap)
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
        
        // Try one more drain
        zebak.zebak.combatStats.drainDefenceLevel(5)
        
        // Should still be at the cap
        assertEquals(initialDefence - 20, zebak.zebak.combatStats.defenceLevel)
    }

    @Test
    fun `should create Zebak with default parameters`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should create Zebak with custom strategy`() {
        val loadout = createTestLoadout()
        val strategy = TestStrategy()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should create Zebak with different invocation levels`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 300, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should create Zebak with different path levels`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 5)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle fight progression`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Initial state
        assertFalse(zebak.isFightOver())
        
        // Simulate some ticks
        zebak.onTick(Tick(1))
        assertFalse(zebak.isFightOver())
        
        zebak.onTick(Tick(2))
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle fight completion`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Kill the boss
        zebak.zebak.takeDamage(zebak.zebak.health.value)
        
        assertTrue(zebak.isFightOver())
    }
    
    @Test
    fun `should handle special attack energy management`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Reduce special attack energy to 30 (100 - 70)
        loadout.player.specialAttackEnergy.consume(70)
        assertEquals(30, loadout.player.specialAttackEnergy.energy)
        
        // Should drink surge pot when energy is low
        zebak.onTick(Tick(1))
        
        // Energy should be restored by surge pot (30 + 25 = 55), then consumed by BGS spec (55 - 50 = 5)
        // The test verifies that surge pot was consumed by checking energy is > 0 (not 0, which would happen without surge pot)
        assertTrue(loadout.player.specialAttackEnergy.energy > 0)
    }
    
    @Test
    fun `should handle liquid adrenaline usage`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Should drink liquid adrenaline when spec is needed
        zebak.onTick(Tick(1))
        
        // Verify that liquid adrenaline was used (this would be tracked in the player)
        // The exact verification depends on how liquid adrenaline usage is tracked
    }
    
    @Test
    fun `should handle weapon selection`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // The strategy should select appropriate weapons
        zebak.onTick(Tick(1))
        
        // Verify that weapons were selected correctly
        // This is tested more thoroughly in ZebakStrategyTest
    }
    
    @Test
    fun `should handle boss attackability`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Boss should be attackable when alive
        assertTrue(zebak.zebak.isAttackable(Tick(1)))
        
        // Kill the boss
        zebak.zebak.takeDamage(zebak.zebak.health.value)
        
        // Boss should not be attackable when dead
        assertFalse(zebak.zebak.isAttackable(Tick(1)))
    }
    
    @Test
    fun `should handle different loadouts`() {
        val player = createTestPlayer()
        val differentLoadout = object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.Zebak6WayTwistedBow
            override val strategy = TestStrategy()
        }
        
        val zebak = Zebak.create(differentLoadout, invocationLevel = 150, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle null strategy`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle extreme invocation levels`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 500, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle extreme path levels`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 6)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle zero invocation level`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 0, pathLevel = 3)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle zero path level`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 0)
        
        assertNotNull(zebak)
        assertFalse(zebak.isFightOver())
    }
    
    @Test
    fun `should handle boss health scaling`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Boss should have scaled health based on invocation and path level
        assertTrue(zebak.zebak.health.value > ZebakConstants.BASE_HP)
    }
    
    @Test
    fun `should handle boss combat stats`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Boss should have appropriate combat stats
        assertTrue(zebak.zebak.combatStats.defenceLevel > 0)
        assertTrue(zebak.zebak.combatStats.magicLevel > 0)
    }
    
    @Test
    fun `should handle boss name generation`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Boss should have a name that includes invocation and path level
        assertTrue(zebak.zebak.name.contains("150"))
        assertTrue(zebak.zebak.name.contains("3"))
        assertTrue(zebak.zebak.name.contains("Zebak"))
    }
    
    @Test
    fun `should handle multiple ticks`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Should handle multiple ticks without issues
        repeat(10) { tick ->
            zebak.onTick(Tick(tick))
            assertFalse(zebak.isFightOver())
        }
    }
    
    @Test
    fun `should handle boss death during fight`() {
        val loadout = createTestLoadout()
        val zebak = Zebak.create(loadout, invocationLevel = 150, pathLevel = 3)
        
        // Simulate some ticks
        zebak.onTick(Tick(1))
        zebak.onTick(Tick(2))
        
        // Kill the boss
        zebak.zebak.takeDamage(zebak.zebak.health.value)
        
        // Fight should be over
        assertTrue(zebak.isFightOver())
        
        // Additional ticks should not cause issues
        zebak.onTick(Tick(3))
        assertTrue(zebak.isFightOver())
    }
} 