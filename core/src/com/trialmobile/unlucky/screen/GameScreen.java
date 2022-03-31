package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.trialmobile.unlucky.event.Battle;
import com.trialmobile.unlucky.event.EventState;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.map.GameMap;
import com.trialmobile.unlucky.parallax.Background;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.screen.game.DialogScreen;
import com.trialmobile.unlucky.screen.game.LevelUpScreen;
import com.trialmobile.unlucky.screen.game.TransitionScreen;
import com.trialmobile.unlucky.ui.Hud;
import com.trialmobile.unlucky.ui.battleui.BattleUIHandler;

/**
 * Handles all gameplay.
 *
 * @author Ming Li
 */
public class GameScreen extends AbstractScreen {

    public EventState currentEvent;

    public GameMap gameMap;
    public Hud hud;
    public BattleUIHandler battleUIHandler;
    public Battle battle;
    public TransitionScreen transition;
    public LevelUpScreen levelUp;
    public DialogScreen dialog;

    // input
    public InputMultiplexer multiplexer;

    // battle background
    private final Background[] bg;

    // key
    private int worldIndex;
    private int levelIndex;

    // whether or not to reset the game map on show
    // used for transitioning between screen during a pause
    public boolean resetGame = true;

    public GameScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        currentEvent = EventState.MOVING;

        gameMap = new GameMap(this, game.player, rm);
        battle = new Battle(this, gameMap.tileMap, gameMap.player);
        hud = new Hud(this, gameMap.tileMap, gameMap.player, rm);
        battleUIHandler = new BattleUIHandler(this, gameMap.tileMap, gameMap.player, battle, rm);
        transition = new TransitionScreen(this, battle, battleUIHandler, hud, gameMap.player, rm);
        levelUp = new LevelUpScreen(this, gameMap.tileMap, gameMap.player, rm);
        dialog = new DialogScreen(this, gameMap.tileMap, gameMap.player, rm);

        // create bg
        bg = new Background[2];
        // sky
        bg[0] = new Background((OrthographicCamera) battleUIHandler.getStage().getCamera(), new Vector2(0.3f, 0));
        // field
        bg[1] = new Background((OrthographicCamera) battleUIHandler.getStage().getCamera(), new Vector2(0, 0));


        // input multiplexer
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(battleUIHandler.getStage());
        multiplexer.addProcessor(levelUp.getStage());
        multiplexer.addProcessor(dialog.getStage());
    }

    public void init(int worldIndex, int levelIndex) {
        this.worldIndex = worldIndex;
        this.levelIndex = levelIndex;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(multiplexer);
        batchFade = renderBatch = true;

        // fade in animation
        hud.getStage().addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.5f)));

        if (resetGame) {
            // init tile map
            setCurrentEvent(EventState.MOVING);
            hud.deathGroup.setVisible(false);
            gameMap.init(worldIndex, levelIndex);
            gameMap.player.moving = -1;
            battle.tileMap = gameMap.tileMap;
            hud.setTileMap(gameMap.tileMap);
            battleUIHandler.setTileMap(gameMap.tileMap);
            levelUp.setTileMap(gameMap.tileMap);
            dialog.setTileMap(gameMap.tileMap);

            // update bg
            createBackground(gameMap.worldIndex);

            hud.toggle(true);
            hud.touchDown = false;
            hud.shade.setVisible(false);
            hud.startLevelDescriptor();
        }
    }

    /**
     * Creates the dynamic background
     * @param bgIndex is the theme of bg
     */
    private void createBackground(int bgIndex) {
        // background image array is ordered by depth
        TextureRegion[] images = rm.battleBackgrounds400x240[bgIndex];
        for (int i = 0; i < 2; i++) bg[i].setImage(images[i]);
        // set background movement for the specific worlds
        if (bgIndex == 0) bg[0].setVector(40, 0);
        else if (bgIndex == 1) bg[0].setVector(0, 0);
        else if (bgIndex == 2) bg[0].setVector(40, 0);
        bg[1].setVector(0, 0);
    }

    /**
     * When the player dies it shows a "click to continue" message along with what they lost
     */
    public void die() {
        // reset player's hp after dying
        gameMap.player.setHp(gameMap.player.getMaxHp());
        setCurrentEvent(EventState.DEATH);
        hud.toggle(false);
        hud.deathGroup.setVisible(true);
    }

    /**
     * Updates the camera position to follow the player unless he's on the edges of the map
     */
    public void updateCamera() {
        int size = 32;
        // camera directs on the player
        if (gameMap.player.getPosition().x <= gameMap.tileMap.mapWidth * size - 7 * size &&
            gameMap.player.getPosition().x >= 6 * size)
            cam.position.x = gameMap.player.getPosition().x + 16;
        if (gameMap.player.getPosition().y <= gameMap.tileMap.mapHeight * size - 4 * size &&
            gameMap.player.getPosition().y >= 4 * size - 16)
            cam.position.y = gameMap.player.getPosition().y + 8;
        cam.update();

        if (gameMap.player.getPosition().x < 6 * size) cam.position.x = 208;
        if (gameMap.player.getPosition().y < 4 * size - 16) cam.position.y = 121f;
        if (gameMap.player.getPosition().x > gameMap.tileMap.mapWidth * size - 7 * size)
            cam.position.x = (gameMap.tileMap.mapWidth * size - 7 * size) + 16;
        if (gameMap.player.getPosition().y > gameMap.tileMap.mapHeight * size - 4 * size)
            cam.position.y = (gameMap.tileMap.mapHeight * size - 4 * size) + 8;
    }

    public void update(float dt) {
        if (currentEvent != EventState.PAUSE) {
            // update game time
            gameMap.time += dt;
        }

        if (currentEvent == EventState.MOVING) {
            updateCamera();

            gameMap.update(dt);
            hud.update(dt);
        }

        if (currentEvent == EventState.BATTLING) {
            // update bg
            for (Background background : bg) {
                background.update(dt);
            }
            battleUIHandler.update(dt);
        }

        if (currentEvent == EventState.TRANSITION) transition.update(dt);
        if (currentEvent == EventState.LEVEL_UP) levelUp.update(dt);
        if (currentEvent == EventState.TILE_EVENT) dialog.update(dt);
        if (currentEvent == EventState.INVENTORY) game.inventoryUI.update(dt);
    }

    public void render(float dt) {
        update(dt);

        // clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderBatch) {
            game.batch.begin();

            // fix fading
            if (batchFade) game.batch.setColor(Color.WHITE);

            if (currentEvent == EventState.BATTLING || transition.renderBattle) {
                // bg camera
                game.batch.setProjectionMatrix(battleUIHandler.getStage().getCamera().combined);
                for (Background background : bg) {
                    background.render(game.batch);
                }
            }

            if (currentEvent == EventState.MOVING || currentEvent == EventState.INVENTORY ||
                transition.renderMap || currentEvent == EventState.TILE_EVENT ||
                currentEvent == EventState.DEATH || currentEvent == EventState.PAUSE) {
                // map camera
                game.batch.setProjectionMatrix(cam.combined);
                // render map and player
                gameMap.render(dt, game.batch, cam);
            }

            game.batch.end();
        }

        if (currentEvent == EventState.MOVING || currentEvent == EventState.DEATH || currentEvent == EventState.PAUSE)
            hud.render(dt);
        if (currentEvent == EventState.BATTLING || transition.renderBattle)
            battleUIHandler.render(dt);
        if (currentEvent == EventState.LEVEL_UP || transition.renderLevelUp)
            levelUp.render(dt);
        if (currentEvent == EventState.TILE_EVENT) dialog.render(dt);
        if (currentEvent == EventState.INVENTORY) game.inventoryUI.render(dt);
        if (currentEvent == EventState.TRANSITION) transition.render(dt);

        game.profile("LevelUpScreen");
    }

    public void dispose() {
        super.dispose();
        hud.dispose();
        battleUIHandler.dispose();
        dialog.dispose();
        levelUp.dispose();
    }

    /**
     * Add some sort of transitioning between events
     * @param event event
     */
    public void setCurrentEvent(EventState event) {
        this.currentEvent = event;
    }

}