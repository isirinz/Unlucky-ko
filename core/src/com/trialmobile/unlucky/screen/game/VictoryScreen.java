package com.trialmobile.unlucky.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.VideoCallback;
import com.trialmobile.unlucky.inventory.Item;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.map.GameMap;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.screen.AbstractScreen;

/**
 * The screen that appears after the player successfully completes a level
 * Shows time of completion, gold obtained, items obtained, exp obtained, etc
 *
 * @author Ming Li
 */
public class VictoryScreen extends AbstractScreen {

    private final Label videoLabel;
    private final TextButton videoButton;

    private final Label info;
    private static final int NUM_COLS = 5;

    private boolean isDoubleReward = false;

    private GameMap gameMap;

    public VictoryScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        // banner
        Image bannerBg = new Image(rm.skin, "default-slider");
        bannerBg.setSize(240, 36);
        bannerBg.setPosition((float)Unlucky.V_WIDTH / 2 - 140, 192);
        stage.addActor(bannerBg);

        Label bannerText = new Label(rm.bundle.get("VICTORY"), new Label.LabelStyle(rm.pixel10, new Color(0, 215 / 255.f, 0, 1)));
        bannerText.setFontScale(1.5f);
        bannerText.setSize(240, 36);
        bannerText.setPosition((float)Unlucky.V_WIDTH / 2 - 140, 192);
        bannerText.setAlignment(Align.center);
        stage.addActor(bannerText);

        // init exit button
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(rm.menuExitButton[0][0]);
        style.imageDown = new TextureRegionDrawable(rm.menuExitButton[1][0]);
        // exit button
        ImageButton exitButton = new ImageButton(style);
        exitButton.getImage().setScale(2);
        exitButton.setSize(28, 28);
        exitButton.setPosition(354, 192);
        stage.addActor(exitButton);

        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                for (Item item : new Array.ArrayIterator<>(gameMap.itemsObtained)) item.actor.remove();
                game.menuScreen.transitionIn = 0;
                setFadeScreen(game.menuScreen);
            }
        });

        // information
        Image infoBg = new Image(rm.skin, "default-slider");
        infoBg.setSize(240, 176);
        infoBg.setPosition((float)Unlucky.V_WIDTH / 2 - 140, 8);
        stage.addActor(infoBg);

        info = new Label("", new Label.LabelStyle(rm.pixel10, Color.WHITE));
        info.setFontScale(0.5f);
        info.setWrap(true);
        info.setAlignment(Align.topLeft);
        info.setSize(224, 100);
        info.setPosition((float)Unlucky.V_WIDTH / 2 - 140 + 8, 76);
        stage.addActor(info);

        ImageButton.ImageButtonStyle nextStyle = new ImageButton.ImageButtonStyle();
        nextStyle.imageUp = new TextureRegionDrawable(rm.smoveButtons[0][0]);
        nextStyle.imageDown = new TextureRegionDrawable(rm.smoveButtons[1][0]);

        // next button
        ImageButton nextButton = new ImageButton(nextStyle);
        nextButton.setPosition(314, 16);
        nextButton.getImage().setScale(2);
        stage.addActor(nextButton);

        Label nextLabel = new Label(rm.bundle.get("NEXT"), new Label.LabelStyle(rm.pixel10, Color.WHITE));
        nextLabel.setFontScale(0.5f);
        nextLabel.setTouchable(Touchable.disabled);
        nextLabel.setSize(76, 36);
        nextLabel.setAlignment(Align.center);
        nextLabel.setPosition(308, 16);
        stage.addActor(nextLabel);

        videoLabel = new Label(rm.bundle.get("DOUBLE_REWARD_FOR_WATCH_VIDEO"), new Label.LabelStyle(rm.pixel10, Color.YELLOW));
        videoLabel.setFontScale(0.75f);
        videoLabel.setTouchable(Touchable.disabled);
        videoLabel.setPosition((float)Unlucky.V_WIDTH / 2 - 114, 19);
        videoButton = new TextButton("", rm.skin);
        videoButton.setPosition((float)Unlucky.V_WIDTH / 2 - 120, 16);
        videoButton.setWidth(200);
        videoButton.setTouchable(Touchable.enabled);
        stage.addActor(videoButton);
        stage.addActor(videoLabel);

        videoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isDoubleReward) {
                    game.appInterface.showVideo(new VideoCallback() {
                        @Override
                        public void noAd() {
                            new Dialog(rm.bundle.get("DIALOG_REWARD"), rm.dialogSkin) {
                                {
                                    getTitleLabel().setFontScale(0.5f);
                                    Label l = new Label(rm.bundle.get("NO_VIDEO"), rm.dialogSkin);
                                    l.setFontScale(0.5f);
                                    l.setAlignment(Align.center);
                                    text(l);
                                    getButtonTable().defaults().width(40);
                                    getButtonTable().defaults().height(15);
                                    button(rm.bundle.get("DIALOG_OK"), "next");
                                }

                                @Override
                                protected void result(Object object) {
                                    if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                                }

                            }.show(stage).getTitleLabel().setAlignment(Align.center);
                        }

                        @Override
                        public void success(String type, int amount) {

                            game.player.addGold(gameMap.goldObtained);
                            gameMap.goldObtained += gameMap.goldObtained;

                            String infoText = rm.bundle.format("COMPLETE_TEXT", rm.worlds.get(gameMap.worldIndex).name,
                                    rm.worlds.get(gameMap.worldIndex).levels[gameMap.levelIndex].name, gameMap.time, gameMap.goldObtained, gameMap.expObtained);
                            info.setText(infoText);

                            isDoubleReward = true;
                            videoLabel.setVisible(false);
                            videoButton.setVisible(false);
                        }
                    });
                }
            }
        });

        nextButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameMap.levelIndex != rm.worlds.get(gameMap.worldIndex).numLevels - 1) {
                    // switch back to level select screen
                    for (Item item : new Array.ArrayIterator<>(gameMap.itemsObtained)) item.actor.remove();
                    game.levelSelectScreen.setWorld(gameMap.worldIndex);
                    rm.menuTheme.play();
                    setFadeScreen(game.levelSelectScreen);
                }
            }
        });
    }

    public void init(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    @Override
    public void show() {
        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        Gdx.input.setInputProcessor(stage);

        isDoubleReward = false;
        batchFade = true;

        renderBatch = false;
        // fade in animation
        stage.addAction(Actions.sequence(Actions.alpha(0), Actions.run(new Runnable() {
            @Override
            public void run() {
                renderBatch = true;
            }
        }), Actions.fadeIn(0.5f)));

        String infoText = rm.bundle.format("COMPLETE_TEXT", rm.worlds.get(gameMap.worldIndex).name,
                rm.worlds.get(gameMap.worldIndex).levels[gameMap.levelIndex].name, gameMap.time, gameMap.goldObtained, gameMap.expObtained);
        info.setText(infoText);

        // show items obtained's image actors in a grid
        for (int i = 0; i < gameMap.itemsObtained.size; i++) {
            int x = i % NUM_COLS;
            int y = i / NUM_COLS;
            Item item = gameMap.itemsObtained.get(i);
            item.actor.remove();
            item.actor.setScale(2);
            item.actor.setPosition((float)Unlucky.V_WIDTH / 2 - 140 + 16 + (x * 48), 68 - (y * 32));
            stage.addActor(item.actor);
        }
    }

    public void update(float dt) {}

    public void render(float dt) {
        update(dt);

        if (renderBatch) {
            stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
            stage.getBatch().begin();

            // fix fading
            if (batchFade) stage.getBatch().setColor(Color.WHITE);

            // render world background corresponding to the selected world
            //stage.getBatch().draw(rm.worldSelectBackgrounds[gameMap.worldIndex], 0, 0);
            stage.getBatch().draw(rm.worldSelectBackgrounds[0], 0, 0, Unlucky.V_WIDTH, Unlucky.V_HEIGHT);

            stage.getBatch().end();
        }

        super.render(dt);
    }

}
