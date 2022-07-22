package com.trialmobile.unlucky.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.inventory.Inventory;
import com.trialmobile.unlucky.inventory.Item;
import com.trialmobile.unlucky.inventory.Shop;
import com.trialmobile.unlucky.inventory.ShopItem;
import com.trialmobile.unlucky.main.Unlucky;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.ui.inventory.ItemTooltip;

import java.util.UUID;

/**
 * The screen for the shop UI where the player can buy/sell items
 * The shop has unique items that can't be dropped by monsters and
 * every item is rare or higher
 *
 * @author Ming Li
 */
public class ShopScreen extends MenuExtensionScreen {

    private final Player player;

    private static final int NUM_COLS = 4;

    // ui
    private Image ui;
    private final Label.LabelStyle whiteStyle;
    private Label.LabelStyle goldStyle;
    private Label gold;

    // 0 - buy, 1 - sell
    private ImageButton[] invButtons;
    private ImageButton.ImageButtonStyle enabled;
    private ImageButton.ImageButtonStyle disabled;
    private Label[] invButtonLabels;

    // inventory ui
    private Image selectedSlot;
    private ItemTooltip tooltip;
    private Item currentItem = null;
    private boolean itemSelected = false;

    private Shop shop;
    private Table[] tabContents;
    private ShopItem currentShopItem = null;

    // dialogs
    private final Dialog warningFullDialog;

    private final Preferences prefs;

    public ShopScreen(final Unlucky game, final ResourceManager rm) {
        super(game, rm);
        prefs = Gdx.app.getPreferences("Unlucky Preference");

        this.player = game.player;

        whiteStyle = new Label.LabelStyle(rm.pixel10, Color.WHITE);

        createInventoryUI();
        createShopUI();

        handleStageEvents();
        handleInvButtonEvents();

        warningFullDialog = new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
            {
                Label l = new Label(rm.bundle.get("INVENTORY_IS_FULL"), rm.dialogSkin);
                l.setFontScale(0.5f);
                l.setAlignment(Align.center);
                text(l);
                getButtonTable().defaults().width(80);
                getButtonTable().defaults().height(30);
                button("OK", "ok");
            }

            @Override
            protected void result(Object object) {
                if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
            }
        };
        warningFullDialog.getTitleLabel().setAlignment(Align.center);
    }

    public void show() {
        checkMyGold();

        game.fps.setPosition(2, 2);
        stage.addActor(game.fps);

        super.showSlide(false);
        addInventoryActors();
        handleInventoryEvents();

        // update labels
        gold.setText(rm.bundle.format("GOLD", player.getGold()));
    }

    private void createInventoryUI() {
        ui = new Image(rm.shopui);
        ui.setScale(2);
        ui.setPosition(8, 8);
        ui.setTouchable(Touchable.enabled);
        stage.addActor(ui);

        // create exit button
        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.imageUp = new TextureRegionDrawable(rm.exitbutton18x18[0][0]);
        exitStyle.imageDown = new TextureRegionDrawable(rm.exitbutton18x18[1][0]);
        // exit button
        ImageButton exitButton = new ImageButton(exitStyle);
        exitButton.getImage().setScale(2);
        exitButton.setSize(36, 36);
        exitButton.setPosition(365, 207);
        exitButton.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick0.play(game.player.settings.sfxVolume);
                removeInventoryActors();
                unselectItem();
                game.menuScreen.transitionIn = 1;
                setSlideScreen(game.menuScreen, true);
            }
        });
        stage.addActor(exitButton);

        // headers
        Label[] headers = new Label[2];
        String[] headerStrs = new String[] { rm.bundle.get("SHOP"), rm.bundle.get("INVENTORY") };
        for (int i = 0; i < headers.length; i++) {
            headers[i] = new Label(headerStrs[i], whiteStyle);
            headers[i].setFontScale(0.6f);
            headers[i].setPosition(8 + 20 + (i * 218), 214);
            stage.addActor(headers[i]);
        }

        goldStyle = new Label.LabelStyle(rm.pixel10, new Color(1, 212 / 255.f, 0, 1));
        gold = new Label("", goldStyle);
        gold.setFontScale(0.6f);
        gold.setPosition(140, 219);
        stage.addActor(gold);

        // inventory ui
        selectedSlot = new Image(rm.selectedslot28x28);
        selectedSlot.setScale(2);
        selectedSlot.setVisible(false);
        stage.addActor(selectedSlot);
        tooltip = new ItemTooltip(rm.skin);
        tooltip.setScale(2);
        tooltip.hide();
        stage.addActor(tooltip);

        enabled = new ImageButton.ImageButtonStyle();
        enabled.imageUp = new TextureRegionDrawable(rm.invbuttons92x28[0][0]);
        enabled.imageDown = new TextureRegionDrawable(rm.invbuttons92x28[1][0]);
        disabled = new ImageButton.ImageButtonStyle();
        disabled.imageUp = new TextureRegionDrawable(rm.invbuttons92x28[2][0]);
        invButtons = new ImageButton[2];
        invButtonLabels = new Label[2];
        for (int i = 0; i < 2; i++) {
            invButtons[i] = new ImageButton(disabled);
            invButtons[i].getImage().setScale(2);
            invButtons[i].setTouchable(Touchable.disabled);
            invButtons[i].setPosition(26 + (i * 96), 20);
            invButtonLabels[i] = new Label("", whiteStyle);
            invButtonLabels[i].setFontScale(0.5f);
            invButtonLabels[i].setTouchable(Touchable.disabled);
            invButtonLabels[i].setSize(92, 28);
            invButtonLabels[i].setAlignment(Align.center);
            invButtonLabels[i].setPosition(26 + (i * 96), 20);
            stage.addActor(invButtons[i]);
            stage.addActor(invButtonLabels[i]);
        }
        invButtonLabels[0].setText(rm.bundle.get("BUY"));
        invButtonLabels[1].setText(rm.bundle.get("SELL"));
    }

    /**
     * Creates the shop UI with a tabbed interface and scroll panes
     */
    private void createShopUI() {
        shop = new Shop(rm);

        // shop ui
        // main table
        Table shopTable = new Table();
        shopTable.setSize(188, 160);
        shopTable.setPosition(34, 52);

        // create tabs
        HorizontalGroup tabGroup = new HorizontalGroup();
        tabGroup.setTransform(false);
        TextButton.TextButtonStyle tabStyle = new TextButton.TextButtonStyle();
        tabStyle.font = rm.pixel10;
        tabStyle.fontColor = Color.WHITE;
        tabStyle.up = new TextureRegionDrawable(rm.shoptab[1][0]);
        tabStyle.checked = new TextureRegionDrawable(rm.shoptab[0][0]);

        final TextButton[] tabButtons = new TextButton[3];
        String[] tabStrs = new String[] { rm.bundle.get("MISC"), rm.bundle.get("EQUIPS"), rm.bundle.get("ACCS") };
        ButtonGroup<TextButton> tabs = new ButtonGroup<>();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        for (int i = 0; i < 3; i++) {
            tabButtons[i] = new TextButton(tabStrs[i], tabStyle);
            tabButtons[i].getLabel().setFontScale(0.5f);
            tabButtons[i].padLeft(10f);
            tabButtons[i].padRight(11f);
            tabButtons[i].padTop(1f);
            tabButtons[i].padBottom(1f);
            tabs.add(tabButtons[i]);
            tabGroup.addActor(tabButtons[i]);
        }
        tabGroup.padTop(4).align(Align.left);
        shopTable.add(tabGroup);
        shopTable.row();

        // tab contents
        Stack content = new Stack();
        createTabContents();
        for (int i = 0; i < 3; i++) {
            content.addActor(tabContents[i]);
        }
        shopTable.add(content).expand().fill();
        tabContents[0].setVisible(true);
        tabContents[1].setVisible(false);
        tabContents[2].setVisible(false);

        // show the correct content for each tab
        for (int i = 0; i < 3; i++) {
            final int index = i;
            tabButtons[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (!game.player.settings.muteSfx) rm.buttonclick1.play(game.player.settings.sfxVolume);
                    tabContents[index].setVisible(tabButtons[index].isChecked());
                }
            });
        }

        stage.addActor(shopTable);
    }

    /**
     * Creates the content in the form of item catalogs for each of the tabs
     */
    private void createTabContents() {
        ButtonGroup<TextButton> itemButtonGroup = new ButtonGroup<>();
        itemButtonGroup.setMinCheckCount(0);
        itemButtonGroup.setMaxCheckCount(1);

        tabContents = new Table[3];
        for (int i = 0; i < 3; i++) {
            tabContents[i] = new Table();
            tabContents[i].setFillParent(true);
            Table selectionContainer = new Table();

            for (int j = 0; j < shop.items.get(i).size; j++) {
                Group itemGroup = new Group();
                itemGroup.setTransform(false);
                Table itemTable = new Table();
                itemTable.setFillParent(true);

                final TextButton b = new TextButton("", rm.skin);
                b.setFillParent(true);
                itemButtonGroup.add(b);

                final ShopItem item = shop.items.get(i).get(j);
                item.actor.setScale(2);
                item.actor.setPosition(20, 6);
                item.actor.setTouchable(Touchable.disabled);
                Label itemName = new Label(item.labelName, Util.getItemColor(item.rarity, rm));
                itemName.setFontScale(0.5f);
                itemName.setTouchable(Touchable.disabled);
                itemName.setAlignment(Align.left);
                Label itemDesc = new Label(item.getFullDesc(), whiteStyle);
                itemDesc.setWrap(true);
                itemDesc.setFontScale(0.5f);
                itemDesc.setTouchable(Touchable.disabled);
                itemDesc.setAlignment(Align.left);
                Label itemPrice = new Label(rm.bundle.format("PRICE", item.price), goldStyle);
                itemPrice.setFontScale(0.5f);
                itemPrice.setTouchable(Touchable.disabled);
                itemPrice.setAlignment(Align.left);

                itemTable.add(itemName).size(120, 16).padBottom(8).padTop(-4).padLeft(24).row();
                itemTable.add(itemDesc).size(120, itemDesc.getPrefHeight()).padLeft(24).row();
                itemTable.add(itemPrice).padTop(8).padBottom(-8).padLeft(24).size(120, 16);

                itemGroup.addActor(b);
                itemGroup.addActor(item.actor);
                itemGroup.addActor(itemTable);

                // handle item selection
                b.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (b.isChecked()) {
                            if (!game.player.settings.muteSfx) rm.invselectclick.play(game.player.settings.sfxVolume);
                            currentShopItem = item;
                            // enable buying
                            invButtons[0].setTouchable(Touchable.enabled);
                            invButtons[0].setStyle(enabled);
                            invButtonLabels[0].setText(rm.bundle.format("BUY_FOR", item.price));
                        }
                        else {
                            currentShopItem = null;
                            invButtons[0].setTouchable(Touchable.disabled);
                            invButtons[0].setStyle(disabled);
                            invButtonLabels[0].setText(rm.bundle.get("BUY"));
                        }
                    }
                });

                int height = (int) (itemTable.getPrefHeight() + itemTable.getPrefHeight() / 2);
                item.actor.setPosition(8, (float)height / 2 - 10);

                selectionContainer.add(itemGroup).padLeft(0).padBottom(4).size(162, height).row();
            }
            selectionContainer.pack();
            selectionContainer.setTransform(false);
            selectionContainer.setOrigin(selectionContainer.getWidth() / 2,
                selectionContainer.getHeight() / 2);

            ScrollPane scrollPane = new ScrollPane(selectionContainer, rm.skin);
            scrollPane.setScrollingDisabled(true, false);
            scrollPane.setFadeScrollBars(true);
            //scrollPane.setupFadeScrollBars(0, 0);
            scrollPane.layout();
            tabContents[i].add(scrollPane).size(184, 132).fill();
        }
    }

    private void handleInventoryEvents() {
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            final Item item = player.inventory.getItem(i);
            if (item != null) {
                addInventoryEvent(item);
            }
        }
    }

    private void checkMyGold() {
        String userId = prefs.getString("USER_ID");
        if (userId == null || "".equals(userId.trim())) {
            userId = UUID.randomUUID().toString();
            prefs.putString("USER_ID", userId);
            prefs.flush();
        }

        Net.HttpRequest httpRequest = new Net.HttpRequest();
        httpRequest.setMethod("GET");
        httpRequest.setUrl("https://photoquoteapp.gnqkd.com/postback/gold?user_id="+userId);
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String body = httpResponse.getResultAsString();
                if (body != null && !"".equals(body.trim())) {
                    try {
                        int gold = Integer.parseInt(body.trim());
                        player.addGold(gold);
                        game.save.save();
                    } catch (Exception ignore) {
                    }
                }
            }

            @Override
            public void failed(Throwable t) {
            }

            @Override
            public void cancelled() {
            }
        });
    }
    /**
     * Adds the necessary events to a given item
     * @param item Item
     */
    private void addInventoryEvent(final Item item) {
        item.actor.clearListeners();
        item.actor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // select item
                if (selectedSlot.isVisible()) {
                    unselectItem();
                }
                else {
                    if (!game.player.settings.muteSfx) rm.invselectclick.play(game.player.settings.sfxVolume);
                    itemSelected = true;
                    currentItem = item;
                    showSelectedSlot(item);
                    tooltip.toFront();
                    Vector2 tpos = getCoords(item);
                    // make sure items at the bottom don't get covered by the tooltip
                    if (tpos.y <= 42)
                        tooltip.show(item, tpos.x + 16, tpos.y + tooltip.getHeight() / 2 + 8);
                    else
                        tooltip.show(item, tpos.x + 16, tpos.y - tooltip.getHeight() * 2);
                    // enable selling
                    invButtons[1].setTouchable(Touchable.enabled);
                    invButtons[1].setStyle(enabled);
                    invButtonLabels[1].setText(rm.bundle.format("SELL_FOR", item.sell));
                }
            }
        });
    }

    private void handleStageEvents() {
        ui.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (itemSelected) {
                    unselectItem();
                }
                return true;
            }
        });
    }

    private void handleInvButtonEvents() {
        // buy
        invButtons[0].addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick1.play(game.player.settings.sfxVolume);
                unselectItem();
                buy();
            }
        });
        // sell
        invButtons[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.player.settings.muteSfx) rm.buttonclick1.play(game.player.settings.sfxVolume);
                sell();
            }
        });
    }

    private void buy() {
        if (player.inventory.isFull()) {
            warningFullDialog.show(stage);
            return;
        }
        if (currentShopItem != null) {
            // item is too expensive to buy
            if (player.getGold() < currentShopItem.price) {
                new Dialog(rm.bundle.get("WARNING"), rm.dialogSkin) {
                    {
                        getTitleLabel().setFontScale(0.5f);
                        Label l = new Label(rm.bundle.format("NOT_ENOUGH_GOLD_TO_BUY", currentShopItem.labelName), rm.dialogSkin);
                        l.setFontScale(0.5f);
                        l.setAlignment(Align.center);
                        text(l);
                        getButtonTable().defaults().width(100);
                        getButtonTable().defaults().height(30);
                        button("OK", "ok");
                    }

                    @Override
                    protected void result(Object object) {
                        if (!game.player.settings.muteSfx) rm.buttonclick2.play(game.player.settings.sfxVolume);
                    }
                }.show(stage).getTitleLabel().setAlignment(Align.center);
                return;
            }
            new Dialog(rm.bundle.get("BUY"), rm.dialogSkin) {
                {
                    getTitleLabel().setFontScale(0.5f);
                    Label l = new Label(rm.bundle.format("DIALOG_BUY", currentShopItem.labelName), rm.dialogSkin);
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
                        player.addGold(-currentShopItem.price);
                        // a copy of the shop item
                        ShopItem item;
                        if (currentShopItem.type == 0) {
                            item = new ShopItem(rm, currentShopItem.name, currentShopItem.desc, currentShopItem.rarity,
                                currentShopItem.imgIndex, currentShopItem.minLevel, currentShopItem.hp, currentShopItem.exp, currentShopItem.sell, currentShopItem.price);
                        }
                        else {
                            item = new ShopItem(rm, currentShopItem.name, currentShopItem.desc, currentShopItem.type, currentShopItem.rarity,
                                currentShopItem.imgIndex, currentShopItem.minLevel, currentShopItem.mhp, currentShopItem.dmg, currentShopItem.acc, currentShopItem.sell, currentShopItem.price);
                        }
                        player.inventory.addItem(item);
                        stage.addActor(item.actor);
                        item.actor.setZIndex(item.index + 1);
                        addInventoryEvent(item);
                        gold.setText(rm.bundle.format("GOLD", player.getGold()));
                        player.stats.numShopItemsBought++;
                        game.save.save();
                        new Dialog("Success", rm.dialogSkin) {
                            {
                                Label l = new Label(rm.bundle.format("DIALOG_BUY_SUCCESS", currentShopItem.labelName), rm.dialogSkin);
                                l.setFontScale(0.5f);
                                l.setAlignment(Align.center);
                                text(l);
                                getButtonTable().defaults().width(40);
                                getButtonTable().defaults().height(15);
                                button(rm.bundle.get("DIALOG_OK"), "ok");
                            }

                            @Override
                            protected void result(Object object) {}
                        }.show(stage).getTitleLabel().setAlignment(Align.center);
                    }
                }

            }.show(stage).getTitleLabel().setAlignment(Align.center);
        }
    }

    private void sell() {
        if (currentItem != null) {
            new Dialog(rm.bundle.get("SELL"), rm.dialogSkin) {
                {
                    getTitleLabel().setFontScale(0.5f);
                    Label l = new Label(rm.bundle.format("DIALOG_SELL", currentItem.labelName), rm.dialogSkin);
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
                        player.addGold(currentItem.sell);
                        player.inventory.items[currentItem.index].actor.remove();
                        player.inventory.removeItem(currentItem.index);
                        unselectItem();
                        gold.setText(rm.bundle.format("GOLD", player.getGold()));
                        game.save.save();
                    }
                }

            }.show(stage).getTitleLabel().setAlignment(Align.center);
        }
    }

    private void addInventoryActors() {
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            Item item = player.inventory.getItem(i);
            if (item != null) {
                stage.addActor(item.actor);
            }
        }
    }

    private void removeInventoryActors() {
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            Item item = player.inventory.getItem(i);
            if (item != null) {
                item.actor.remove();
            }
        }
    }

    private void unselectItem() {
        itemSelected = false;
        currentItem = null;
        selectedSlot.setVisible(false);
        tooltip.hide();
        invButtons[1].setTouchable(Touchable.disabled);
        invButtons[1].setStyle(disabled);
        invButtonLabels[1].setText(rm.bundle.get("SELL"));
    }

    private void showSelectedSlot(Item item) {
        Vector2 pos = getCoords(item);
        selectedSlot.setPosition(pos.x, pos.y);
        selectedSlot.setVisible(true);
    }

    /**
     * Returns a Vector2 containing the x y coordinates of a slot in the inventory
     *
     * @param item Item
     * @return Vector2
     */
    private Vector2 getCoords(Item item) {
        Vector2 ret = new Vector2();

        int i = item.index;
        int x = i % NUM_COLS;
        int y = i / NUM_COLS;
        ret.set(246 + (x * 32), 180 - (y * 32));

        return ret;
    }

    public void update(float dt) {
        // update inventory positions
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            Item item = player.inventory.getItem(i);
            int x = i % NUM_COLS;
            int y = i / NUM_COLS;
            if (item != null) {
                item.actor.setPosition(250 + (x * 32), 184 - (y * 32));
            }
        }
    }

}
