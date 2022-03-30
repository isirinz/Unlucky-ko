package com.trialmobile.unlucky.inventory;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;

/**
 * An Item is held by an inventory slot and can be one of:
 * - potion (restores current hp)
 * - equip (several categories of equips)
 * - misc (some other useless thing)
 *
 * @author Ming Li
 */
public class Item {

    // id
    public String name;
    // name displayed on tooltip
    public String labelName;
    // for rendering onto tooltip
    public String desc;
    // type of item
    public String fullDesc;
    /**
     * 0 - potion
     * 1 - misc
     * 2 - helmet
     * 3 - armor
     * 4 - weapon
     * 5 - gloves
     * 6 - shoes
     * 7 - necklace
     * 8 - shield
     * 9 - ring
     * 10 - enchant scroll
     */
    public int type;

    /**
     * items are weighted with rarity meaning
     * different likelihoods to drop
     * 0 - common (60% chance out of all items)
     * 1 - rare (25% chance)
     * 2 - epic (10% chance)
     * 3 - legendary (5% chance)
     */
    public int rarity;
    private final String rarityName;

    // the range of enemy levels that can drop this item
    public int minLevel;
    public int maxLevel;

    // item stats
    // if hp is negative then its absolute value is the percentage hp that the item gives
    // used to separate percentage hp from regular hp potions
    public int hp = 0;
    public int mhp = 0;
    public int dmg = 0;
    public int acc = 0;
    public int sell = 0;
    // potions can give exp (percentage)
    public int exp = 0;

    // an item's index in the inventory
    public int index;
    // whether or not this item is equipped
    public boolean equipped = false;
    // the number of successful enchants on the item
    public int enchants = 0;
    public int enchantCost;
    // percentage bonus enchant chance from scrolls
    public int bonusEnchantChance = 0;
    // for enchant scrolls representing the bonus enchant percentage that the scroll gives
    public int eChance = 0;

    // rendering
    public Image actor;
    public int imgIndex;

    /**
     * For potions
     * Only can be consumed for hp or sold for gold
     *
     * @param name
     * @param desc
     * @param rarity
     * @param imgIndex for textureregion in spritesheet
     * @param hp
     * @param sell
     */
    public Item(ResourceManager rm, String name, String desc, int rarity, int imgIndex, int minLevel, int maxLevel, int hp, int exp, int sell) {
        this.name = name;
        this.desc = desc;
        this.rarity = rarity;
        this.imgIndex = imgIndex;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.hp = hp;
        this.exp = exp;
        this.sell = sell;
        type = 0;
        actor = new Image(rm.items20x20[0][imgIndex]);
        actor.setScale(2);
        labelName = name;
        this.rarityName = getRarityName(rm, this.rarity);
        fullDesc = initFullDesc(rm);
    }

    /**
     * For misc items
     * Only can be sold for gold
     *
     * @param name
     * @param desc
     * @param rarity
     * @param imgIndex
     * @param sell
     */
    public Item(ResourceManager rm, String name, String desc, int rarity, int imgIndex, int minLevel, int maxLevel, int sell) {
        this.name = name;
        this.desc = desc;
        this.rarity = rarity;
        this.imgIndex = imgIndex;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.sell = sell;
        type = 1;
        actor = new Image(rm.items20x20[1][imgIndex]);
        actor.setScale(2);
        labelName = name;
        this.rarityName = getRarityName(rm, this.rarity);
        fullDesc = initFullDesc(rm);
    }

    /**
     * For all types of equips
     * Gives increased stats and can be sold for gold
     *
     * @param name
     * @param desc
     * @param type
     * @param rarity
     * @param imgIndex
     * @param mhp
     * @param dmg
     * @param acc
     * @param sell
     */
    public Item(ResourceManager rm, String name, String desc, int type, int rarity, int imgIndex, int minLevel, int maxLevel,
                int mhp, int dmg, int acc, int sell) {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.rarity = rarity;
        this.imgIndex = imgIndex;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.mhp = mhp;
        this.dmg = dmg;
        this.acc = acc;
        this.sell = sell;
        actor = new Image(rm.items20x20[type][imgIndex]);
        actor.setScale(2);
        labelName = name;
        this.rarityName = getRarityName(rm, this.rarity);
        fullDesc = initFullDesc(rm);
    }

    /**
     * For enchant scrolls
     *
     * @param rm
     * @param name
     * @param desc
     * @param rarity
     * @param imgIndex
     * @param eChance
     * @param sell
     */
    public Item(ResourceManager rm, String name, String desc, int rarity, int imgIndex, int minLevel, int maxLevel, int eChance, int sell) {
        this.name = name;
        this.desc = desc;
        this.rarity = rarity;
        this.imgIndex = imgIndex;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.eChance = eChance;
        this.sell = sell;
        type = 10;
        actor = new Image(rm.items20x20[10][imgIndex]);
        actor.setScale(2);
        labelName = name;
        this.rarityName = getRarityName(rm, this.rarity);
        fullDesc = initFullDesc(rm);
    }

    /**
     * Adjusts the stats/attributes of an Item based on a given level
     * Only called once per item's existence
     *
     * @param level level
     */
    public void adjust(int level) {
        // max hp will be scaled by 5-7 parts of original item stat added on each level
        // dmg is scaled 4-6 parts of original per level
        int mhpSeed = mhp / MathUtils.random(5, 7);
        int dmgSeed = dmg / MathUtils.random(4, 6);
        // set initial enchant cost
        int enchantSeed = MathUtils.random(50, 100);
        int sellSeed = sell / MathUtils.random(10, 15);

        for (int i = 0; i < level - 1; i++) {
            mhp += mhpSeed;
            dmg += dmgSeed;
            sell += sellSeed;
        }
        for (int i = 0; i < level; i++) {
            enchantCost += enchantSeed;
        }
    }

    public String initFullDesc(ResourceManager rm) {
        String ret = "";
        if (type == 0) {
            // percentage hp potions
            if (hp < 0) ret = desc + "\n" + rm.bundle.format("ITEM_PERCENTAGE_HP_POTION", -hp);
                // exp potions
            else if (exp > 0) ret = desc + "\n" + rm.bundle.format("ITEM_EXP_POTION", exp);
            else ret = desc + "\n" + rm.bundle.format("ITEM_HP_POTION", hp);
            ret += "\n\n" + rm.bundle.get("ITEM_DOUBLE_TAP_TO_CONSUME");
        } else if (type == 1) {
            ret = desc;
        } else if (type >= 2 && type <= 9) {
            ret = desc + "\n";
            if (mhp != 0) ret += rm.bundle.format("ITEM_HP", mhp) + "\n";
            if (dmg != 0) ret += rm.bundle.format("ITEM_DAMAGE", dmg) + "\n";
            if (acc != 0) ret += rm.bundle.format("ITEM_ACCURACY", acc);
            if (bonusEnchantChance != 0) ret += "\n" + rm.bundle.format("ITEM_BONUS_ENCHANT_CHANCE", bonusEnchantChance);
        } else if (type == 10) {
            ret = desc + "\n" + rm.bundle.format("ITEM_ENCHANT_CHANCE", eChance);
            ret += "\n\n" + rm.bundle.get("ITEM_DRAG_ONTO_AN_EQUIP_TO_USE");
        }
        // remove newline from end of string if there is one
        ret = ret.trim();
        return ret;
    }
    /**
     * Returns the full description with all stats and descriptions
     * concatenated into a single string
     *
     * @return fullDescription
     */
    public String getFullDesc() {
        return fullDesc;
    }

    /**
     * Enchanting an item (equip) causes its stats (except accuracy)
     * to be multiplied by random values depending on the item's rarity
     * There is a 50% chance that the enchant succeeds
     * If it fails, there is a 40% chance that the item gets destroyed
     * "+(num success)" is added to the item's name
     * An item can be enchanted as many times as possible
     *
     * This method deals with enchant success
     *
     */
    public void enchant() {
        float multiplier = 1.f;
        bonusEnchantChance = 0;

        switch (rarity) {
            // common
            case 0:
                multiplier = MathUtils.random(Util.COMMON_ENCHANT_MIN, Util.COMMON_ENCHANT_MAX);
                break;
            // rare
            case 1:
                multiplier = MathUtils.random(Util.RARE_ENCHANT_MIN, Util.RARE_ENCHANT_MAX);
                break;
            // epic
            case 2:
                multiplier = MathUtils.random(Util.EPIC_ENCHANT_MIN, Util.EPIC_ENCHANT_MAX);
                break;
            // legendary
            case 3:
                multiplier = MathUtils.random(Util.LEGENDARY_ENCHANT_MIN, Util.LEGENDARY_ENCHANT_MAX);
                break;
        }

        mhp = (int) (mhp * multiplier);
        dmg = (int) (dmg * multiplier);
        sell = (int) (sell * multiplier);
        enchantCost = (int) (enchantCost * multiplier);

        enchants++;
        // every 5 enchants an item goes up 1 rarity
        if (enchants % 5 == 0) rarity++;
        if (rarity > 3) rarity = 3;
        // enchant number indicator
        labelName = "+" + enchants + " " + name;
    }

    private String getRarityName(ResourceManager rm, int rarity) {
        String ret = "";
        switch (rarity) {
            case 0:
                ret = rm.bundle.get("COMMON");
                break;
            case 1:
                ret = rm.bundle.get("RARE");
                break;
            case 2:
                ret = rm.bundle.get("EPIC");
                break;
            case 3:
                ret = rm.bundle.get("LEGENDARY");
                break;
        }
        return ret;
    }

    /**
     * Returns the item's name as [RARITY] [name] for dialog box
     *
     * @return
     */
    public String getDialogName() {
        return "["+rarityName+"] "+name;
    }

}
