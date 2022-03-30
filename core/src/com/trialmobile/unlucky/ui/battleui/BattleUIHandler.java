package com.trialmobile.unlucky.ui.battleui;

import com.badlogic.gdx.math.MathUtils;
import com.trialmobile.unlucky.event.BattleState;
import com.trialmobile.unlucky.ui.UI;
import com.trialmobile.unlucky.entity.enemy.Boss;
import com.trialmobile.unlucky.entity.enemy.Enemy;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.event.*;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.screen.GameScreen;

/**
 * Handles all UI for battle scenes
 *
 * @author Ming Li
 */
public class BattleUIHandler extends UI {

    public MoveUI moveUI;
    public BattleEventHandler battleEventHandler;
    public BattleScene battleScene;

    // battle
    public BattleState currentState;

    public BattleUIHandler(GameScreen gameScreen, TileMap tileMap, Player player, Battle battle, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);

        currentState = BattleState.NONE;

        battleScene = new BattleScene(gameScreen, tileMap, player, battle, this, stage, rm);
        moveUI = new MoveUI(gameScreen, tileMap, player, battle, this, stage, rm);
        battleEventHandler = new BattleEventHandler(gameScreen, tileMap, player, battle, this, stage, rm);

        moveUI.toggleMoveAndOptionUI(false);
        battleEventHandler.endDialog();
    }

    public void update(float dt) {
        if (currentState == BattleState.MOVE) moveUI.update(dt);
        if (currentState == BattleState.DIALOG) battleEventHandler.update(dt);
        battleScene.update(dt);
    }

    public void render(float dt) {
        battleScene.render(dt);

        stage.act(dt);
        stage.draw();

        if (currentState == BattleState.MOVE) moveUI.render(dt);
        if (currentState == BattleState.DIALOG) battleEventHandler.render(dt);
    }

    /**
     * When the player first encounters the enemy and engages in battle
     * There's a 1% chance that the enemy doesn't want to fight
     *
     * @param enemy enemy
     */
    public void engage(Enemy enemy) {
        player.setDead(false);
        moveUI.init();
        battleScene.resetPositions();
        battleScene.toggle(true);
        currentState = BattleState.DIALOG;

        String[] intro;
        boolean saved = Util.isSuccess(Util.SAVED_FROM_BATTLE);

        if (enemy.isElite()) player.stats.eliteEncountered++;
        else if (enemy.isBoss()) player.stats.bossEncountered++;

        if (enemy.isBoss()) {
            if (MathUtils.randomBoolean()) {
                intro = new String[] {
                        rm.bundle.format("ENCOUNTER_BOSS1", enemy.getId()),
                        rm.bundle.get("ENCOUNTER_BOSS2"),
                        rm.bundle.format("ENCOUNTER_PASSIVE", ((Boss) enemy).getPassiveDescription())
                };
                battleEventHandler.startDialog(intro, BattleEvent.NONE, BattleEvent.PLAYER_TURN);
            } else {
                intro = new String[] {
                        rm.bundle.format("ENCOUNTER_BOSS1", enemy.getId()),
                        rm.bundle.get("ENCOUNTER_BOSS2"),
                        rm.bundle.format("ENCOUNTER_PASSIVE", ((Boss) enemy).getPassiveDescription()),
                        rm.bundle.format("ENCOUNTER_FIRST", enemy.getId()),
                };
                battleEventHandler.startDialog(intro, BattleEvent.NONE, BattleEvent.ENEMY_TURN);
            }
        }
        else {
            if (saved) {
                intro = new String[]{
                        rm.bundle.format("ENCOUNTER_ENEMY", enemy.getId()),
                        rm.bundle.get("ENCOUNTER_ENEMY_FLEE")
                };
                battleEventHandler.startDialog(intro, BattleEvent.NONE, BattleEvent.END_BATTLE);
            } else {
                // 50-50 chance for first attack from enemy or player
                if (MathUtils.randomBoolean()) {
                    intro = new String[]{
                            rm.bundle.format("ENCOUNTER_ENEMY", enemy.getId()),
                            rm.bundle.get("ENCOUNTER_ENEMY_BATTLE")
                    };
                    battleEventHandler.startDialog(intro, BattleEvent.NONE, BattleEvent.PLAYER_TURN);
                } else {
                    intro = new String[]{
                            rm.bundle.format("ENCOUNTER_ENEMY", enemy.getId()),
                            rm.bundle.get("ENCOUNTER_ENEMY_BATTLE"),
                            rm.bundle.format("ENCOUNTER_FIRST", enemy.getId())
                    };
                    battleEventHandler.startDialog(intro, BattleEvent.NONE, BattleEvent.ENEMY_TURN);
                }
            }
        }
    }

}
