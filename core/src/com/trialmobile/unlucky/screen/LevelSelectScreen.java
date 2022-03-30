package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.map.Level;
import com.trialmobile.unlucky.resource.ResourceManager;

/**
 * Allows the player to select a level from a world
 * Displays the levels using a scroll pane
 *
 * WorldSelectScreen will always come before this screen and pass world data
 *
 * @author Ming Li
 */
public class LevelSelectScreen extends SelectScreen {

    // the world these levels are in
    private int numLevels;

    // current level selection
    private int currentLevelIndex;
    private int numLevelsToShow;

    // player stats to be displayed
    private String playerStats;

    public LevelSelectScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);

        handleExitButton();
        handleEnterButton();
        createScrollPane();
    }

    @Override
    public void show() {
        super.show();
        game.player.inMap = false;

        bannerLabel.setText(rm.worlds.get(worldIndex).name);
        bannerLabel.setStyle(nameStyles[worldIndex]);

        playerStats = rm.bundle.format("PLAYER_STATS",
                game.player.getLevel(), game.player.getHp(), game.player.getMaxHp(),
                game.player.getMinDamage(), game.player.getMaxDamage(), game.player.smoveset.toString());

        // the level the player is currently on and not completed
        if (this.worldIndex == game.player.maxWorld) {
            this.currentLevelIndex = game.player.maxLevel;
            this.numLevelsToShow = game.player.maxLevel;
        }
        // levels the player have completed so show all the levels
        else if (this.worldIndex < game.player.maxWorld) {
            this.currentLevelIndex = 0;
            this.numLevelsToShow = rm.worlds.get(worldIndex).numLevels - 1;
        }
        // in a world the player has not gotten to yet
        else {
            this.currentLevelIndex = 0;
            this.numLevelsToShow = -1;
        }

        // the side description will show player stats and level name
        String levelName = rm.worlds.get(worldIndex).levels[currentLevelIndex].name;
        fullDescLabel.setText(levelName + "\n\n" + playerStats);

        if (this.worldIndex > game.player.maxWorld) fullDescLabel.setText("???????????????" + "\n\n" + playerStats);

        scrollTable.remove();
        createScrollPane();

        // automatically scroll to the position of the currently selected world button
        float r = (float) currentLevelIndex / (numLevels - 1);
        scrollPane.setScrollPercentY(r);
    }

    /**
     * To know know what world this screen is in
     *
     * @param worldIndex int
     */
    public void setWorld(int worldIndex) {
        this.worldIndex = worldIndex;
        this.numLevels = rm.worlds.get(worldIndex).numLevels;
    }

    protected void handleExitButton() {
        super.handleExitButton(game.worldSelectScreen);
    }

    protected void handleEnterButton() {
        enterButtonGroup.setPosition(228, 8);
        stage.addActor(enterButtonGroup);
        enterButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                if (worldIndex <= game.player.maxWorld) {
                    // if the player's inventory is full give a warning
                    if (game.player.inventory.isFull()) {
                        new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
                            {
                                Label l = new Label(rm.bundle.get("DIALOG_INVENTORY_IS_FULL"), rm.dialogSkin);
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
                                if (object.equals("yes")) enterGame();
                            }

                        }.show(stage).getTitleLabel().setAlignment(Align.center);
                    } else {
                        enterGame();
                    }
                }
            }
        });
    }

    /**
     * Enters the map with the corresponding world, level key
     */
    private void enterGame() {
        game.gameScreen.init(worldIndex, currentLevelIndex);
        game.gameScreen.resetGame = true;
        rm.menuTheme.pause();
        game.player.inMap = true;
        setFadeScreen(game.gameScreen);
    }

    protected void createScrollPane() {
        scrollButtons = new Array<>();

        nameStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        descStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        buttonSelected = new TextButton.TextButtonStyle();
        buttonSelected.up = new TextureRegionDrawable(rm.skin.getRegion("default-round-down"));

        scrollTable = new Table();
        scrollTable.setFillParent(true);
        stage.addActor(scrollTable);

        selectionContainer = new Table();
        for (int i = 0; i < numLevels; i++) {
            final int index = i;

            // button and label group
            Group g = new Group();
            g.setSize(180, 40);
            g.setTransform(false);

            Level l = rm.worlds.get(worldIndex).levels[index];

            Label name;
            // on last level (boss level) the name is red
            if (i == numLevels - 1)
                name = new Label(l.name, new Label.LabelStyle(rm.pixel10, new Color(225 / 255.f, 0, 0, 1)));
            else
                name = new Label(l.name, nameStyle);
            name.setPosition(10, 20);
            name.setFontScale(0.66f);
            name.setTouchable(Touchable.disabled);
            Label desc = new Label(rm.bundle.format("AVERAGE_LEVEL", l.avgLevel), descStyle);
            desc.setPosition(10, 8);
            desc.setFontScale(0.5f);
            desc.setTouchable(Touchable.disabled);

            final TextButton b = new TextButton("", rm.skin);
            b.getStyle().checked = b.getStyle().down;
            b.getStyle().over = null;
            if (i == currentLevelIndex) b.setChecked(true);

            // only enable the levels the player has defeated
            if (index > numLevelsToShow) {
                b.setTouchable(Touchable.disabled);
                name.setText("???????????????");
                desc.setText(rm.bundle.get("AVERAGE_LEVEL_?"));
            }
            else {
                b.setTouchable(Touchable.enabled);
                scrollButtons.add(b);
                name.setText(l.name);
                desc.setText(rm.bundle.format("AVERAGE_LEVEL", l.avgLevel));
            }

            // select level
            b.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!game.player.settings.muteSfx) rm.invselectclick.play(game.player.settings.sfxVolume);
                    currentLevelIndex = index;
                    selectAt(currentLevelIndex);
                    String levelName = rm.worlds.get(worldIndex).levels[currentLevelIndex].name;
                    fullDescLabel.setText(levelName + "\n\n" + playerStats);
                }
            });
            b.setFillParent(true);

            g.addActor(b);
            g.addActor(name);
            g.addActor(desc);

            selectionContainer.add(g).padBottom(4).size(180, 40).row();
        }
        selectionContainer.pack();
        selectionContainer.setTransform(false);
        selectionContainer.setOrigin(selectionContainer.getWidth() / 2,
            selectionContainer.getHeight() / 2);

        scrollPane = new ScrollPane(selectionContainer, rm.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.layout();
        scrollTable.add(scrollPane).size(214, 186).fill();
        scrollTable.setPosition(-76, -20);
    }

    public void render(float dt) {
        super.render(dt, worldIndex);
    }

}
