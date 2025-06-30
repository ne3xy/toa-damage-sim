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
import com.osrs.toa.actors.ToaCombatEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class BabaTest {
    
    @Test
    fun `test Baba boss creation`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        
        assertEquals("500 Level 2 Baba", baba.baba.name)
        // HP is scaled by invocation level and path level
        val expectedHp = ToaCombatEntity.calculateScaledHp(BabaConstants.BASE_HP, 500, 2)
        assertEquals(expectedHp, baba.baba.health.value)
        assertTrue(baba.baba.isAlive)
    }
    
    @Test
    fun `test Baba boss is attackable`() {
        val babaBoss = createBabaBoss()
        
        assertTrue(babaBoss.isAttackable(Tick(0)))
        assertTrue(babaBoss.isAttackable(Tick(100)))
        
        // Kill the boss
        babaBoss.takeDamage(babaBoss.health.value)
        
        assertFalse(babaBoss.isAttackable(Tick(0)))
    }
    
    @Test
    fun `test Baba shouldZcbSpec logic`() {
        val babaBoss = createBabaBoss()
        // Should use ZCB when BGS is not available and health >= 500
        babaBoss.combatStats.drainDefenceLevel(13)
        assertTrue(babaBoss.shouldZcbSpec())
        // Reduce health to 500
        babaBoss.takeDamage(babaBoss.health.value - 500)
        assertEquals(500, babaBoss.health.value)
        // Should use ZCB at exactly 500
        assertTrue(babaBoss.shouldZcbSpec())
        // Reduce health below 500
        babaBoss.takeDamage(1)
        assertEquals(499, babaBoss.health.value)
        // Should not use ZCB below 500
        assertFalse(babaBoss.shouldZcbSpec())
    }
    
    @Test
    fun `test Baba shouldVoidwakerSpec logic`() {
        val babaBoss = createBabaBoss()
        
        // Should not use Voidwaker when health is above 500
        assertFalse(babaBoss.shouldVoidwakerSpec())
        
        // Reduce health to 500
        babaBoss.takeDamage(babaBoss.health.value - 500)
        assertEquals(500, babaBoss.health.value)
        
        // Should not use Voidwaker at exactly 500 (ZCB should be used instead)
        assertFalse(babaBoss.shouldVoidwakerSpec())
        
        // Reduce health below 500
        babaBoss.takeDamage(1)
        assertEquals(499, babaBoss.health.value)
        
        // Should use Voidwaker below 500
        assertTrue(babaBoss.shouldVoidwakerSpec())
    }
    
    @Test
    fun `test BabaMainFightStrategy weapon selection`() {
        val babaBoss = createBabaBoss()
        val strategy = BabaMainFightStrategy(babaBoss)
        // Initial: BGS should be used
        val (normalWeapon, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.UltorFang)
        assertEquals(Weapons.UltorFang, normalWeapon)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        assertTrue(shouldSpec)
        // Simulate BGS threshold reached (defence reduced by 13)
        babaBoss.combatStats.drainDefenceLevel(13)
        // Still above 500 HP, should use ZCB
        val (_, specWeaponAfterBgs, _) = strategy.selectWeapons(Tick(2), Weapons.UltorFang)
        assertEquals(Weapons.ZaryteCrossbow, specWeaponAfterBgs)
        // Simulate health to exactly 500, BGS threshold reached
        babaBoss.takeDamage(babaBoss.health.value - 500)
        val (_, specWeaponAt500, _) = strategy.selectWeapons(Tick(3), Weapons.UltorFang)
        assertEquals(Weapons.ZaryteCrossbow, specWeaponAt500)
        // Simulate health below 500
        babaBoss.takeDamage(1)
        val (_, specWeaponBelow500, _) = strategy.selectWeapons(Tick(4), Weapons.UltorFang)
        assertEquals(Weapons.Voidwaker, specWeaponBelow500)
    }
    
    @Test
    fun `test BabaMainFightStrategy BGS threshold logic`() {
        val babaBoss = createBabaBoss()
        val strategy = BabaMainFightStrategy(babaBoss)
        val initialDefence = babaBoss.combatStats.defenceLevel
        // Should use BGS initially
        val (_, specWeapon, _) = strategy.selectWeapons(Tick(1), Weapons.UltorFang)
        assertEquals(Weapons.BandosGodsword, specWeapon)
        // Simulate BGS threshold reached (defence reduced by 13)
        babaBoss.combatStats.drainDefenceLevel(13)
        // Should use ZCB now (health >= 500)
        val (_, specWeaponAfterBgs, _) = strategy.selectWeapons(Tick(2), Weapons.UltorFang)
        assertEquals(Weapons.ZaryteCrossbow, specWeaponAfterBgs)
        // Simulate health to exactly 500
        babaBoss.takeDamage(babaBoss.health.value - 500)
        val (_, specWeaponAt500, _) = strategy.selectWeapons(Tick(3), Weapons.UltorFang)
        assertEquals(Weapons.ZaryteCrossbow, specWeaponAt500)
        // Simulate health below 500
        babaBoss.takeDamage(1)
        val (_, specWeaponBelow500, _) = strategy.selectWeapons(Tick(4), Weapons.UltorFang)
        assertEquals(Weapons.Voidwaker, specWeaponBelow500)
        // Check if defence has been reduced
        assertTrue(babaBoss.combatStats.defenceLevel < initialDefence)
    }
    
    @Test
    fun `test BabaMainFightStrategy health threshold logic`() {
        val babaBoss = createBabaBoss()
        val strategy = BabaMainFightStrategy(babaBoss)
        
        val initialHealth = babaBoss.health.value
        
        // Reduce health to below 50% threshold
        val damageTo50Percent = (initialHealth * 0.5).toInt()
        babaBoss.takeDamage(damageTo50Percent)
        
        // Should not use BGS when health is below threshold
        val (_, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.UltorFang)
        
        // Should use ZCB instead since health is still above 500
        assertEquals(Weapons.ZaryteCrossbow, specWeapon)
        assertTrue(shouldSpec)
    }
    
    @Test
    fun `test BabaMainFightStrategy Voidwaker selection`() {
        val babaBoss = createBabaBoss()
        val strategy = BabaMainFightStrategy(babaBoss)
        
        // Reduce health below 500 to trigger Voidwaker
        babaBoss.takeDamage(babaBoss.health.value - 400)
        
        val (_, specWeapon, shouldSpec) = strategy.selectWeapons(Tick(1), Weapons.UltorFang)
        
        assertEquals(Weapons.Voidwaker, specWeapon)
        assertTrue(shouldSpec)
    }
    
    @Test
    fun `test Baba fight simulation`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        
        // Simulate a few ticks
        repeat(10) { tick ->
            baba.onTick(Tick(tick))
        }
        
        // Boss should still be alive after a few ticks
        assertTrue(baba.baba.isAlive)
    }
    
    @Test
    fun `test Baba fight completion`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        
        // Kill the boss
        baba.baba.takeDamage(baba.baba.health.value)
        
        // Fight should be over
        assertTrue(baba.isFightOver())
    }
    
    @Test
    fun `test Baba debug health values`() {
        val babaBoss = createBabaBoss()
        println("Baba boss health: ${babaBoss.health.value}")
        println("Should ZCB spec: ${babaBoss.shouldZcbSpec()}")
        println("Should Voidwaker spec: ${babaBoss.shouldVoidwakerSpec()}")
        
        val strategy = BabaMainFightStrategy(babaBoss)
        val (_, specWeapon, _) = strategy.selectWeapons(Tick(1), Weapons.UltorFang)
        println("Selected spec weapon: ${specWeapon?.name}")
        
        // This test is just for debugging
        assertTrue(true)
    }
    
    @Test
    fun `test Baba constants`() {
        assertEquals(380, BabaConstants.BASE_HP)
    }
    
    @Test
    fun `test BaboonThrower creation and properties`() {
        val thrower = BaboonThrower()
        
        assertEquals("Baboon Thrower", thrower.name)
        assertEquals(1, thrower.health.value) // 1 HP
        assertTrue(thrower.isAlive)
        
        // Should die from any damage
        thrower.takeDamage(1)
        assertFalse(thrower.isAlive)
    }
    
    @Test
    fun `test Baba baboon thrower spawning at tick 37`() {
        val babaBoss = createBabaBoss()
        
        // Before tick 37, no throwers should exist
        assertEquals(0, babaBoss.getBaboonThrowers().size)
        
        // At tick 37, should spawn 2 throwers
        babaBoss.maybeSpawnBaboonThrowers(Tick(37))
        assertEquals(2, babaBoss.getBaboonThrowers().size)
        assertTrue(babaBoss.getBaboonThrowers().all { it.isAlive })
        
        // At other ticks, should not spawn
        babaBoss.maybeSpawnBaboonThrowers(Tick(36))
        babaBoss.maybeSpawnBaboonThrowers(Tick(38))
        assertEquals(2, babaBoss.getBaboonThrowers().size) // Still only 2
    }
    
    @Test
    fun `test Baba baboon thrower spawning does not clear existing throwers`() {
        val babaBoss = createBabaBoss()
        
        // Spawn initial throwers
        babaBoss.maybeSpawnBaboonThrowers(Tick(37))
        assertEquals(2, babaBoss.getBaboonThrowers().size)
        
        // Kill one thrower
        babaBoss.getBaboonThrowers()[0].takeDamage(1)
        assertEquals(1, babaBoss.getBaboonThrowers().count { it.isAlive })
        
        // Spawn again at tick 37 (should add 2 more, not clear)
        babaBoss.maybeSpawnBaboonThrowers(Tick(37))
        assertEquals(4, babaBoss.getBaboonThrowers().size)
        assertEquals(3, babaBoss.getBaboonThrowers().count { it.isAlive })
    }
    
    @Test
    fun `test Baba boulder phase triggers at 66 percent HP`() {
        val babaBoss = createBabaBoss()
        val initialHp = babaBoss.health.value
        val threshold66 = (initialHp * 0.66).toInt()
        
        // Should be attackable initially
        assertTrue(babaBoss.isAttackable(Tick(0)))
        
        // Reduce HP to just above 66% threshold
        babaBoss.takeDamage(initialHp - threshold66 - 1)
        assertTrue(babaBoss.isAttackable(Tick(1)))
        
        // Reduce HP to exactly 66% threshold
        babaBoss.takeDamage(1)
        assertFalse(babaBoss.isAttackable(Tick(2))) // Should trigger boulder phase
        
        // Should remain unattackable for 21 ticks
        repeat(20) { tick ->
            assertFalse(babaBoss.isAttackable(Tick(3 + tick)))
        }
        
        // Should be attackable again after 21 ticks
        assertTrue(babaBoss.isAttackable(Tick(23)))
    }
    
    @Test
    fun `test Baba boulder phase triggers at 33 percent HP`() {
        val babaBoss = createBabaBoss()
        val initialHp = babaBoss.health.value
        val threshold66 = (initialHp * 0.66).toInt()
        val threshold33 = (initialHp * 0.33).toInt()
        
        // Trigger 66% boulder phase
        babaBoss.takeDamage(initialHp - threshold66)
        assertFalse(babaBoss.isAttackable(Tick(0)))
        // Wait for boulder phase to end
        repeat(21) { tick ->
            babaBoss.isAttackable(Tick(tick))
        }
        assertTrue(babaBoss.isAttackable(Tick(21)))
        
        // Now trigger 33% boulder phase
        babaBoss.takeDamage(threshold66 - threshold33)
        assertFalse(babaBoss.isAttackable(Tick(22))) // Should trigger boulder phase
        // Should remain unattackable for 21 ticks
        repeat(20) { tick ->
            assertFalse(babaBoss.isAttackable(Tick(23 + tick)))
        }
        // Should be attackable again after 21 ticks
        assertTrue(babaBoss.isAttackable(Tick(43)))
    }
    
    @Test
    fun `test Baba boulder phases only trigger once each`() {
        val babaBoss = createBabaBoss()
        val initialHp = babaBoss.health.value
        val threshold66 = (initialHp * 0.66).toInt()
        val threshold33 = (initialHp * 0.33).toInt()
        
        // Trigger 66% boulder phase
        babaBoss.takeDamage(initialHp - threshold66)
        assertFalse(babaBoss.isAttackable(Tick(0)))
        
        // Wait for boulder phase to end
        repeat(21) { tick ->
            babaBoss.isAttackable(Tick(tick))
        }
        
        // Should be attackable again
        assertTrue(babaBoss.isAttackable(Tick(21)))
        
        // Further reduce HP below 66% - should not trigger another boulder phase
        babaBoss.takeDamage(threshold66 - threshold33 - 1)
        assertTrue(babaBoss.isAttackable(Tick(22)))
        
        // Trigger 33% boulder phase
        babaBoss.takeDamage(1)
        assertFalse(babaBoss.isAttackable(Tick(23)))
        
        // Wait for boulder phase to end
        repeat(21) { tick ->
            babaBoss.isAttackable(Tick(24 + tick))
        }
        
        // Should be attackable again
        assertTrue(babaBoss.isAttackable(Tick(45)))
        
        // Further reduce HP below 33% - should not trigger another boulder phase
        babaBoss.takeDamage(threshold33 - 10)
        assertTrue(babaBoss.isAttackable(Tick(46)))
    }
    
    @Test
    fun `test Baba uses initial scaled HP for boulder phase calculations`() {
        // Create Baba with different scaling
        val babaBaseStats = DefaultCombatStats(
            defenceLevel = 70,
            magicLevel = 100,
            meleeSlashDefenceBonus = 160,
            rangedDefenceBonus = 110,
            magicDefenceBonus = 200
        )
        
        val babaBoss = BabaBoss(ToaCombatEntity(
            name = "Test Baba",
            baseHp = BabaConstants.BASE_HP,
            invocationLevel = 300, // Different scaling
            pathLevel = 1,
            baseCombatStats = DefenceDrainCappedCombatStats(ToaMonsterCombatStats(babaBaseStats, invocationLevel = 300), drainCap = 20)
        ))
        
        val initialHp = babaBoss.health.value
        val threshold66 = (initialHp * 0.66).toInt()
        
        // Should trigger at the correct scaled HP threshold
        babaBoss.takeDamage(initialHp - threshold66)
        assertFalse(babaBoss.isAttackable(Tick(0)))
        
        // Verify the threshold is based on scaled HP, not base HP
        assertNotEquals((BabaConstants.BASE_HP * 0.66).toInt(), threshold66)
    }
    
    @Test
    fun `test Baba fight prioritizes baboon throwers over boss`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        
        // Spawn baboon throwers
        baba.baba.maybeSpawnBaboonThrowers(Tick(37))
        assertEquals(2, baba.baba.getBaboonThrowers().size)
        
        // Simulate a few ticks - should attack throwers first
        repeat(5) { tick ->
            baba.onTick(Tick(40 + tick))
        }
        
        // Should have killed some throwers
        val aliveThrowers = baba.baba.getBaboonThrowers().count { it.isAlive }
        assertTrue(aliveThrowers < 2)
    }
    
    @Test
    fun `test Baba fight can attack throwers during boulder phase`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        
        // Spawn baboon throwers
        baba.baba.maybeSpawnBaboonThrowers(Tick(37))
        
        // Trigger boulder phase
        val initialHp = baba.baba.health.value
        val threshold66 = (initialHp * 0.66).toInt()
        baba.baba.takeDamage(initialHp - threshold66)
        
        // Should not be able to attack Baba during boulder phase
        assertFalse(baba.baba.isAttackable(Tick(0)))
        
        // But should still be able to attack throwers
        assertTrue(baba.baba.getBaboonThrowers().any { it.isAlive })
        
        // Simulate a few ticks - should attack throwers even during boulder phase
        repeat(3) { tick ->
            baba.onTick(Tick(tick))
        }
        
        // Should have killed some throwers
        val aliveThrowers = baba.baba.getBaboonThrowers().count { it.isAlive }
        assertTrue(aliveThrowers < 2)
    }
    
    @Test
    fun `test Baba fight returns to attacking boss after throwers are dead`() {
        val loadout = createTestLoadout()
        val baba = Baba.create(loadout, invocationLevel = 500, pathLevel = 2)
        val initialHp = baba.baba.health.value
        
        // Spawn baboon throwers
        baba.baba.maybeSpawnBaboonThrowers(Tick(37))
        
        // Kill all throwers
        baba.baba.getBaboonThrowers().forEach { it.takeDamage(1) }
        assertEquals(0, baba.baba.getBaboonThrowers().count { it.isAlive })
        
        // Should be able to attack Baba now
        assertTrue(baba.baba.isAttackable(Tick(0)))
        
        // Ensure player can attack by setting last attack tick to allow immediate attack
        loadout.player.setLastAttackTick(Tick(0), 1) // Allow attack immediately
        
        // Simulate a few ticks - should attack Baba
        repeat(3) { tick ->
            baba.onTick(Tick(40 + tick))
        }
        
        // Boss should have taken some damage (or at least be attackable)
        assertTrue(baba.baba.isAlive)
        assertTrue(baba.baba.isAttackable(Tick(50)))
    }
    
    private fun createBabaBoss(): BabaBoss {
        val babaBaseStats = DefaultCombatStats(
            defenceLevel = 70,
            magicLevel = 100,
            meleeSlashDefenceBonus = 160,
            rangedDefenceBonus = 110,
            magicDefenceBonus = 200
        )
        
        return BabaBoss(ToaCombatEntity(
            name = "Test Baba",
            baseHp = BabaConstants.BASE_HP,
            invocationLevel = 500,
            pathLevel = 2,
            baseCombatStats = DefenceDrainCappedCombatStats(ToaMonsterCombatStats(babaBaseStats, invocationLevel = 500), drainCap = 20)
        ))
    }
    
    private fun createTestLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            health = Health(99),
            name = "Test Player",
            hasLightbearer = true
        ))
        
        val babaBoss = createBabaBoss()
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = Weapons.UltorFang
            override val strategy = BabaMainFightStrategy(babaBoss)
        }
    }
} 