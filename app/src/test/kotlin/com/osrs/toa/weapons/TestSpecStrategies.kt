package com.osrs.toa.weapons

import com.osrs.toa.Tick
import com.osrs.toa.weapons.SpecStrategy
import com.osrs.toa.weapons.Weapon
import com.osrs.toa.weapons.SpecWeapon

class TestStrategy : SpecStrategy {
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        return Triple(mainWeapon, Weapons.ZaryteCrossbow, true)
    }
}

class NoSpecStrategy : SpecStrategy {
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        return Triple(mainWeapon, null, false)
    }
}

class ConditionalSpecStrategy(private val shouldSpec: Boolean) : SpecStrategy {
    override fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean> {
        return Triple(mainWeapon, Weapons.ZaryteCrossbow, shouldSpec && tick.value != 0)
    }
} 