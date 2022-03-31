package com.trialmobile.unlucky.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.trialmobile.unlucky.AppInterface;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.parallax.Background;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.save.Save;
import com.trialmobile.unlucky.screen.GameScreen;
import com.trialmobile.unlucky.screen.InventoryScreen;
import com.trialmobile.unlucky.screen.LevelSelectScreen;
import com.trialmobile.unlucky.screen.MenuScreen;
import com.trialmobile.unlucky.screen.SettingsScreen;
import com.trialmobile.unlucky.screen.ShopScreen;
import com.trialmobile.unlucky.screen.SpecialMoveScreen;
import com.trialmobile.unlucky.screen.StatisticsScreen;
import com.trialmobile.unlucky.screen.WorldSelectScreen;
import com.trialmobile.unlucky.screen.game.VictoryScreen;
import com.trialmobile.unlucky.ui.inventory.InventoryUI;

/**
 * "Unlucky" is a RPG/Dungeon Crawler based on RNG
 * The player will go through various levels with numerous enemies
 * and attempt to complete each level by reaching the end tile.
 *
 * @author Ming Li
 */
public class Unlucky extends Game {

//    private GLProfiler profiler;

    public static final String VERSION = "1.0";
    public static final String TITLE = "Unlucky Version " + VERSION;
    public AppInterface appInterface;

    // Links
    public static final String GITHUB = "https://github.com/isirinz/unlucky-ko";
    public static final String YOUTUBE = "https://www.youtube.com/channel/UC-oA-vkeYrgEy23Sq2PLC8w/videos?shelf_id=0&sort=dd&view=0";

    // Desktop screen dimensions
    public static final int V_WIDTH = 400;
    public static final int V_HEIGHT = 240;
    public static final int V_SCALE = 6;

    // Rendering utilities
    public SpriteBatch batch;

    // Resources
    public ResourceManager rm;

    // Universal player
    public Player player;

    // Game save
    public Save save;

    // Screens
    public MenuScreen menuScreen;
    public GameScreen gameScreen;
    public WorldSelectScreen worldSelectScreen;
    public LevelSelectScreen levelSelectScreen;
    public InventoryScreen inventoryScreen;
    public ShopScreen shopScreen;
    public SpecialMoveScreen smoveScreen;
    public StatisticsScreen statisticsScreen;
    public InventoryUI inventoryUI;
    public VictoryScreen victoryScreen;
    public SettingsScreen settingsScreen;

    // main bg
    public Background[] menuBackground;

    // debugging
    public Label fps;

	public void create() {
//	    profiler = new GLProfiler(Gdx.graphics);

        batch = new SpriteBatch();
        rm = new ResourceManager();
        player = new Player("player", rm);

        save = new Save(player, "save.json");
        save.load(rm);

        // debugging
        fps = new Label("", new Label.LabelStyle(rm.pixel10, Color.RED));
        fps.setFontScale(0.5f);
        fps.setVisible(player.settings.showFps);

        inventoryUI = new InventoryUI(this, player, rm);
        menuScreen = new MenuScreen(this, rm);
        gameScreen = new GameScreen(this, rm);
        worldSelectScreen = new WorldSelectScreen(this, rm);
        levelSelectScreen = new LevelSelectScreen(this, rm);
        inventoryScreen = new InventoryScreen(this, rm);
        shopScreen = new ShopScreen(this, rm);
        smoveScreen = new SpecialMoveScreen(this, rm);
        statisticsScreen = new StatisticsScreen(this, rm);
        victoryScreen = new VictoryScreen(this, rm);
        settingsScreen = new SettingsScreen(this, rm);

        // create parallax background
        menuBackground = new Background[3];

        // ordered by depth
        // sky
        menuBackground[0] = new Background(rm.titleScreenBackground[0],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0, 0));
        menuBackground[0].setVector(0, 0);
        // back clouds
        menuBackground[1] = new Background(rm.titleScreenBackground[2],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0.3f, 0));
        menuBackground[1].setVector(20, 0);
        // front clouds
        menuBackground[2] = new Background(rm.titleScreenBackground[1],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0.3f, 0));
        menuBackground[2].setVector(60, 0);

        // profiler
//        profiler.enable();

        this.setScreen(menuScreen);
	}

	public void render() {
        fps.setText(Gdx.graphics.getFramesPerSecond() + " fps");
        super.render();
    }

	public void dispose() {
        batch.dispose();
        super.dispose();

        rm.dispose();
        menuScreen.dispose();
        gameScreen.dispose();
        worldSelectScreen.dispose();
        levelSelectScreen.dispose();
        inventoryScreen.dispose();
        shopScreen.dispose();
        statisticsScreen.dispose();
        inventoryUI.dispose();
        victoryScreen.dispose();
        settingsScreen.dispose();

//        profiler.disable();
	}

    /**
     * Logs profile for SpriteBatch calls
     */
	public void profile(String source) {
//        System.out.println("Profiling " + source + "..." + "\n" +
//            "  Drawcalls: " + profiler.getDrawCalls() +
//            ", Calls: " + profiler.getCalls() +
//            ", TextureBindings: " + profiler.getTextureBindings() +
//            ", ShaderSwitches:  " + profiler.getShaderSwitches() +
//            " vertexCount: " + profiler.getVertexCount().value);
//        profiler.reset();
    }

}
