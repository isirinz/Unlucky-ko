package com.trialmobile.unlucky.resource;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.trialmobile.unlucky.battle.SpecialMove;
import com.trialmobile.unlucky.entity.Entity;
import com.trialmobile.unlucky.entity.enemy.Boss;
import com.trialmobile.unlucky.entity.enemy.Normal;
import com.trialmobile.unlucky.map.TileMap;

/**
 * Stores useful constants and functions
 *
 * @author Ming Li
 */
public class Util {

    // rates
    public static final float TEXT_SPEED = 0.03f;
    public static final float HP_BAR_DECAY_RATE = 25.f;
    public static final float TRANSITION_SCREEN_SPEED = 220;

    public static final int RAINDROP_X = 100;
    public static final int RAINDROP_Y_DEVIATED = 40;
    public static final int SNOWFLAKE_X = 60;
    public static final int SNOWFLAKE_Y_DEVIATED = 60;

    public static final Vector2 RAIN_VELOCITY = new Vector2(Util.RAINDROP_X, -200);
    public static final Vector2 HEAVY_RAIN_VELOCITY = new Vector2(Util.RAINDROP_X, -240);
    public static final Vector2 SNOW_VELOCITY = new Vector2(Util.SNOWFLAKE_X, -120);
    public static final Vector2 BLIZZARD_VELOCITY = new Vector2(Util.SNOWFLAKE_X + 100, -160);

    // Animation indexes
    public static final int PLAYER_WALKING = 0;

    // Animation delay
    public static final float PLAYER_WALKING_DELAY = 1 / 6f;

    // Directional pad button positions
    public static final int DIR_PAD_SIZE = 40;
    public static final int DIR_PAD_OFFSET = 10;

    // Special move indexes
    public static final int NUM_SPECIAL_MOVES = 8;
    public static final int DISTRACT = 0;
    public static final int FOCUS = 1;
    public static final int INTIMIDATE = 2;
    public static final int REFLECT = 3;
    public static final int STUN = 4;
    public static final int INVERT = 5;
    public static final int SACRIFICE = 6;
    public static final int SHIELD = 7;

    public static final SpecialMove S_DISTRACT = new SpecialMove(DISTRACT,
            "Distract", "Next enemy attack\n-" + Util.P_DISTRACT + "% ACC", 1, null);
    public static final SpecialMove S_FOCUS = new SpecialMove(FOCUS,
            "Focus", "Next attack 100% ACC\nand +" + Util.P_FOCUS_CRIT + "% crit chance", 7, null);
    public static final SpecialMove S_INTIMIDATE = new SpecialMove(INTIMIDATE,
            "Intimidate", "Next attack is\namplified by " + Util.P_INTIMIDATE + "%", 3, null);
    public static final SpecialMove S_REFLECT = new SpecialMove(REFLECT,
            "Reflect", "Next enemy attack\nis reflected back", 23, null);
    public static final SpecialMove S_STUN = new SpecialMove(STUN,
            "Stun", Util.P_STUN + "% chance to\nstun enemy", 5, null);
    public static final SpecialMove S_INVERT = new SpecialMove(INVERT,
            "Invert", "Heal moves damage\nDamage moves heal", 13, null);
    public static final SpecialMove S_SACRIFICE = new SpecialMove(SACRIFICE,
            "Sacrifice", "Sacrifice all but 1 hp\nfor increased dmg", 10, null);
    public static final SpecialMove S_SHIELD = new SpecialMove(SHIELD,
            "Shield", "Summon a shield that\nabsorbs " + Util.P_SHIELD + "% max hp", 17, null);

    public static final SpecialMove[] SMOVES_ORDER_BY_LVL = {
        S_DISTRACT, S_INTIMIDATE, S_STUN, S_FOCUS, S_SACRIFICE, S_INVERT, S_SHIELD, S_REFLECT
    };
    public static final SpecialMove[] SMOVES_ORDER_BY_ID = {
        S_DISTRACT, S_FOCUS, S_INTIMIDATE, S_REFLECT, S_STUN, S_INVERT, S_SACRIFICE, S_SHIELD
    };

    // Button dimensions
    public static final int MOVE_WIDTH = 144;
    public static final int MOVE_HEIGHT = 50;

    // Probabilities, Percentages, and Multipliers
    public static final int RUN_FROM_BATTLE = 7;
    public static final int SAVED_FROM_BATTLE = 1;
    public static final int ELITE_CHANCE = 5;
    public static final int PLAYER_ACCURACY = 80;
    public static final int ENEMY_MIN_ACCURACY = 75;
    public static final int ENEMY_MAX_ACCURACY = 95;
    public static final int REVIVAL = 1;
    public static final int ENCHANT = 50;
    public static final int DESTROY_ITEM_IF_FAIL = 50;
    public static final int TILE_INTERATION = 70;
    public static final int DEATH_PENALTY = 1;

    // Battle special moves percentages
    public static final int P_DISTRACT = 50;
    public static final int P_INTIMIDATE = 40;
    public static final float INTIMIDATE_MULT = 1.4f;
    public static final int P_FOCUS_CRIT = 30;
    public static final int P_STUN = 60;
    public static final int P_SHIELD = 20;

    public static final int CRIT_MULTIPLIER = 3;
    public static final float MIN_ELITE_MULTIPLIER = 1.3f;
    public static final float MAX_ELITE_MULTIPLIER = 1.6f;

    public static final int NORMAL_ITEM_DROP = 30;
    public static final int ELITE_ITEM_DROP = 60;
    public static final int BOSS_ITEM_DROP = 80;
    public static final int COMMON_ITEM_RNG_INDEX = 60;
    public static final int RARE_ITEM_RNG_INDEX = 90;
    public static final int EPIC_ITEM_RNG_INDEX = 99;
    public static final int LEGENDARY_ITEM_RNG_INDEX = 100;

    public static final float COMMON_ENCHANT_MIN = 1.f;
    public static final float COMMON_ENCHANT_MAX = 1.2f;
    public static final float RARE_ENCHANT_MIN = 1.1f;
    public static final float RARE_ENCHANT_MAX = 1.3f;
    public static final float EPIC_ENCHANT_MIN = 1.2f;
    public static final float EPIC_ENCHANT_MAX = 1.4f;
    public static final float LEGENDARY_ENCHANT_MIN = 1.3f;
    public static final float LEGENDARY_ENCHANT_MAX = 1.6f;

    // Level up scaling
    public static final int PLAYER_INIT_MAX_HP = 75;
    public static final int PLAYER_INIT_MIN_DMG = 10;
    public static final int PLAYER_INIT_MAX_DMG = 16;
    public static final int PLAYER_MIN_HP_INCREASE = 9;
    public static final int PLAYER_MAX_HP_INCREASE = 17;
    public static final int PLAYER_MIN_DMG_INCREASE = 1;
    public static final int PLAYER_MAX_DMG_INCREASE = 3;

    public static final int ENEMY_INIT_MIN_MINDMG = 2;
    public static final int ENEMY_INIT_MAX_MINDMG = 4;
    public static final int ENEMY_INIT_MIN_MAXDMG = 5;
    public static final int ENEMY_INIT_MAX_MAXDMG = 7;

    public static final int ENEMY_MIN_DMG_INCREASE = 1;
    public static final int ENEMY_MAX_DMG_INCREASE = 4;

    // Experience

    /**
     * Current max exp formula is:
     * EXP = 2 * level * (1.35 ^ (level / 3)) + offset
     * Slow exponential growth
     *
     * @param level the level of the player
     * @param offset the initial max exp at level 1 acting as an offset to the curve (random)
     * @return max experience at a given level
     */
    public static int calculateMaxExp(int level, int offset) {
        return (int) (2 * level * (Math.pow(1.3, level / 3f)) + offset) + 4;
    }

    /**
     * The exp given to the player after it's defeated
     * EXP = (enemyLevel ^ 0.8) + offset
     *
     * @param enemyLevel level
     * @param offset offset
     * @return exp
     */
    public static int calculateExpEarned(int enemyLevel, int offset) {
        return (int) (Math.pow(enemyLevel, 0.95)) + offset;
    }

    // Random helper functions

    /**
     * Returns if an event was successful given a probability
     *
     * @param p percentage
     * @return boolean
     */
    public static boolean isSuccess(int p) {
        int k = MathUtils.random(99);
        return k < p;
    }

    /**
     * Returns a random number in a range produced by a mean and standard deviation
     *
     * @param mu avg
     * @param sigma deviation
     * @return value
     */
    public static int getDeviatedRandomValue(int mu, int sigma) {
        int n0 = mu - sigma;
        int n1 = mu + sigma;
        return MathUtils.random(n0, n1);
    }

    public static Label.LabelStyle getItemColor(int rarity, ResourceManager rm) {
        switch (rarity) {
            case 0: return new Label.LabelStyle(rm.skin.getFont("default-font"), new Color(1, 1, 1, 1));
            case 1: return new Label.LabelStyle(rm.skin.getFont("default-font"), new Color(0, 200 / 255.f, 0, 1));
            case 2: return new Label.LabelStyle(rm.skin.getFont("default-font"), new Color(0, 180 / 255.f, 1, 1));
            case 3: return new Label.LabelStyle(rm.skin.getFont("default-font"), new Color(164 / 255.f, 80 / 255.f, 1, 1));
        }
        return null;
    }

    // Map

    // all blocked tile ids
    public static final int[] BLOCKED_TILE_IDS = {
        5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 25, 26, 27, 28, 29, 30, 31, 36,
        38, 41, 42, 48, 49, 50, 51, 54, 55, 64, 65, 66, 67, 70, 84, 85, 86,
        87, 88, 91, 92, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 112,
        113, 114, 115, 116, 117, 118, 120, 121, 123, 128, 129, 130, 130, 131, 132,
        133, 134, 135, 136, 137, 138, 139, 144, 145, 146, 147, 156, 157, 165, 166,
        151, 152, 167, 168, 169, 170, 184, 185, 186, 197, 198, 199, 200, 201, 202,
        215, 216, 217, 218, 227, 228, 229, 230, 231, 232, 233, 234, 235, 243, 244,
        247, 249, 263, 264, 265, 266, 267, 268, 269
    };

    public static final int[] BLOCKED_ANIMATED_TILE_IDS = { 96, 109 };

    public static boolean isBlockedTile(int id) {
        for (int blockedTileId : BLOCKED_TILE_IDS) {
            if (id == blockedTileId) return true;
        }
        return false;
    }

    public static boolean isBlockedAnimatedTile(int id) {
        for (int blockedAnimatedTileId : BLOCKED_ANIMATED_TILE_IDS) {
            if (id == blockedAnimatedTileId) return true;
        }
        return false;
    }

    /**
     * Returns an instance of an Entity based on numerical Entity id
     *
     * @param id enemy name
     * @param position position
     * @param map tile map
     * @param rm resource manager
     * @return entity
     */
    public static Entity getEntity(int id, Vector2 position, TileMap map, ResourceManager rm) {
        switch (id) {
            case 2: return new Normal(rm.bundle.get("slime"), position, map, rm, 1, 0, 2, 1 / 3f);
            case 3: return new Normal(rm.bundle.get("blue_slime"), position, map, rm, 1, 2, 2, 1 / 3f);
            case 4: return new Normal(rm.bundle.get("blast_slime"), position, map, rm, 1, 4, 2, 1 / 3f);
            case 5: return new Boss(rm.bundle.get("slime_king"), 0, position, map, rm, 1, 6, 2, 1 / 3f);
            case 6: return new Normal(rm.bundle.get("ghost"), position, map, rm, 2, 0, 2, 1 / 3f);
            case 7: return new Normal(rm.bundle.get("zombie"), position, map, rm, 2, 2, 2, 1 / 3f);
            case 8: return new Normal(rm.bundle.get("skeleton"), position, map, rm, 2, 4, 2, 1 / 3f);
            case 9: return new Normal(rm.bundle.get("witch"), position, map, rm, 2, 6, 2, 1 / 3f);
            case 10: return new Boss(rm.bundle.get("red_reaper"), 1, position, map, rm, 2, 8, 2, 1 / 3f);
            case 11: return new Normal(rm.bundle.get("snow_puff"), position, map, rm, 3, 0, 2, 1 / 3f);
            case 12: return new Normal(rm.bundle.get("angry_penguin"), position, map, rm, 3, 2, 2, 1 / 3f);
            case 13: return new Normal(rm.bundle.get("yeti"), position, map, rm, 3, 4, 2, 1 / 3f);
            case 14: return new Normal(rm.bundle.get("ice_bat"), position, map, rm, 3, 6, 2, 1 / 3f);
            case 15: return new Boss(rm.bundle.get("ice_golem"), 2, position, map, rm, 3, 8, 2, 1 / 3f);
        }
        return null;
    }

}
