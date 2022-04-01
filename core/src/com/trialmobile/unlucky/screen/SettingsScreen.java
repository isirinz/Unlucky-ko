package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.resource.ResourceManager;

/**
 *
 * Screen that allows the player to modify settings of the game such as
 * music and sfx volume, toggle animations, show fps, etc.
 * Can be accessed either in game while paused or through the main menu.
 *
 * @author Ming Li
 */
public class SettingsScreen extends MenuExtensionScreen {

    // to be set when the player accesses the screen from in game
    public int worldIndex;

    // whether or not the player is accessing settings from in game
    public boolean inGame = false;

    private Slider musicSlider;
    private Slider sfxSlider;
    private CheckBox muteMusic;
    private CheckBox muteSfx;
    private CheckBox showEnemyLevels;
    private CheckBox showWeatherAnims;
    private CheckBox showFps;
    private SelectBox<String> language;

    Preferences prefs;

    public SettingsScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        prefs = Gdx.app.getPreferences("Unlucky Preference");

        // exit button
        stage.addActor(exitButton);
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                if (inGame) {
                    game.gameScreen.resetGame = false;
                    setFadeScreen(game.gameScreen);
                    game.gameScreen.hud.settingsDialog.show(game.gameScreen.hud.getStage());
                }
                else {
                    game.menuScreen.transitionIn = 2;
                    setSlideScreen(game.menuScreen, false);
                }
            }
        });

        // create title label
        // ui
        Image banner = new Image(rm.skin, "default-slider");
        banner.setPosition(16, 204);
        banner.setSize(328, 24);
        stage.addActor(banner);

        Label bannerLabel = new Label(rm.bundle.get("SETTINGS"), rm.skin);
        bannerLabel.setStyle(new Label.LabelStyle(rm.pixel10, new Color(1, 212 / 255.f, 0, 1)));
        bannerLabel.setSize(100, 24);
        bannerLabel.setTouchable(Touchable.disabled);
        bannerLabel.setPosition(28, 204);
        bannerLabel.setAlignment(Align.left);
        stage.addActor(bannerLabel);

        Image bg = new Image(rm.skin, "default-slider");
        bg.setPosition(16, 16);
        bg.setSize(368, 176);
        stage.addActor(bg);

        Label.LabelStyle white = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        Label description = new Label("SOUND                                 MISC",
                new Label.LabelStyle(rm.pixel10, new Color(1, 212 / 255.f, 0, 1)));
        description.setFontScale(0.75f);
        description.setTouchable(Touchable.disabled);
        description.setPosition(28, 170);
        stage.addActor(description);

        // create settings labels
        Label[] settingLabels = new Label[8];
        String[] settingStrs = new String[] {
            "MUSIC VOLUME", "SFX VOLUME", "MUTE MUSIC:", "MUTE SFX:",
            "SHOW ENEMY LEVELS:", "WEATHER ANIMATIONS:", "SHOW FPS:", "LANGUAGE:"
        };
        for (int i = 0; i < 8; i++) {
            settingLabels[i] = new Label(settingStrs[i], white);
            settingLabels[i].setTouchable(Touchable.disabled);
            settingLabels[i].setFontScale(0.6f);
            stage.addActor(settingLabels[i]);
        }
        for (int i = 0; i < 2; i++) settingLabels[i].setPosition(28, 152 - i * 48);
        for (int i = 2; i < 4; i++) settingLabels[i].setPosition(28, 52 - (i - 2) * 28);
        for (int i = 4; i < 8; i++) settingLabels[i].setPosition(222, 144 - (i - 4) * 32);

        createSliders();
        createCheckboxes();
    }

    /**
     * Creates the volume sliders for the music and sound effects
     */
    private void createSliders() {
        musicSlider = new Slider(0.f, 1.f, 0.02f, false, rm.skin);
        musicSlider.setPosition(28, 128);
        musicSlider.setSize(150, 20);
        stage.addActor(musicSlider);

        sfxSlider = new Slider(0.f, 1.f, 0.02f, false, rm.skin);
        sfxSlider.setPosition(28, 80);
        sfxSlider.setSize(150, 20);
        stage.addActor(sfxSlider);

        // slider events
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // save to player's settings
                game.player.settings.musicVolume = musicSlider.getValue();
                if (!game.player.settings.muteMusic) rm.setMusicVolume(musicSlider.getValue());
                if (!inGame) if (!musicSlider.isDragging()) game.save.save();
            }
        });
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.player.settings.sfxVolume = sfxSlider.getValue();
                if (!inGame) if (!sfxSlider.isDragging()) game.save.save();
            }
        });
    }

    /**
     * Creates the checkboxes for the toggle settings
     */
    private void createCheckboxes() {
        muteMusic = new CheckBox("", rm.skin);
        muteMusic.setPosition(100, 50);
        stage.addActor(muteMusic);

        muteSfx = new CheckBox("", rm.skin);
        muteSfx.setPosition(100, 20);
        stage.addActor(muteSfx);

        showEnemyLevels = new CheckBox("", rm.skin);
        showEnemyLevels.setPosition(340, 142);
        stage.addActor(showEnemyLevels);
        showWeatherAnims = new CheckBox("", rm.skin);
        showWeatherAnims.setPosition(340, 110);
        stage.addActor(showWeatherAnims);
        showFps = new CheckBox("", rm.skin);
        showFps.setPosition(340, 78);
        stage.addActor(showFps);

        final String [] languages = { "ENGLISH", "KOREAN" };

        language = new SelectBox<>(rm.skin);
        language.setPosition(280, 46);
        language.setItems(languages);
        language.pack();
        stage.addActor(language);

        // checkbox events
        muteMusic.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                game.player.settings.muteMusic = muteMusic.isChecked();
                if (muteMusic.isChecked()) rm.setMusicVolume(0f);
                else rm.setMusicVolume(game.player.settings.musicVolume);
                if (!inGame) game.save.save();
            }
        });
        muteSfx.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                game.player.settings.muteSfx = muteSfx.isChecked();
                if (!inGame) game.save.save();
            }
        });
        showEnemyLevels.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                game.player.settings.showEnemyLevels = showEnemyLevels.isChecked();
                if (!inGame) game.save.save();
            }
        });
        showWeatherAnims.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                game.player.settings.showWeatherAnimations = showWeatherAnims.isChecked();
                if (inGame) {
                    if (showWeatherAnims.isChecked()) game.gameScreen.gameMap.setWeather(game.gameScreen.gameMap.tileMap.weather);
                    else game.gameScreen.gameMap.setWeather(0);
                }
                if (!inGame) game.save.save();
            }
        });
        showFps.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                game.player.settings.showFps = showFps.isChecked();
                game.fps.setVisible(showFps.isChecked());
                if (!inGame) game.save.save();
            }
        });
        language.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                boolean isChanged = false;
                String old = prefs.getString("lang", "ko");
                if ("ENGLISH".equalsIgnoreCase(language.getSelected())) {
                    if (!old.equals("en")) {
                        isChanged = true;
                    }
                    prefs.putString("lang", "en");
                } else {
                    if (!old.equals("ko")) {
                        isChanged = true;
                    }
                    prefs.putString("lang", "ko");
                }
                prefs.flush();
                if (!inGame) game.save.save();

                if (isChanged) {
                    new Dialog(rm.bundle.get("DIALOG_RESTART_TITLE"), rm.dialogSkin) {
                        {
                            getTitleLabel().setFontScale(0.5f);
                            Label l = new Label(rm.bundle.format("DIALOG_RESTART"), rm.dialogSkin);
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
            }
        });
    }

    public void show() {
        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        // fade in transition if in game
        if (inGame) {
            Gdx.input.setInputProcessor(stage);
            renderBatch = false;
            batchFade = true;

            stage.addAction(Actions.sequence(Actions.moveTo(0, 0), Actions.alpha(0),
                Actions.run(new Runnable() {
                @Override
                public void run() {
                    renderBatch = true;
                }
            }), Actions.fadeIn(0.5f)));
        }
        // slide in transition if in menu
        else {
            super.showSlide(true);
            stage.addAction(Actions.alpha(1));
        }

        // set saved settings
        musicSlider.setValue(game.player.settings.musicVolume);
        sfxSlider.setValue(game.player.settings.sfxVolume);
        muteMusic.setChecked(game.player.settings.muteMusic);
        muteSfx.setChecked(game.player.settings.muteSfx);
        showEnemyLevels.setChecked(game.player.settings.showEnemyLevels);
        showWeatherAnims.setChecked(game.player.settings.showWeatherAnimations);
        showFps.setChecked(game.player.settings.showFps);
        language.setSelected(prefs.getString("lang", "ko").equals("ko") ? "KOREAN" : "ENGLISH");
    }

    @Override
    public void render(float dt) {
        update(dt);

        if (!inGame) {
            for (int i = 0; i < game.menuBackground.length; i++) {
                game.menuBackground[i].update(dt);
            }
        }

        // clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderBatch) {
            stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
            stage.getBatch().begin();
            // fix fading
            if (batchFade) stage.getBatch().setColor(Color.WHITE);

            // if in game the background is the background of the current world
            if (inGame) {
                stage.getBatch().draw(rm.worldSelectBackgrounds[worldIndex], 0, 0, Unlucky.V_WIDTH, Unlucky.V_HEIGHT);
            }
            else {
                for (int i = 0; i < game.menuBackground.length; i++) {
                    game.menuBackground[i].render((SpriteBatch) stage.getBatch());
                }
            }
            stage.getBatch().end();
        }

        stage.act(dt);
        stage.draw();
    }

}
