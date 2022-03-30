package com.trialmobile.unlucky.entity.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.trialmobile.unlucky.animation.AnimationManager;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;

/**
 * A boss enemy with special attributes
 *
 * @author Ming Li
 */
public class Boss extends Enemy {

    // the unique identifier for bosses
    public int bossId;

    public Boss(String id, Vector2 position, TileMap tileMap, ResourceManager rm) {
        super(id, position, tileMap, rm);
    }

    public Boss(String id, int bossId, Vector2 position, TileMap tileMap, ResourceManager rm,
                int worldIndex, int startIndex, int numFrames, float delay) {
        this(id, position, tileMap, rm);
        this.bossId = bossId;

        // create tilemap animation
        am = new AnimationManager(rm.sprites16x16, worldIndex, startIndex, numFrames, delay);
        // create battle scene animation
        bam = new AnimationManager(rm.battleSprites96x96, worldIndex, startIndex, 2, delay);
    }

    @Override
    public boolean isElite() {
        return false;
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    /**
     * Sets the stats of the boss based on level and the boss index
     */
    @Override
    public void setStats() {
        int mhp = 0;
        int minDmg = 0;
        int maxDmg = 0;

        switch (bossId) {
            case 0: // king slime
                // has lower hp because its passive compensates for it
                int mhpSeed0 = (int) (Math.pow(level, 2.1) + 15);
                mhp = Util.getDeviatedRandomValue(mhpSeed0, 1);
                minDmg = MathUtils.random(5, 9);
                maxDmg = MathUtils.random(10, 15);
                for (int i = 0; i < level - 1; i++) {
                    minDmg += MathUtils.random(2, 4) - MathUtils.random(1);
                    maxDmg += MathUtils.random(2, 4) + MathUtils.random(1);
                }
                break;
            case 1: // red reaper
                int mhpSeed1 = (int) (Math.pow(level, 2) + 14);
                mhp = Util.getDeviatedRandomValue(mhpSeed1, 3);
                minDmg = MathUtils.random(3, 8);
                maxDmg = MathUtils.random(9, 15);
                for (int i = 0; i < level - 1; i++) {
                    minDmg += MathUtils.random(1, 2) - MathUtils.random(2);
                    maxDmg += MathUtils.random(1, 2) + MathUtils.random(2);
                }
                break;
            case 2: // ice golem
                int mhpSeed2 = (int) (Math.pow(level, 2.3) + 25);
                mhp = Util.getDeviatedRandomValue(mhpSeed2, 150);
                minDmg = MathUtils.random(1, 4);
                maxDmg = MathUtils.random(5, 8);
                for (int i = 0; i < level - 1; i++) {
                    minDmg += MathUtils.random(1, 2) - 1;
                    maxDmg += MathUtils.random(1, 2) + 1;
                }
                break;
        }

        this.setMaxHp(mhp);
        this.setMinDamage(minDmg);
        this.setMaxDamage(maxDmg);
        this.setAccuracy(MathUtils.random(Util.ENEMY_MIN_ACCURACY, Util.ENEMY_MAX_ACCURACY));
    }

    /**
     * Returns a description of a boss's passive based on bossId
     *
     * @return String
     */
    public String getPassiveDescription() {
        switch (bossId) {
            // king slime
            case 0: return rm.bundle.get("BOSS_PASSIVE_SLIME_KING");
            // red reaper
            case 1: return rm.bundle.get("BOSS_PASSIVE_RED_REAPER");
            // ice golem
            case 2: return rm.bundle.get("BOSS_PASSIVE_ICE_GOLEM");
        }
        return "";
    }

}
