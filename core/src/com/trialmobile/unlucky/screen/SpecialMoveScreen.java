package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.battle.SpecialMove;
import com.trialmobile.unlucky.battle.SpecialMoveset;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.ui.smove.SMoveTooltip;

/**
 * Screen for managing the player's special moveset
 * The player can drag and drop unlocked special moves onto their moveset
 *
 * @author Ming Li
 */
public class SpecialMoveScreen extends MenuExtensionScreen {

    private final Player player;

    // ui
    private final Label.LabelStyle white;
    private final Label.LabelStyle red;
    private final Label.LabelStyle green;
    private final Label.LabelStyle headerStyle;
    private final Label turnPrompt;
    private final Array<Image> garbage;
    private final SMoveTooltip tooltip;
    private final Image selectedSlot;
    // 0 - add button, 1 - remove button
    private ImageButton[] smoveButtons;
    private Label[] smoveButtonLabels;
    // 0 - enabled, 1 - disabled
    private ImageButton.ImageButtonStyle[] addButtonStyle;
    private ImageButton.ImageButtonStyle[] removeButtonStyle;
    private SpecialMove smoveToAdd = null;
    private int smoveToRemove = -1;

    // scroll pane
    private Table scrollTable;

    // dialogs
    private final Dialog warningFullDialog;

    public SpecialMoveScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);
        this.player = game.player;

        garbage = new Array<>();

        white = new Label.LabelStyle(rm.pixel10, Color.WHITE);
        red = new Label.LabelStyle(rm.pixel10, Color.RED);
        green = new Label.LabelStyle(rm.pixel10, new Color(0, 225 / 255.f, 0, 1));
        headerStyle = new Label.LabelStyle(rm.pixel10, new Color(1, 150 / 255.f, 66 / 255.f, 1));

        // create banner
        // screen banner
        Image banner = new Image(rm.skin, "default-slider");
        banner.setPosition(16, 204);
        banner.setSize(328, 24);
        stage.addActor(banner);

        Label bannerLabel = new Label(rm.bundle.get("MANAGE_SPECIAL_MOVES"), rm.skin);
        bannerLabel.setStyle(headerStyle);
        bannerLabel.setSize(100, 24);
        bannerLabel.setTouchable(Touchable.disabled);
        bannerLabel.setPosition(28, 204);
        bannerLabel.setAlignment(Align.left);
        stage.addActor(bannerLabel);

        stage.addActor(exitButton);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                unselectSlot();
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                smoveButtons[0].setStyle(addButtonStyle[1]);
                smoveButtons[0].setTouchable(Touchable.disabled);
                smoveButtons[1].setStyle(removeButtonStyle[1]);
                smoveButtons[1].setTouchable(Touchable.disabled);
                smoveToRemove = -1;
                smoveToAdd = null;
                game.menuScreen.transitionIn = 1;
                setSlideScreen(game.menuScreen, true);
            }
        });

        // create bg
        // bg layer
        Image bg = new Image(rm.skin, "default-slider");
        bg.setPosition(16, 12);
        bg.setSize(368, 184);
        bg.setTouchable(Touchable.enabled);
        stage.addActor(bg);

        turnPrompt = new Label("", white);
        turnPrompt.setFontScale(0.5f);
        turnPrompt.setPosition(24, 184);
        turnPrompt.setTouchable(Touchable.disabled);
        stage.addActor(turnPrompt);

        // create slots
        // the smove slots to be arranged in a pentagon
        Image[] slots = new Image[SpecialMoveset.MAX_MOVES];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Image(rm.smoveSlots[0]);
            Vector2 pos = getSlotPositions(i);
            slots[i].setScale(2);
            slots[i].setPosition(pos.x, pos.y);
            stage.addActor(slots[i]);
        }
        scrollTable = new Table();

        tooltip = new SMoveTooltip(rm.skin, headerStyle);
        tooltip.setScale(2);
        stage.addActor(tooltip);
        selectedSlot = new Image(rm.smoveSlots[1]);
        selectedSlot.setVisible(false);
        selectedSlot.setScale(2);
        stage.addActor(selectedSlot);

        bg.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                unselectSlot();
                smoveButtons[1].setStyle(removeButtonStyle[1]);
                smoveButtons[1].setTouchable(Touchable.disabled);
                smoveToRemove = -1;
            }
        });

        Label smoveset = new Label(rm.bundle.get("SPECIAL_MOVESET"), headerStyle);
        smoveset.setFontScale(0.5f);
        smoveset.setPosition(280, 84);
        smoveset.setAlignment(Align.center);
        smoveset.setTouchable(Touchable.disabled);
        stage.addActor(smoveset);

        createSmoveButtons();

        warningFullDialog = new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
            {
                Label l = new Label(rm.bundle.get("DIALOG_MOVE_IS_FULL"), rm.dialogSkin);
                l.setFontScale(0.5f);
                l.setAlignment(Align.center);
                text(l);
                getButtonTable().defaults().width(40);
                getButtonTable().defaults().height(15);
                button(rm.bundle.get("DIALOG_OK"), "ok");
            }

            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
            }
        };
        warningFullDialog.getTitleLabel().setAlignment(Align.center);
    }

    @Override
    public void show() {
        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        super.showSlide(false);
        scrollTable.remove();
        createScrollPane();
        smoveButtons[0].toFront();
        smoveButtons[1].toFront();
        smoveButtonLabels[0].toFront();
        smoveButtonLabels[1].toFront();
        addSmoveActors();
        // update turn cd
        turnPrompt.setText(rm.bundle.format("SPECIAL_MOVE_DESCRIPTION", player.smoveCd));
    }

    /**
     * Creates the two add and remove smove buttons
     */
    private void createSmoveButtons() {
        smoveButtons = new ImageButton[2];
        smoveButtonLabels = new Label[2];
        addButtonStyle = new ImageButton.ImageButtonStyle[2];
        removeButtonStyle = new ImageButton.ImageButtonStyle[2];
        String[] str = new String[] { rm.bundle.get("SPECIAL_MOVE_ADD"), rm.bundle.get("SPECIAL_MOVE_REMOVE") };

        for (int i = 0; i < 2; i++) {
            addButtonStyle[i] = new ImageButton.ImageButtonStyle();
            removeButtonStyle[i] = new ImageButton.ImageButtonStyle();
            smoveButtonLabels[i] = new Label(str[i], white);
            smoveButtonLabels[i].setFontScale(0.5f);
            smoveButtonLabels[i].setSize(76, 36);
            smoveButtonLabels[i].setAlignment(Align.center);
            smoveButtonLabels[i].setTouchable(Touchable.disabled);
        }
        // enabled add
        addButtonStyle[0].imageUp = new TextureRegionDrawable(rm.smoveButtons[0][0]);
        addButtonStyle[0].imageDown = new TextureRegionDrawable(rm.smoveButtons[1][0]);
        // disabled add
        addButtonStyle[1].imageUp = new TextureRegionDrawable(rm.smoveButtons[2][0]);
        // enabled remove
        removeButtonStyle[0].imageUp = new TextureRegionDrawable(rm.smoveButtons[0][1]);
        removeButtonStyle[0].imageDown = new TextureRegionDrawable(rm.smoveButtons[1][1]);
        // disabled remove
        removeButtonStyle[1].imageUp = new TextureRegionDrawable(rm.smoveButtons[2][1]);

        smoveButtons[0] = new ImageButton(addButtonStyle[1]);
        smoveButtons[1] = new ImageButton(removeButtonStyle[1]);
        smoveButtons[0].getImage().setScale(2);
        smoveButtons[1].getImage().setScale(2);

        smoveButtons[0].setPosition(192, 128);
        smoveButtons[1].setPosition(192, 28);
        smoveButtons[0].setTouchable(Touchable.disabled);
        smoveButtons[1].setTouchable(Touchable.disabled);
        smoveButtonLabels[0].setPosition(188, 128);
        smoveButtonLabels[1].setPosition(194, 28);
        stage.addActor(smoveButtons[0]);
        stage.addActor(smoveButtons[1]);
        stage.addActor(smoveButtonLabels[0]);
        stage.addActor(smoveButtonLabels[1]);

        // add button
        smoveButtons[0].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick1.play(game.player.settings.sfxVolume);
                unselectSlot();
                add();
            }
        });

        // remove button
        smoveButtons[1].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick1.play(game.player.settings.sfxVolume);
                if (smoveToRemove != -1) {
                    unselectSlot();
                    smoveButtons[1].setStyle(removeButtonStyle[1]);
                    smoveButtons[1].setTouchable(Touchable.disabled);
                    player.smoveset.remove(smoveToRemove);
                    addSmoveActors();
                    game.save.save();
                }
            }
        });
    }

    /**
     * Adds an smove from the selection to the player's smoveset
     */
    private void add() {
        if (player.smoveset.isFull()) {
            warningFullDialog.show(stage);
            return;
        }
        if (smoveToAdd != null) {
            // already two of a kind in the set
            if (!player.smoveset.canAdd(smoveToAdd.id)) {
                new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
                    {
                        Label l = new Label(rm.bundle.format("SPECIAL_MOVE_ALREADY", smoveToAdd.name), rm.dialogSkin);
                        l.setFontScale(0.5f);
                        l.setAlignment(Align.center);
                        text(l);
                        getButtonTable().defaults().width(40);
                        getButtonTable().defaults().height(15);
                        button(rm.bundle.get("DIALOG_OK"), "ok");
                    }

                    @Override
                    protected void result(Object object) {
                        if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                    }
                }.show(stage).getTitleLabel().setAlignment(Align.center);
                return;
            }
            // add smove
            player.smoveset.addSMove(smoveToAdd.id);
            addSmoveActors();
            game.save.save();
        }
    }

    /**
     * Adds the icons of the currently equipped smoves
     */
    private void addSmoveActors() {
        // clear garbage and remove from stage
        for (Image i : new Array.ArrayIterator<>(garbage)) i.remove();
        garbage.clear();
        Array<SpecialMove> set = player.smoveset.smoveset;
        for (int i = 0; i < set.size; i++) {
            Image icon = new Image(set.get(i).icon.getDrawable());
            icon.setScale(2);
            garbage.add(icon);
            Vector2 pos = getSlotPositions(i);
            icon.setPosition(pos.x + 2, pos.y + 2);
            addSmoveEvent(icon, i, set.get(i));
            stage.addActor(icon);
        }
    }

    /**
     * Returns the positions of the slot at a given smove index
     *
     * @param index index
     * @return position
     */
    private Vector2 getSlotPositions(int index) {
        // middle left
        if (index == 0) return new Vector2(232, 80);
        // top
        if (index == 1) return new Vector2(276, 138);
        // top right
        if (index == 2) return new Vector2(340, 112);
        // bottom right
        if (index == 3) return new Vector2(340, 48);
        // bottom left
        else return new Vector2(276, 20);
    }

    /**
     * Shows the smove tooltip and enables the remove button
     * @param icon image
     * @param index index
     */
    private void addSmoveEvent(final Image icon, final int index, final SpecialMove smove) {
        final Vector2 pos = getSlotPositions(index);
        icon.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // unselect slots
                if (selectedSlot.isVisible()) {
                    selectedSlot.setVisible(false);
                    tooltip.setVisible(false);
                    smoveButtons[1].setStyle(removeButtonStyle[1]);
                    smoveButtons[1].setTouchable(Touchable.disabled);
                    smoveToRemove = -1;
                }
                else {
                    if (!game.player.settings.muteSfx) rm.invselectclick.play(game.player.settings.sfxVolume);
                    // show selected slot
                    selectedSlot.setPosition(pos.x, pos.y);
                    selectedSlot.setVisible(true);
                    // show tooltip
                    Vector2 t = getTooltipCoords(pos, index);
                    tooltip.show(smove, t.x, t.y);
                    tooltip.toFront();

                    // enable remove button
                    smoveButtons[1].setStyle(removeButtonStyle[0]);
                    smoveButtons[1].setTouchable(Touchable.enabled);
                    smoveToRemove = index;
                }
            }
        });
    }

    private Vector2 getTooltipCoords(Vector2 pos, int index) {
//        Vector2 ret = new Vector2();
//        if (index == 0) ret.set(pos.x + 32, pos.y - tooltip.getHeight());
//        if (index == 1 || index == 2) ret.set(pos.x + 32, pos.y - tooltip.getHeight());
//        if (index == 3 || index == 4) ret.set(pos.x - 16, pos.y + tooltip.getHeight() + 16);
//        return ret;
        if (index == 0) return new Vector2(264, 80 - tooltip.getHeight());
        // top
        if (index == 1) return new Vector2(276, 106 - tooltip.getHeight());
        // top right
        if (index == 2) return new Vector2(308 - tooltip.getWidth(), 112 - tooltip.getHeight());
        // bottom right
        if (index == 3) return new Vector2(308 - tooltip.getWidth(), 48 - tooltip.getHeight());
            // bottom left
        else return new Vector2(276, 20 + tooltip.getHeight());
    }

    private void unselectSlot() {
        // unselect slots
        if (selectedSlot.isVisible()) {
            selectedSlot.setVisible(false);
            tooltip.setVisible(false);
        }
    }

    /**
     * Creates the scroll pane displaying all the special moves, icons and descs
     */
    private void createScrollPane() {
        scrollTable = new Table();
        scrollTable.setFillParent(true);
        stage.addActor(scrollTable);
        Table selectionContainer = new Table();

        ButtonGroup<TextButton> bg = new ButtonGroup<>();
        bg.setMaxCheckCount(1);
        bg.setMinCheckCount(0);

        for (int i = 0; i < Util.SMOVES_ORDER_BY_LVL.length; i++) {
            SpecialMove smove = Util.SMOVES_ORDER_BY_LVL[i];
            Group smoveGroup = new Group();
            smoveGroup.setSize(160, 60);
            smoveGroup.setTransform(false);

            Image frame = new Image(rm.smoveFrame);
            frame.setScale(2);
            frame.setPosition(6, 14);
            frame.setTouchable(Touchable.disabled);
            Image icon = new Image(smove.icon.getDrawable());
            icon.setScale(2);
            icon.setPosition(8, 16);
            icon.setTouchable(Touchable.disabled);
            Label name = new Label(smove.name, headerStyle);
            name.setFontScale(0.5f);
            name.setPosition(40, 42);
            name.setTouchable(Touchable.disabled);
            Label desc = new Label(smove.desc, white);
            desc.setFontScale(0.5f);
            desc.setPosition(40, 16);
            desc.setTouchable(Touchable.disabled);
            Label status;
            // green label if unlocked
            if (player.getLevel() >= smove.levelUnlocked) status = new Label(rm.bundle.get("UNLOCKED"), green);
            // red label with level to unlock
            else status = new Label(rm.bundle.format("UNLOCKED_AT_LV", smove.levelUnlocked), red);
            status.setFontScale(0.5f);
            status.setPosition(40, 6);
            status.setTouchable(Touchable.disabled);

            final TextButton button = new TextButton("", rm.skin);
            button.setFillParent(true);
            button.setTouchable(player.getLevel() >= smove.levelUnlocked ?
                Touchable.enabled : Touchable.disabled);
            bg.add(button);
            addScrollPaneEvents(button, smove);

            smoveGroup.addActor(button);
            smoveGroup.addActor(frame);
            smoveGroup.addActor(icon);
            smoveGroup.addActor(name);
            smoveGroup.addActor(desc);
            smoveGroup.addActor(status);

            selectionContainer.add(smoveGroup).padBottom(2).size(160, 60).row();
        }
        selectionContainer.pack();
        selectionContainer.setTransform(false);
        selectionContainer.setOrigin(selectionContainer.getWidth() / 2,
            selectionContainer.getHeight() / 2);

        ScrollPane scrollPane = new ScrollPane(selectionContainer, rm.skin);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(true);
        // remove scroll bar
        scrollPane.setupFadeScrollBars(0, 0);
        scrollPane.layout();
        scrollTable.add(scrollPane).size(240, 160).fill();
        scrollTable.setPosition(-88, -24);
    }

    /**
     * Handles button checked events for the smove scroll pane
     * @param button button
     * @param smove move
     */
    private void addScrollPaneEvents(final TextButton button, final SpecialMove smove) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                unselectSlot();
                smoveButtons[1].setStyle(removeButtonStyle[1]);
                smoveButtons[1].setTouchable(Touchable.disabled);
                if (button.isChecked()) {
                    if (!game.player.settings.muteSfx) rm.invselectclick.play(game.player.settings.sfxVolume);
                    smoveToAdd = smove;
                    smoveButtons[0].setStyle(addButtonStyle[0]);
                    smoveButtons[0].setTouchable(Touchable.enabled);
                }
                else {
                    smoveToAdd = null;
                    smoveButtons[0].setStyle(addButtonStyle[1]);
                    smoveButtons[0].setTouchable(Touchable.disabled);
                }
            }
        });
    }

}
