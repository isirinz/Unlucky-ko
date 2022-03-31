package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.trialmobile.unlucky.VideoCallback;
import com.trialmobile.unlucky.effects.Moving;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.resource.ResourceManager;

import java.util.UUID;

/**
 * The main menu screen of the game that holds all access points for playing,
 * managing the player's inventory, bank, shop, etc, and the settings
 *
 * @author Ming Li
 */
public class MenuScreen extends MenuExtensionScreen {

    // whether to fade or slide in after a transition from another screen
    // 0 - fade in
    // 1 - slide in right
    // 2 - slide in left
    public int transitionIn = 0;

    // title animation (each letter moves down at descending speeds)
    private Moving[] titleMoves;
    private Image[] letters;

    // label style
    private Label.LabelStyle menuStyle;
    private Label battleLabel;

    // play button
    private ImageButton playButton;
    // other buttons
    private ImageButton[] optionButtons;
    private ImageButton offerWallButton;
    private ImageButton videoAdButton;

    private static final int NUM_BUTTONS = 6;

    // Credits Screen box
    private Image dark;
    private Group credits;
    private Image frame;
    private Label copyright;
    private Label github;
    private Label youtube;
    private Image[] creditsIcons;
    private ImageButton exitButton;

    private final Preferences prefs;

    public MenuScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);
        prefs = Gdx.app.getPreferences("Unlucky Preference");

        menuStyle = new Label.LabelStyle(rm.pixel10, new Color(79 / 255.f, 79 / 255.f, 117 / 255.f, 1));

        // one for each letter
        titleMoves = new Moving[7];
        letters = new Image[7];
        for (int i = 0; i < 7; i++) {
            titleMoves[i] = new Moving(new Vector2(), new Vector2(), 0);
            letters[i] = new Image(rm.title[i]);
            letters[i].setScale(2);
            stage.addActor(letters[i]);
        }

        handlePlayButton();
        handleOptionButtons();

        battleLabel = new Label(rm.bundle.get("BATTLE"), menuStyle);
        battleLabel.setSize(160, 80);
        battleLabel.setFontScale(1.5f);
        battleLabel.setTouchable(Touchable.disabled);
        battleLabel.setAlignment(Align.center);
        battleLabel.setPosition(140, 70);

        stage.addActor(battleLabel);

        createCreditsScreen();

        // menu music
        rm.menuTheme.setLooping(true);
        rm.menuTheme.play();
    }

    @Override
    public void show() {
        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        if (!rm.menuTheme.isPlaying()) rm.menuTheme.play();

        Gdx.input.setInputProcessor(stage);
        renderBatch = false;
        batchFade = true;
        resetTitleAnimation();

        if (transitionIn == 0) {
            // fade in animation
            stage.addAction(Actions.sequence(Actions.alpha(0), Actions.run(new Runnable() {
                @Override
                public void run() {
                    renderBatch = true;
                }
            }), Actions.fadeIn(0.5f)));
        } else {
            renderBatch = true;
            // slide in animation
            stage.addAction(Actions.sequence(Actions.moveTo(
                transitionIn == 1 ? Unlucky.V_WIDTH : -Unlucky.V_WIDTH, 0), Actions.moveTo(0, 0, 0.3f)));
        }

//        showAds();
    }

    @Override
    public void hide() {
        hideAds();

        super.hide();
    }

    private void handlePlayButton() {
        ImageButton.ImageButtonStyle s = new ImageButton.ImageButtonStyle();
        s.imageUp = new TextureRegionDrawable(rm.playButton[0][0]);
        s.imageDown = new TextureRegionDrawable(rm.playButton[1][0]);
        playButton = new ImageButton(s);
        playButton.setPosition(140, 70);
        playButton.getImage().setScale(2);
        stage.addActor(playButton);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                setFadeScreen(game.worldSelectScreen);
            }
        });
    }

    private void showAds() {
        game.appInterface.showBanner(true);
    }

    private void hideAds() {
        game.appInterface.showBanner(false);
    }

    private void showVideo() {
        game.appInterface.showVideo(new VideoCallback() {
            @Override
            public void success(String type, int amount) {
                final int gold = amount;
                game.player.addGold(amount);
                game.save.save();

                new Dialog(rm.bundle.get("DIALOG_REWARD"), rm.dialogSkin) {
                    {
                        getTitleLabel().setFontScale(0.5f);
                        Label l = new Label(rm.bundle.format("You_obtained_gold", gold), rm.dialogSkin);
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
        });
    }

    private void openOfferwall() {
        String userId = prefs.getString("USER_ID");
        if (userId == null || "".equals(userId.trim())) {
            userId = UUID.randomUUID().toString();
            prefs.putString("USER_ID", userId);
            prefs.flush();
        }
        game.appInterface.openOfferwall(userId);
    }

    private void handleOptionButtons() {
        ImageButton.ImageButtonStyle[] styles = rm.loadImageButtonStyles(NUM_BUTTONS, rm.menuButtons);
        optionButtons = new ImageButton[NUM_BUTTONS];
        for (int i = 0; i < NUM_BUTTONS; i++) {
            optionButtons[i] = new ImageButton(styles[i]);
            optionButtons[i].setSize(40, 40);
            optionButtons[i].getImage().setFillParent(true);
            stage.addActor(optionButtons[i]);
        }
        // inventory button
        optionButtons[0].setPosition(12, 170);
        // settings button
        optionButtons[1].setPosition(340, 170);
        // shop button
        optionButtons[2].setPosition(12, 100);
        // smove button
        optionButtons[3].setPosition(12, 30);
        // statistics button
        optionButtons[4].setPosition(340, 100);
        // credits button
        optionButtons[5].setPosition(340, 30);

        // inventory screen
        optionButtons[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                setSlideScreen(game.inventoryScreen, false);
            }
        });
        // settings screen
        optionButtons[1].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                game.settingsScreen.inGame = false;
                setSlideScreen(game.settingsScreen, true);
            }
        });
        // shop screen
        optionButtons[2].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                setSlideScreen(game.shopScreen, false);
            }
        });
        // smove screen
        optionButtons[3].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                setSlideScreen(game.smoveScreen, false);
            }
        });
        // statistics screen
        optionButtons[4].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                setSlideScreen(game.statisticsScreen, true);
            }
        });
        // credits screen
        optionButtons[5].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                credits.setVisible(true);
            }
        });

        ImageButton.ImageButtonStyle videoAdStyle = new ImageButton.ImageButtonStyle();
        videoAdStyle.imageUp = new TextureRegionDrawable(rm.videoAdIcon);
        videoAdButton = new ImageButton(videoAdStyle);
//        stage.addActor(videoAdButton);
        videoAdButton.setPosition(80, 115);
        videoAdButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showVideo();
            }
        });

        if (Gdx.app.getType().equals(Application.ApplicationType.Android)) {
            ImageButton.ImageButtonStyle offerWallStyle = new ImageButton.ImageButtonStyle();
            offerWallStyle.imageUp = new TextureRegionDrawable(rm.offerWallIcon);
            offerWallButton = new ImageButton(offerWallStyle);
//            stage.addActor(offerWallButton);
            offerWallButton.setPosition(80, 45);
            offerWallButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    openOfferwall();
                }
            });
        }
    }

    private void createCreditsScreen() {
        credits = new Group();
        credits.setTransform(false);
        credits.setVisible(false);
        credits.setSize(Unlucky.V_WIDTH, Unlucky.V_HEIGHT);

        // darken the menu screen to focus on the credits
        dark = new Image(rm.shade);
        dark.setScale(2);
        credits.addActor(dark);

        frame = new Image(rm.skin, "textfield");
        frame.setScale(2);
        frame.setSize(120, 60);
        frame.setPosition(Unlucky.V_WIDTH / 2 - 120, Unlucky.V_HEIGHT / 2 - 60);
        credits.addActor(frame);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.imageUp = new TextureRegionDrawable(rm.exitbutton18x18[0][0]);
        exitStyle.imageDown = new TextureRegionDrawable(rm.exitbutton18x18[1][0]);
        exitButton = new ImageButton(exitStyle);
        exitButton.getImage().setScale(2);
        exitButton.setSize(28, 28);
        exitButton.setPosition(Unlucky.V_WIDTH / 2 + 106, Unlucky.V_HEIGHT / 2 + 44);
        credits.addActor(exitButton);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                credits.setVisible(false);
            }
        });

        copyright = new Label(rm.bundle.format("COPYRIGHT", Unlucky.VERSION),
            new Label.LabelStyle(rm.pixel10, Color.WHITE));
        copyright.setFontScale(0.75f);
        copyright.setPosition(106, 140);
        copyright.setTouchable(Touchable.disabled);
        credits.addActor(copyright);

        github = new Label("GITHUB", new Label.LabelStyle(rm.pixel10, new Color(140 / 255.f, 60 / 255.f, 1, 1)));
        github.setPosition(160, 112);
        credits.addActor(github);
        github.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Unlucky.GITHUB);
            }
        });

        youtube = new Label("YOUTUBE", new Label.LabelStyle(rm.pixel10, Color.RED));
        youtube.setPosition(160, 76);
        credits.addActor(youtube);
        youtube.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI(Unlucky.YOUTUBE);
            }
        });

        creditsIcons = new Image[2];
        for (int i = 0; i < 2; i++) {
            final int index = i;
            creditsIcons[i] = new Image(rm.creditsicons[i]);
            creditsIcons[i].setScale(2);
            creditsIcons[i].setPosition(112, 68 + i * 36);
            creditsIcons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (index == 1) Gdx.net.openURI(Unlucky.GITHUB);
                    else Gdx.net.openURI(Unlucky.YOUTUBE);
                }
            });
            credits.addActor(creditsIcons[i]);
        }

        stage.addActor(credits);
    }

    public void update(float dt) {
        for (int i = 0; i < 7; i++) {
            titleMoves[i].update(dt);
            letters[i].setPosition(titleMoves[i].position.x, titleMoves[i].position.y);
        }
    }
    /**
     * Resets and starts the title animation on every transition to this screen
     */
    private void resetTitleAnimation() {
        // entire title text starts at x = 74
        for (int i = 0; i < titleMoves.length; i++) {
            titleMoves[i].origin.set(new Vector2(74 + i * 36, 240 + 48));
            titleMoves[i].target.set(new Vector2(74 + i * 36, 240 - 70));
            titleMoves[i].speed = (275 - i * 24) / 2;
            titleMoves[i].horizontal = false;
            titleMoves[i].start();
        }
    }

}
