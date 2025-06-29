package com.osrs.toa.weapons

import com.osrs.toa.Tick

interface SpecStrategy {
    fun selectWeapons(tick: Tick, mainWeapon: Weapon): Triple<Weapon, SpecWeapon?, Boolean>
} 