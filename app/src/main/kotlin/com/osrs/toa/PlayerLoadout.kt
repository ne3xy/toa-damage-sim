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
 * Represents a player loadout with specific weapons and strategy
 */
interface PlayerLoadout {
    val player: Player
    val mainWeapon: Weapon
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
        val mainWeapon = Weapons.MagussShadow
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = mainWeapon
            override val strategy = AkkhaMainFightStrategy(Akkha(this, invocationLevel = 530, pathLevel = 3).akkha)
        }
    }
    
    /**
     * Creates a player with Lightbearer=false, BGS + Twisted Bow
     */
    fun createBgsTbowZebakLoadout(): PlayerLoadout {
        val player = Player(GenericCombatEntity(
            name = "BGS+TBow Player",
            health = Health(99),
            hasLightbearer = false
        ))
        val mainWeapon = Weapons.Zebak6WayTwistedBow
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = mainWeapon
            override val strategy = ZebakMainFightStrategy(Zebak(this, invocationLevel = 530, pathLevel = 3).zebak)
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
        val mainWeapon = Weapons.MagussShadow
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = mainWeapon
            override val strategy = AkkhaMainFightStrategy(Akkha(this, invocationLevel = 530, pathLevel = 3).akkha)
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
        val mainWeapon = Weapons.Zebak6WayTwistedBow
        
        return object : PlayerLoadout {
            override val player = player
            override val mainWeapon = mainWeapon
            override val strategy = ZebakMainFightStrategy(Zebak(this, invocationLevel = 530, pathLevel = 3).zebak)
        }
    }
}

class AkkhaLoadout(
    override val player: Player,
    override val mainWeapon: Weapon = Weapons.MagussShadow
) : PlayerLoadout {
    override val strategy = AkkhaMainFightStrategy(Akkha(this, invocationLevel = 530, pathLevel = 3).akkha)
}

class AkkhaLoadoutWithCustomStrategy(
    override val player: Player,
    override val mainWeapon: Weapon = Weapons.MagussShadow,
    private val customStrategy: SpecStrategy
) : PlayerLoadout {
    override val strategy = AkkhaMainFightStrategy(Akkha(this, customStrategy, invocationLevel = 530, pathLevel = 3).akkha)
}

class ZebakLoadout(
    override val player: Player,
    override val mainWeapon: Weapon = Weapons.Zebak6WayTwistedBow
) : PlayerLoadout {
    override val strategy = ZebakMainFightStrategy(Zebak(this, invocationLevel = 530, pathLevel = 3).zebak)
}

class ZebakLoadoutWithCustomStrategy(
    override val player: Player,
    override val mainWeapon: Weapon = Weapons.Zebak6WayTwistedBow,
    private val customStrategy: SpecStrategy
) : PlayerLoadout {
    override val strategy = ZebakMainFightStrategy(Zebak(this, customStrategy, invocationLevel = 530, pathLevel = 3).zebak)
} 