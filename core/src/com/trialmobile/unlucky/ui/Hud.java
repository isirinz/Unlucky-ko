package com.trialmobile.unlucky.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.effects.Moving;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.event.EventState;
import com.trialmobile.unlucky.inventory.Inventory;
import com.trialmobile.unlucky.inventory.Item;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.map.WeatherType;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.screen.GameScreen;

/**
 * Handles button input and everything not in the game camera
 *
 * @author Ming Li
 */
public class Hud extends UI {

    // directional pad: index i 0 - down, 1 - up, 2 - right, 3 - left
    private ImageButton[] dirPad;
    // if dir pad is held down
    public boolean touchDown = false;
    public int dirIndex = -1;
    // for changing the player's facing direction with a short tap like in pokemon
    private float dirTime = 0;

    // option buttons: inventoryUI and settings
    private ImageButton[] optionButtons;

    // window that slides on the screen to show the world and level
    private Window levelDescriptor;
    private Label levelDesc;
    private Moving levelMoving;
    private boolean ld = false;
    private float showTime = 0;

    // death screen that is a prompt with the map background dimmed out
    public Group deathGroup;
    private Label loss;

    public Image shade;
    public Dialog settingsDialog;

    public Hud(final GameScreen gameScreen, TileMap tileMap, final Player player, final ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);

        createDirPad();
        createOptionButtons();
        createLevelDescriptor();
        createDeathPrompt();

        shade = new Image(rm.shade);
        shade.setScale(2);
        shade.setVisible(false);
        shade.setTouchable(Touchable.disabled);
        stage.addActor(shade);

        settingsDialog = new Dialog(rm.bundle.get("PAUSED"), rm.dialogSkin) {
            {
                getTitleLabel().setFontScale(0.5f);
                getButtonTable().defaults().width(100);
                getButtonTable().defaults().height(30);
                TextButton b = new TextButton(rm.bundle.get("BUTTON_Back"), rm.dialogSkin);
                b.getLabel().setFontScale(0.5f);
                button(b, "back");
                getButtonTable().padTop(-6).row();
                TextButton s = new TextButton(rm.bundle.get("BUTTON_Settings"), rm.dialogSkin);
                s.getLabel().setFontScale(0.5f);
                button(s, "settings");
                getButtonTable().row();
                TextButton q = new TextButton(rm.bundle.get("BUTTON_Quit"), rm.dialogSkin);
                q.getLabel().setFontScale(0.5f);
                button(q, "quit");
            }
            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                if (object.equals("back")) {
                    shade.setVisible(false);
                    toggle(true);

                    // play music and sfx
                    if (!player.settings.muteMusic) gameScreen.gameMap.mapTheme.play();
                    if (!player.settings.muteSfx) {
                        if (gameScreen.gameMap.weather == WeatherType.RAIN) {
                            gameScreen.gameMap.soundId = rm.lightrain.play(player.settings.sfxVolume);
                            rm.lightrain.setLooping(gameScreen.gameMap.soundId, true);
                        }
                        else if (gameScreen.gameMap.weather == WeatherType.HEAVY_RAIN || gameScreen.gameMap.weather == WeatherType.THUNDERSTORM) {
                            gameScreen.gameMap.soundId = rm.heavyrain.play(player.settings.sfxVolume);
                            rm.heavyrain.setLooping(gameScreen.gameMap.soundId, true);
                        }
                    }

                    gameScreen.setCurrentEvent(EventState.MOVING);
                }
                else if (object.equals("settings")) {
                    game.settingsScreen.inGame = true;
                    game.settingsScreen.worldIndex = gameScreen.gameMap.worldIndex;
                    if (gameScreen.isClickable()) {
                        gameScreen.setClickable(false);
                        gameScreen.setBatchFade(false);
                        // fade out animation
                        stage.addAction(Actions.sequence(Actions.fadeOut(0.3f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    gameScreen.setClickable(true);
                                    game.setScreen(game.settingsScreen);
                                }
                            })));
                    }
                }
                else if (object.equals("quit")) {
                    quit();
                }
            }
        };
        settingsDialog.getTitleLabel().setAlignment(Align.center);
        settingsDialog.getBackground().setMinWidth(70);
    }

    public void update(float dt) {
        if (touchDown) {
            dirTime += dt;
            // quick tap to change direction
            if (dirTime > 0 && dirTime <= 0.15f) player.getAm().setAnimation(dirIndex);
            // move the player
            else movePlayer(dirIndex);
        }
        else {
            player.getAm().stopAnimation();
        }

        if (ld) {
            levelMoving.update(dt);
            levelDescriptor.setPosition(levelMoving.position.x, levelMoving.position.y);
            if (!levelMoving.shouldStart) showTime += dt;
            // after 3 seconds of showing moves back to the starting position
            if (showTime >= 3) {
                showTime = 0;
                float y = 232 - levelDescriptor.getPrefHeight();
                levelMoving.origin.set(4, y);
                levelMoving.target.set(-levelDescriptor.getPrefWidth(), y);
                levelMoving.start();
            }
            if (levelMoving.target.x == -levelDescriptor.getPrefWidth() &&
                levelMoving.position.x == levelMoving.target.x) {
                ld = false;
                showTime = 0;
                levelDescriptor.setVisible(false);
            }
        }
    }

    public void render(float dt) {
        stage.act(dt);
        stage.draw();
    }

    /**
     * Starts the slide in animation of the level descriptor when
     * the player first enters the level.
     */
    public void startLevelDescriptor() {
        int worldIndex = gameScreen.gameMap.worldIndex;
        int levelIndex = gameScreen.gameMap.levelIndex;
        String worldName = rm.worlds.get(worldIndex).name;
        String levelName = rm.worlds.get(worldIndex).levels[levelIndex].name;

        levelDescriptor.getTitleLabel().setText("WORLD " + (worldIndex + 1) + " : " + "LEVEL " + (levelIndex + 1));
        levelDesc.setText(worldName + "\n" + levelName + "\n" + "AVG LVL: " + rm.worlds.get(worldIndex).levels[levelIndex].avgLevel);
        levelDescriptor.setVisible(true);
        levelDescriptor.pack();

        float y = 232 - levelDescriptor.getPrefHeight();
        levelMoving.origin.set(-levelDescriptor.getPrefWidth(), y);
        levelMoving.target.set(4, y);

        ld = true;
        levelMoving.start();
    }

    /**
     * Turns the HUD on and off when another event occurs
     *
     * @param toggle toggle
     */
    public void toggle(boolean toggle) {
        if (toggle) {
            gameScreen.getGame().fps.setPosition(5, 5);
            stage.addActor(gameScreen.getGame().fps);
        }
        for (int i = 0; i < 4; i++) dirPad[i].setVisible(toggle);
        for (int i = 0; i < 2; i++) optionButtons[i].setVisible(toggle);
        levelDescriptor.setVisible(toggle);
    }

    /**
     * Draws the directional pad and applies Drawable effects
     * Unfortunately have to do each one separately
     */
    private void createDirPad() {
        dirPad = new ImageButton[4];

        // when each button is pressed it changes for a more visible effect
        ImageButton.ImageButtonStyle[] styles = rm.loadImageButtonStyles(4, rm.dirpad20x20);

        // down
        dirPad[0] = new ImageButton(styles[0]);
        dirPad[0].getImage().setScale(2);
        dirPad[0].setPosition(Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET, Util.DIR_PAD_OFFSET);
        // up
        dirPad[1] = new ImageButton(styles[1]);
        dirPad[1].getImage().setScale(2);
        dirPad[1].setPosition(Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET, (Util.DIR_PAD_SIZE * 2) + Util.DIR_PAD_OFFSET);
        // right
        dirPad[2] = new ImageButton(styles[2]);
        dirPad[2].getImage().setScale(2);
        dirPad[2].setPosition((Util.DIR_PAD_SIZE * 2) + Util.DIR_PAD_OFFSET, Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET);
        // left
        dirPad[3] = new ImageButton(styles[3]);
        dirPad[3].getImage().setScale(2);
        dirPad[3].setPosition(Util.DIR_PAD_OFFSET, Util.DIR_PAD_SIZE + Util.DIR_PAD_OFFSET);

        handleDirPadEvents();

        for (ImageButton imageButton : dirPad) {
            stage.addActor(imageButton);
        }
    }

    /**
     * Creates the two option buttons: inventoryUI and settings
     */
    private void createOptionButtons() {
        optionButtons = new ImageButton[2];

        ImageButton.ImageButtonStyle[] styles = rm.loadImageButtonStyles(2, rm.optionbutton32x32);
        for (int i = 0; i < 2; i++) {
            optionButtons[i] = new ImageButton(styles[i]);
            optionButtons[i].setPosition(310 + (i * 50), 200);
            optionButtons[i].getImage().setScale(2);
            stage.addActor(optionButtons[i]);
        }
        handleOptionEvents();
    }

    /**
     * Creates the level descriptor window
     */
    private void createLevelDescriptor() {
        levelDescriptor = new Window("", rm.skin);
        levelDescriptor.getTitleLabel().setFontScale(0.5f);
        levelDescriptor.getTitleLabel().setAlignment(Align.center);
        levelDescriptor.setMovable(false);
        levelDescriptor.setTouchable(Touchable.disabled);
        levelDescriptor.setKeepWithinStage(false);
        levelDescriptor.setVisible(false);
        levelDesc = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        levelDesc.setFontScale(0.5f);
        levelDesc.setAlignment(Align.center);
        levelDescriptor.left();
        levelDescriptor.padTop(24);
        levelDescriptor.padLeft(4);
        levelDescriptor.padBottom(8);
        levelDescriptor.add(levelDesc).width(140);
        stage.addActor(levelDescriptor);
        levelMoving = new Moving(new Vector2(), new Vector2(), 150.f);
    }

    /**
     * Creates the death screen message
     */
    private void createDeathPrompt() {
        deathGroup = new Group();
        deathGroup.setTransform(false);
        deathGroup.setVisible(false);
        deathGroup.setSize(Unlucky.V_WIDTH, Unlucky.V_HEIGHT);
        deathGroup.setTouchable(Touchable.enabled);

        Image dark = new Image(rm.shade);
        dark.setScale(2);
        deathGroup.addActor(dark);

        Image frame = new Image(rm.skin, "textfield");
        frame.setSize(200, 120);
        frame.setPosition((float)Unlucky.V_WIDTH / 2 - 100, (float)Unlucky.V_HEIGHT / 2 - 60);
        deathGroup.addActor(frame);

        Label youDied = new Label(rm.bundle.get("YOU_DIED"), new Label.LabelStyle(rm.pixel10, Color.RED));
        youDied.setSize(200, 20);
        youDied.setPosition(100, 150);
        youDied.setAlignment(Align.center);
        youDied.setTouchable(Touchable.disabled);
        deathGroup.addActor(youDied);

        loss = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        loss.setFontScale(0.5f);
        loss.setWrap(true);
        loss.setSize(200, 80);
        loss.setAlignment(Align.top);
        loss.setPosition((float)Unlucky.V_WIDTH / 2 - 100, (float)Unlucky.V_HEIGHT / 2 - 60);
        loss.setTouchable(Touchable.disabled);
        deathGroup.addActor(loss);

        // click to continue
        deathGroup.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                backToMenu();
            }
        });

        stage.addActor(deathGroup);
    }

    /**
     * Death text displays the items the player would've got, the gold and exp lost
     * @param text text
     */
    public void setDeathText(String text) {
        loss.setText(text);
    }

    private void backToMenu() {
        game.menuScreen.transitionIn = 0;
        if (gameScreen.gameMap.weather != WeatherType.NORMAL) {
            rm.lightrain.stop(gameScreen.gameMap.soundId);
            rm.heavyrain.stop(gameScreen.gameMap.soundId);
        }
        if (gameScreen.isClickable()) {
            gameScreen.setClickable(false);
            gameScreen.setBatchFade(false);
            // fade out animation
            stage.addAction(Actions.sequence(Actions.fadeOut(0.3f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        gameScreen.setClickable(true);
                        gameScreen.gameMap.mapTheme.stop();
                        game.setScreen(game.menuScreen);
                    }
                })));
        }
    }

    private void loseObtained() {
        player.addGold(-gameScreen.gameMap.goldObtained);
        player.addExp(-gameScreen.gameMap.expObtained);
        if (gameScreen.gameMap.itemsObtained.size != 0) {
            for (Item item : new Array.ArrayIterator<>(gameScreen.gameMap.itemsObtained)) {
                for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
                    if (player.inventory.getItem(i) == item)
                        player.inventory.removeItem(i);
                }
            }
        }
    }

    /**
     * Player quits the level and returns to the main menu screen
     * The player will lose all gold, exp, and items obtained during the level
     */
    private void quit() {
        final String text = rm.bundle.get("QUIT_TEXT");
            new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
            {
                Label l = new Label(text, rm.dialogSkin);
                l.setFontScale(0.5f);
                l.setAlignment(Align.center);
                text(l);
                getButtonTable().defaults().width(40);
                getButtonTable().defaults().height(15);
                button(rm.bundle.get("DIALOG_YES"), "yes");
                button(rm.bundle.get("DIALOG_NO"), "no");
            }
            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                if (object.equals("yes")) {
                    loseObtained();
                    player.setHp(player.getMaxHp());
                    player.inMap = false;
                    if (gameScreen.gameMap.weather != WeatherType.NORMAL) {
                        rm.lightrain.stop(gameScreen.gameMap.soundId);
                        rm.heavyrain.stop(gameScreen.gameMap.soundId);
                    }
                    backToMenu();
                }
                else settingsDialog.show(stage);
            }
        }.show(stage).getTitleLabel().setAlignment(Align.center);
    }

    /**
     * Handles player movement commands
     */
    private void handleDirPadEvents() {
        for (int i = 0; i < 4; i++) {
            final int index = i;
            dirPad[i].addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    dirTime = 0;
                    touchDown = true;
                    dirIndex = index;
                    return true;
                }
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    touchDown = false;
                }
            });
        }
    }

    private void movePlayer(int dir) {
        if (player.canMove()) player.getAm().setAnimation(dir);
        if (player.canMove() && !player.nextTileBlocked(dir)) {
            player.move(dir);
        }
    }

    /**
     * Handles two option button commands
     */
    private void handleOptionEvents() {
        // inventoryUI
        optionButtons[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle(false);
                gameScreen.setCurrentEvent(EventState.INVENTORY);
                gameScreen.getGame().inventoryUI.init(false, null);
                gameScreen.getGame().inventoryUI.start();
            }
        });

        // settings
        optionButtons[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                shade.setVisible(true);
                toggle(false);

                // pause music and sfx
                gameScreen.gameMap.mapTheme.pause();
                if (gameScreen.gameMap.weather != WeatherType.NORMAL) {
                    rm.lightrain.stop(gameScreen.gameMap.soundId);
                    rm.heavyrain.stop(gameScreen.gameMap.soundId);
                }

                gameScreen.setCurrentEvent(EventState.PAUSE);
                settingsDialog.show(stage);
            }
        });
    }

}