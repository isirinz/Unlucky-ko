package com.trialmobile.unlucky.save;

import com.trialmobile.unlucky.battle.SpecialMoveset;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.inventory.Equipment;
import com.trialmobile.unlucky.inventory.Inventory;
import com.trialmobile.unlucky.inventory.Item;
import com.trialmobile.unlucky.inventory.ShopItem;
import com.trialmobile.unlucky.resource.Statistics;

import java.util.Arrays;

/**
 * Provides a serializable object that represents the data of the player
 * that is considered important for save files.
 *
 * @author Ming Li
 */
public class PlayerAccessor  {

    // status fields
    public int hp;
    public int maxHp;
    public int level;
    public int exp;
    public int maxExp;
    public int gold;
    public int minDamage;
    public int maxDamage;
    public int accuracy;
    public int smoveCd;

    // level save
    public int maxWorld;
    public int maxLevel;

    // inventory and equips consist of ItemAccessors to reduce unnecessary fields
    public ItemAccessor[] inventory;
    public ItemAccessor[] equips;

    // smoveset is simply an array of integers representing smove ids
    // -1 if no smove in slot
    public int[] smoveset;

    // statistics
    public Statistics stats;

    // settings
    public Settings settings;

    public PlayerAccessor() {
        inventory = new ItemAccessor[Inventory.NUM_SLOTS];
        equips = new ItemAccessor[Equipment.NUM_SLOTS];
        smoveset = new int[SpecialMoveset.MAX_MOVES];
        Arrays.fill(smoveset, -1);
    }

    /**
     * Updates the fields of this accessor with data from the player
     * @param player
     */
    public void load(Player player) {
        // load atomic fields
        this.hp = player.getHp();
        this.maxHp = player.getMaxHp();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.maxExp = player.getMaxExp();
        this.gold = player.getGold();
        this.minDamage = player.getMinDamage();
        this.maxDamage = player.getMaxDamage();
        this.accuracy = player.getAccuracy();
        this.smoveCd = player.smoveCd;
        this.maxWorld = player.maxWorld;
        this.maxLevel = player.maxLevel;

        // load inventory and equips
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            if (!player.inventory.isFreeSlot(i)) {
                Item item = player.inventory.getItem(i);
                if (item instanceof ShopItem) {
                    ShopItemAccessor sia = new ShopItemAccessor();
                    sia.load((ShopItem) item);
                    inventory[i] = sia;
                }
                else {
                    ItemAccessor ia = new ItemAccessor();
                    ia.load(item);
                    inventory[i] = ia;
                }
            }
            else {
                inventory[i] = null;
            }
        }
        for (int i = 0; i < Equipment.NUM_SLOTS; i++) {
            if (player.equips.getEquipAt(i) != null) {
                Item equip = player.equips.getEquipAt(i);
                if (equip instanceof ShopItem) {
                    ShopItemAccessor sia = new ShopItemAccessor();
                    sia.load((ShopItem) equip);
                    equips[i] = sia;
                }
                else {
                    ItemAccessor ia = new ItemAccessor();
                    ia.load(equip);
                    equips[i] = ia;
                }
            }
            else {
                equips[i] = null;
            }
        }

        Arrays.fill(smoveset, -1);
        // load smoveset
        for (int i = 0; i < player.smoveset.smoveset.size; i++) {
            smoveset[i] = player.smoveset.getMoveAt(i).id;
        }

        // statistics
        this.stats = player.stats;

        // settings
        this.settings = player.settings;
    }

}
