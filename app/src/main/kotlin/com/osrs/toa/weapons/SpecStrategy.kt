package com.osrs.toa.weapons

import com.osrs.toa.Tick

interface SpecStrategy {
    fun selectWeapons(tick: Tick): Triple<Weapon, SpecWeapon?, Boolean>
} 