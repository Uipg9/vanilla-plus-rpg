package com.vanillaplus.rpg.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client-side cache for skill levels synced from server
 * This allows the Skills screen to display data without server round-trips
 */
@Environment(EnvType.CLIENT)
public class ClientSkillCache {
    
    private static int farmingLevel = 0;
    private static int combatLevel = 0;
    private static int defenseLevel = 0;
    private static int smithingLevel = 0;
    private static int woodcuttingLevel = 0;
    private static int miningLevel = 0;
    
    /**
     * Update all skill levels from server sync
     */
    public static void updateSkillLevels(int farming, int combat, int defense, int smithing, int woodcutting, int mining) {
        farmingLevel = farming;
        combatLevel = combat;
        defenseLevel = defense;
        smithingLevel = smithing;
        woodcuttingLevel = woodcutting;
        miningLevel = mining;
    }
    
    // Getters
    public static int getFarmingLevel() { return farmingLevel; }
    public static int getCombatLevel() { return combatLevel; }
    public static int getDefenseLevel() { return defenseLevel; }
    public static int getSmithingLevel() { return smithingLevel; }
    public static int getWoodcuttingLevel() { return woodcuttingLevel; }
    public static int getMiningLevel() { return miningLevel; }
    
    /**
     * Get skill level by index (matching Skill enum order)
     * 0=Farming, 1=Combat, 2=Defense, 3=Smithing, 4=Woodcutting, 5=Mining
     */
    public static int getSkillLevel(int index) {
        return switch (index) {
            case 0 -> farmingLevel;
            case 1 -> combatLevel;
            case 2 -> defenseLevel;
            case 3 -> smithingLevel;
            case 4 -> woodcuttingLevel;
            case 5 -> miningLevel;
            default -> 0;
        };
    }
    
    /**
     * Get bonus percentage for a skill (each level = 5%)
     */
    public static int getBonusPercent(int index) {
        return getSkillLevel(index) * 5;
    }
    
    /**
     * Get total skill points invested
     */
    public static int getTotalInvested() {
        return farmingLevel + combatLevel + defenseLevel + smithingLevel + woodcuttingLevel + miningLevel;
    }
}
