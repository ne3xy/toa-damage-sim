package com.osrs.toa

import com.osrs.toa.actors.GenericCombatEntity
import com.osrs.toa.actors.Player
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon
import com.osrs.toa.weapons.Weapons
import com.osrs.toa.Tick
import com.osrs.toa.sims.Zebak
import com.osrs.toa.sims.Akkha
import com.osrs.toa.sims.ZebakMainFightStrategy
import com.osrs.toa.sims.AkkhaMainFightStrategy

/**
 * Represents a player loadout with specific strategy
 */
interface PlayerLoadout {
    val player: Player
    val strategy: SpecStrategy
}

/**
 * Factory for creating different player loadout combinations
 */
object PlayerLoadoutFactory {
    
    /**
     * Creates a player with Lightbearer=false, Magus Shadow + ZCB, and Akkha strategy
     */
    fun createMagusZcbAkkhaLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            name = "Magus+ZCB Player",
            health = Health(99),
            hasLightbearer = false
        ))
        val akkhaBoss = Akkha(player).akkha
        val strategy = AkkhaMainFightStrategy(akkhaBoss)
        return object : PlayerLoadout {
            override val player = player
            override val strategy = strategy
            private val normalWeapon = Weapons.MagussShadow
            private val specWeapon = Weapons.ZaryteCrossbow
        }
    }
    
    /**
     * Creates a player with Lightbearer=false, BGS + Twisted Bow
     */
    fun createBgsTbowLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            name = "BGS+TBow Player",
            health = Health(99),
            hasLightbearer = false
        ))
        val zebakBoss = Zebak(player).zebak
        val strategy = ZebakMainFightStrategy(zebakBoss)
        return object : PlayerLoadout {
            override val player = player
            override val strategy = strategy
            private val normalWeapon = Weapons.Zebak6WayTwistedBow
            private val specWeapon = Weapons.BandosGodsword
        }
    }
    
    /**
     * Creates a player with Lightbearer=true, Magus Shadow + ZCB
     */
    fun createLightbearerMagusZcbLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            name = "Lightbearer+Magus+ZCB Player",
            health = Health(99),
            hasLightbearer = true
        ))
        val akkhaBoss = Akkha(player).akkha
        val strategy = AkkhaMainFightStrategy(akkhaBoss)
        return object : PlayerLoadout {
            override val player = player
            override val strategy = strategy
            private val normalWeapon = Weapons.MagussShadow
            private val specWeapon = Weapons.ZaryteCrossbow
        }
    }
    
    /**
     * Creates a player with Lightbearer=true, BGS + Twisted Bow
     */
    fun createLightbearerBgsTbowLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            name = "Lightbearer+BGS+TBow Player",
            health = Health(99),
            hasLightbearer = true
        ))
        val zebakBoss = Zebak(player).zebak
        val strategy = ZebakMainFightStrategy(zebakBoss)
        return object : PlayerLoadout {
            override val player = player
            override val strategy = strategy
            private val normalWeapon = Weapons.Zebak6WayTwistedBow
            private val specWeapon = Weapons.BandosGodsword
        }
    }
} 