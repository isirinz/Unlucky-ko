package com.trialmobile.unlucky.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.animation.AnimationManager;
import com.trialmobile.unlucky.battle.Moveset;
import com.trialmobile.unlucky.battle.SpecialMoveset;
import com.trialmobile.unlucky.battle.StatusSet;
import com.trialmobile.unlucky.entity.enemy.Enemy;
import com.trialmobile.unlucky.inventory.Equipment;
import com.trialmobile.unlucky.inventory.Inventory;
import com.trialmobile.unlucky.inventory.Item;
import com.trialmobile.unlucky.map.GameMap;
import com.trialmobile.unlucky.map.Tile;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Statistics;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.save.Settings;

/**
 * The protagonist of the game.
 *
 * @author Ming Li
 */
public class Player extends Entity {

    /**
     * -1 - stop
     * 0 - down
     * 1 - up
     * 2 - right
     * 3 - left
     */
    public int moving = -1;
    // entity is in a continuous movement
    private float speed;
    // the Entity's current tile coordinates
    private int currentTileX;
    private int currentTileY;
    private int prevDir = -1;
    // tile causing a dialog event
    private boolean tileInteraction = false;
    // teleportation tiles
    private boolean teleporting = false;
    // end tiles
    public boolean completedMap = false;

    // Statistics
    public Statistics stats = new Statistics();

    // Battle
    private Enemy opponent;
    private boolean battling = false;

    // exp and level up
    private int exp;
    private int maxExp;

    private int hpIncrease = 0;
    private int minDmgIncrease = 0;
    private int maxDmgIncrease = 0;
    private int accuracyIncrease = 0;
    private int maxExpIncrease = 0;

    // gold
    private int gold = 0;

    // inventory and equips
    public Inventory inventory;
    public Equipment equips;

    // battle status effects
    public StatusSet statusEffects;
    // special moveset
    public SpecialMoveset smoveset;

    // special move cooldown
    // starts at 4 turns then every 10 levels it is reduced by 1 with a min of 1
    public int smoveCd = 4;

    // whether or not the player is currently in a map
    public boolean inMap = false;

    // player's level progress stored as a (world, level) key
    public int maxWorld = 0;
    public int maxLevel = 0;

    // the player's custom game settings
    public Settings settings = new Settings();

    public Player(String id, ResourceManager rm) {
        super(id, rm);

        inventory = new Inventory();
        equips = new Equipment();

        // attributes
        hp = maxHp = previousHp = Util.PLAYER_INIT_MAX_HP;
        accuracy = Util.PLAYER_ACCURACY;
        minDamage = Util.PLAYER_INIT_MIN_DMG;
        maxDamage = Util.PLAYER_INIT_MAX_DMG;

        level = 1;
        speed = 100.f;

        exp = 0;
        // offset between 3 and 5
        maxExp = Util.calculateMaxExp(1, MathUtils.random(3, 5));

        // create tilemap animation
        am = new AnimationManager(rm.sprites16x16, Util.PLAYER_WALKING, Util.PLAYER_WALKING_DELAY);
        // create battle scene animation
        bam = new AnimationManager(rm.battleSprites96x96, 2, Util.PLAYER_WALKING, 2 / 5f);

        moveset = new Moveset(rm);
        // damage seed is a random number between the damage range
        moveset.reset(minDamage, maxDamage, maxHp);

        statusEffects = new StatusSet(true, rm);
        smoveset = new SpecialMoveset();
    }

    public void update(float dt) {
        super.update(dt);

        // movement
        handleMovement(dt);
        // special tile handling
        handleSpecialTiles();

        // check for Entity interaction
        if (tileMap.containsEntity(tileMap.toTileCoords(position)) && canMove()) {
            opponent = (com.trialmobile.unlucky.entity.enemy.Enemy) tileMap.getEntity(tileMap.toTileCoords(position));
            battling = true;
        }
    }

    public void render(SpriteBatch batch) {
        // draw shadow
        batch.draw(rm.shadow11x6, position.x + 6, position.y - 6, 22, 12);
        batch.draw(am.getKeyFrame(true), position.x + 1, position.y, am.width * 2, am.height * 2);
    }

    /**
     * Moves an entity to a target position with a given magnitude.
     * Player movement triggered by input
     *
     * @param dir
     */
    public void move(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        prevDir = dir;
        moving = dir;
        stats.numSteps++;
    }

    public boolean canMove() {
        return moving == -1;
    }

    /**
     * This method is to fix a problem where the player can reset their
     * movement magnitudes continuously on a blocked tile
     *
     * @param dir
     * @return
     */
    public boolean nextTileBlocked(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        switch (dir) {
            case 0: // down
                return tileMap.getTile(currentTileX, currentTileY - 1).isBlocked();
            case 1: // up
                return tileMap.getTile(currentTileX, currentTileY + 1).isBlocked();
            case 2: // right
                return tileMap.getTile(currentTileX + 1, currentTileY).isBlocked();
            case 3: // left
                return tileMap.getTile(currentTileX - 1, currentTileY).isBlocked();
        }
        return false;
    }

    /**
     * Returns the next tile coordinate to move to either
     * currentPos +/- 1 or currentPos if the next tile is blocked
     *
     * @param dir
     * @return
     */
    public int nextPosition(int dir) {
        switch (dir) {
            case 0: // down
                Tile d = tileMap.getTile(currentTileX, currentTileY - 1);
                if (d.isBlocked() || currentTileY - 1 <= 0) {
                    return currentTileY;
                }
                return currentTileY - 1;
            case 1: // up
                Tile u = tileMap.getTile(currentTileX, currentTileY + 1);
                if (u.isBlocked() || currentTileY + 1 >= tileMap.mapHeight - 1) {
                    return currentTileY;
                }
                return currentTileY + 1;
            case 2: // right
                Tile r = tileMap.getTile(currentTileX + 1, currentTileY);
                if (r.isBlocked() || currentTileX + 1 >= tileMap.mapWidth - 1) {
                    return currentTileX;
                }
                return currentTileX + 1;
            case 3: // left
                Tile l = tileMap.getTile(currentTileX - 1, currentTileY);
                if (l.isBlocked() || currentTileX - 1 <= 0) {
                    return currentTileX;
                }
                return currentTileX - 1;
        }
        return 0;
    }

    /**
     * Handles the player's next movements when standing on a special tile
     */
    public void handleSpecialTiles() {
        int cx = (int) (position.x / tileMap.tileSize);
        int cy = (int) (position.y / tileMap.tileSize);
        Tile currentTile = tileMap.getTile(cx, cy);

        if (currentTile.isSpecial()) am.currentAnimation.stop();

        if (canMove()) {
            // Player goes forwards or backwards from the tile in the direction they entered
            if (currentTile.isChange()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                boolean k = MathUtils.randomBoolean();
                switch (prevDir) {
                    case 0: // down
                        if (k) changeDirection(1);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (k) changeDirection(0);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (k) changeDirection(3);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (k) changeDirection(2);
                        else changeDirection(3);
                        break;
                }
            }
            // Player goes 1 tile in a random direction not the direction they entered the tile on
            else if (currentTile.isInAndOut()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                // output direction (all other directions other than input direction)
                int odir = MathUtils.random(2);
                switch (prevDir) {
                    case 0: // down
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(3);
                        break;
                }
            }
            else if (currentTile.isDown()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(0);
            }
            else if (currentTile.isUp()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(1);
            }
            else if (currentTile.isRight()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(2);
            }
            else if (currentTile.isLeft()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(3);
            }
            // trigger dialog event
            else if (currentTile.isQuestionMark() || currentTile.isExclamationMark()) tileInteraction = true;
            // trigger teleport event
            else if (currentTile.isTeleport()) teleporting = true;
            // ice sliding
            else if (currentTile.isIce()) {
                if (!nextTileBlocked(prevDir)) {
                    move(prevDir);
                    am.setAnimation(prevDir);
                    am.stopAnimation();
                    pauseAnim = true;
                }
            }
            // map completed
            else if (currentTile.isEnd()) completedMap = true;
            else pauseAnim = false;
        }
    }

    public void changeDirection(int dir) {
        move(dir);
        prevDir = dir;
        am.setAnimation(dir);
    }

    /**
     * Updates every tick and moves an Entity if not on the tile map grid
     */
    public void handleMovement(float dt) {
        // down
        if (moving == 0) {
            int targetY = nextPosition(0);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y -= speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // up
        if (moving == 1) {
            int targetY = nextPosition(1);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y += speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // right
        if (moving == 2) {
            int targetX = nextPosition(2);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x += speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // left
        if (moving == 3) {
            int targetX = nextPosition(3);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x -= speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
    }

    /**
     * Increments level and recalculates max exp
     * Sets increase variables to display on screen
     * Recursively accounts for n consecutive level ups from remaining exp
     *
     * @param remainder the amount of exp left after a level up
     */
    public void levelUp(int remainder) {
        level++;

        hpIncrease += MathUtils.random(Util.PLAYER_MIN_HP_INCREASE, Util.PLAYER_MAX_HP_INCREASE);
        int dmgMean = MathUtils.random(Util.PLAYER_MIN_DMG_INCREASE, Util.PLAYER_MAX_DMG_INCREASE);

        // deviates from mean by 0 to 2
        minDmgIncrease += (dmgMean - MathUtils.random(1));
        maxDmgIncrease += (dmgMean + MathUtils.random(1));
        // accuracy increases by 1% every 10 levels
        accuracyIncrease += level % 10 == 0 ? 1 : 0;
        // smoveCd reduces every 10 levels
        if (smoveCd > 1) smoveCd -= level % 10 == 0 ? 1 : 0;

        int prevMaxExp = maxExp;
        maxExp = Util.calculateMaxExp(level, MathUtils.random(3, 5));
        maxExpIncrease += (maxExp - prevMaxExp);

        // another level up
        if (remainder >= maxExp) {
            levelUp(remainder - maxExp);
        } else {
            exp = remainder;
        }
    }

    /**
     * Increases the actual stats by their level up amounts
     */
    public void applyLevelUp() {
        maxHp += hpIncrease;
        hp = maxHp;
        minDamage += minDmgIncrease;
        maxDamage += maxDmgIncrease;
        accuracy += accuracyIncrease;

        // reset variables
        hpIncrease = 0;
        minDmgIncrease = 0;
        maxDmgIncrease = 0;
        accuracyIncrease = 0;
        maxExpIncrease = 0;
    }

    /**
     * Applies the stats of an equipable item
     *
     * @param item
     */
    public void equip(Item item) {
        maxHp += item.mhp;
        hp = maxHp;
        minDamage += item.dmg;
        maxDamage += item.dmg;
        accuracy += item.acc;
    }

    /**
     * Removes the stats of an equipable item
     *
     * @param item
     */
    public void unequip(Item item) {
        maxHp -= item.mhp;
        hp = maxHp;
        minDamage -= item.dmg;
        maxDamage -= item.dmg;
        accuracy -= item.acc;
    }

    public Enemy getOpponent() {
        return opponent;
    }

    public void finishBattling() {
        battling = false;
        opponent = null;
        moving = -1;
    }

    public void finishTileInteraction() {
        tileInteraction = false;
        moving = -1;
    }

    /**
     * After teleportation is done the player is moved out of the tile in a random direction
     */
    public void finishTeleporting() {
        teleporting = false;
        changeDirection(MathUtils.random(3));
    }

    public void potion(int heal) {
        hp += heal;
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * Applies a percentage health potion
     * @param php
     */
    public void percentagePotion(int php) {
        hp += (int) ((php / 100f) * maxHp);
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * Green question mark tiles can drop 70% of the time
     * if does drop:
     * - gold (50% of the time) (based on map level)
     * - heals based on map level (45% of the time)
     * - items (5% of the time)
     *
     * @return
     */
    public String[] getQuestionMarkDialog(int mapLevel, GameMap gameMap) {
        String[] ret = null;

        if (Util.isSuccess(Util.TILE_INTERATION)) {
            int k = MathUtils.random(99);
            // gold
            if (k < 50) {
                // gold per level scaled off map's average level
                int gold = 0;
                for (int i = 0; i < mapLevel; i++) {
                    gold += MathUtils.random(7, 13);
                }
                this.gold += gold;
                gameMap.goldObtained += gold;
                ret = new String[] {
                    rm.bundle.get("TILE_RANDOM"),
                    rm.bundle.format("TILE_GOLD", gold)
                };
            }
            // heal
            else if (k < 95) {
                int heal = 0;
                for (int i = 0; i < mapLevel; i++) {
                    heal += MathUtils.random(2, 5);
                }
                this.hp += heal;
                if (hp > maxHp) hp = maxHp;
                ret = new String[] {
                    rm.bundle.get("TILE_RANDOM"),
                    rm.bundle.format("TILE_HEAL", heal)
                };
            }
            // item
            else if (k < 100) {
                Item item = rm.getRandomItem();
                if (inventory.isFull()) {
                    ret = new String[] {
                        rm.bundle.get("TILE_RANDOM"),
                        rm.bundle.format("TILE_ITEM", item.getDialogName()),
                        rm.bundle.get("TILE_INVENTORY_FULL")
                    };
                }
                else {
                    ret = new String[]{
                        rm.bundle.get("TILE_RANDOM"),
                        rm.bundle.format("TILE_ITEM", item.getDialogName()),
                        rm.bundle.get("TILE_ITEM_SUCCESS")
                    };
                    item.adjust(mapLevel);
                    inventory.addItem(item);
                    gameMap.itemsObtained.add(item);
                }
            }
        }
        else {
            ret = new String[] {
                rm.bundle.get("TILE_NOTHING")
            };
        }

        return ret;
    }

    /**
     * The purple exclamation mark tile is a destructive tile
     * that has a 60% chance to do damage to the player and
     * 40% chance to steal gold.
     *
     * @param mapLevel level
     * @return message
     */
    public String[] getExclamDialog(int mapLevel, GameMap gameMap) {
        String[] ret = null;

        if (Util.isSuccess(Util.TILE_INTERATION)) {
            if (Util.isSuccess(60)) {
                int dmg = 0;
                for (int i = 0; i < mapLevel; i++) {
                    dmg += MathUtils.random(1, 4);
                }
                hp -= dmg;
                // player dies from tile
                if (hp <= 0) {
                    ret = new String[] { "" +
                        rm.bundle.get("TILE_CURSE"),
                        rm.bundle.format("TILE_DAMAGE", dmg),
                        rm.bundle.get("TILE_DIED"),
                        rm.bundle.format("TILE_PENALTY", Util.DEATH_PENALTY)
                    };
                }
                else {
                    ret = new String[] {
                        rm.bundle.get("TILE_CURSE"),
                        rm.bundle.format("TILE_DAMAGE", dmg),
                    };
                }
            }
            else {
                int steal = 0;
                for (int i = 0; i < mapLevel; i++) {
                    steal += MathUtils.random(4, 9);
                }
                gold -= steal;
                if (gold < 0) gold = 0;
                ret = new String[] {
                    rm.bundle.get("TILE_CURSE"),
                    rm.bundle.format("TILE_LOSE_GOLD", steal)
                };
            }
        }
        else {
            ret = new String[] {
                rm.bundle.get("TILE_NOT_EFFECT")
            };
        }

        return ret;
    }

    /**
     * Sets the player's position to another teleportation tile anywhere on the map
     */
    public void teleport() {
        Tile currentTile = tileMap.getTile(tileMap.toTileCoords(position));
        Array<Tile> candidates = tileMap.getTeleportationTiles(currentTile);
        Tile choose = candidates.get(MathUtils.random(candidates.size - 1));
        position.set(tileMap.toMapCoords(choose.tilePosition));
    }

    /**
     * Adds a given amount of exp to the player's current exp and checks for level up
     */
    public void addExp(int exp) {
        // level up with no screen
        if (this.exp + exp >= maxExp) {
            int remainder = (this.exp + exp) - maxExp;
            levelUp(remainder);
            applyLevelUp();
        }
        else if (this.exp + exp < 0) {
            this.exp = 0;
        }
        else {
            this.exp += exp;
        }
    }

    public boolean isBattling() {
        return battling;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }

    public int getExp() {
        return exp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public int getHpIncrease() {
        return hpIncrease;
    }

    public void setHpIncrease(int hpIncrease) {
        this.hpIncrease = hpIncrease;
    }

    public int getMinDmgIncrease() {
        return minDmgIncrease;
    }

    public void setMinDmgIncrease(int minDmgIncrease) {
        this.minDmgIncrease = minDmgIncrease;
    }

    public int getMaxDmgIncrease() {
        return maxDmgIncrease;
    }

    public void setMaxDmgIncrease(int maxDmgIncrease) {
        this.maxDmgIncrease = maxDmgIncrease;
    }

    public int getAccuracyIncrease() {
        return accuracyIncrease;
    }

    public void setAccuracyIncrease(int accuracyIncrease) {
        this.accuracyIncrease = accuracyIncrease;
    }

    public int getMaxExpIncrease() { return maxExpIncrease; }

    public void addGold(int g) {
        if (this.gold + g < 0) this.gold = 0;
        else this.gold += g;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGold() { return gold; }

    public int getCurrentTileX() {
        return currentTileX;
    }

    public int getCurrentTileY() {
        return currentTileY;
    }

    public boolean isMoving() {
        return moving != -1;
    }

    public boolean isTileInteraction() { return tileInteraction; }

    public boolean isTeleporting() { return teleporting; }

}