package com.osrs.toa.sims

import com.osrs.toa.Tick

interface BossFight {
    fun onTick(tick: Tick)
    fun isFightOver(): Boolean
}
