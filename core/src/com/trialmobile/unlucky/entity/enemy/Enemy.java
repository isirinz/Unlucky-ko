package com.trialmobile.unlucky.entity.enemy;

import com.badlogic.gdx.math.Vector2;
import com.trialmobile.unlucky.battle.Moveset;
import com.trialmobile.unlucky.battle.StatusSet;
import com.trialmobile.unlucky.entity.Entity;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.resource.ResourceManager;

/**
 * An Entity that the Player can battle if encountered
 * A Tile will hold an Enemy
 * An Enemy will not be able to move
 *
 * @author Ming Li
 */
public abstract class Enemy extends Entity {

    // battle status effects
    public StatusSet statusEffects;

    // battle sprite size
    // (used for making sprites bigger or smaller for effect)
    public int battleSize;
    // num of times the enemy respawned
    // (used for enemies that have special respawn capabilities)
    public int numRespawn;

    public Enemy(String id, Vector2 position, TileMap tileMap, ResourceManager rm) {
        super(id, position, tileMap, rm);
        moveset = new Moveset(rm);
        statusEffects = new StatusSet(false, rm);
        battleSize = 96;
        numRespawn = 0;
    }

    public abstract boolean isElite();

    public abstract boolean isBoss();

    /**
     * Sets and scales the stats of the enemy based on its type and level
     */
    public abstract void setStats();

    @Override
    public void setMaxHp(int maxHp) {
        this.maxHp = this.hp = this.previousHp = maxHp;
    }

    public void setOnlyMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

}
