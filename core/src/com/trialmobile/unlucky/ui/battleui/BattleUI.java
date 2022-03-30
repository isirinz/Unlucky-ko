package com.trialmobile.unlucky.ui.battleui;

import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.event.Battle;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.screen.GameScreen;
import com.trialmobile.unlucky.ui.UI;

/**
 * Superclass for all UI related to battle events
 *
 * @author Ming Li
 */
public abstract class BattleUI extends UI {

    protected Battle battle;
    protected BattleUIHandler uiHandler;

    public BattleUI(GameScreen gameScreen, TileMap tileMap, Player player, Battle battle,
                    BattleUIHandler uiHandler, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);
        this.battle = battle;
        this.uiHandler = uiHandler;
    }

}
