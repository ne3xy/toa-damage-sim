package com.osrs.toa

import com.osrs.toa.weapons.SpecWeapon

/**
 * Tracks special attack usage across simulations
 */
class SpecTracker {
    private val specCounts = mutableMapOf<String, Int>()
    
    /**
     * Record a special attack usage
     */
    fun recordSpec(specWeapon: SpecWeapon) {
        val weaponName = specWeapon.name
        specCounts[weaponName] = specCounts.getOrDefault(weaponName, 0) + 1
    }
    
    /**
     * Get the count for a specific weapon
     */
    fun getSpecCount(weaponName: String): Int {
        return specCounts.getOrDefault(weaponName, 0)
    }
    
    /**
     * Get all spec counts as a map
     */
    fun getAllSpecCounts(): Map<String, Int> {
        return specCounts.toMap()
    }
    
    /**
     * Reset all counts
     */
    fun reset() {
        specCounts.clear()
    }
    
    /**
     * Get the total number of specs used
     */
    fun getTotalSpecs(): Int {
        return specCounts.values.sum()
    }
    
    /**
     * Get average specs per weapon across iterations
     */
    fun getAverageSpecsPerWeapon(iterations: Int): Map<String, Double> {
        return specCounts.mapValues { (_, count) -> count.toDouble() / iterations }
    }
} 