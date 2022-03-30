package com.trialmobile.unlucky.ui.battleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.trialmobile.unlucky.animation.AnimationManager;
import com.trialmobile.unlucky.effects.Moving;
import com.trialmobile.unlucky.effects.Particle;
import com.trialmobile.unlucky.effects.ParticleFactory;
import com.trialmobile.unlucky.entity.Entity;
import com.trialmobile.unlucky.entity.Player;
import com.trialmobile.unlucky.event.Battle;
import com.trialmobile.unlucky.map.TileMap;
import com.trialmobile.unlucky.map.WeatherType;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;
import com.trialmobile.unlucky.screen.GameScreen;
import com.trialmobile.unlucky.ui.MovingImageUI;

/**
 * Displays health bars, battle animations, move animations, etc.
 *
 * @author Ming Li
 */
public class BattleScene extends BattleUI {

    private final Stage stage;

    // Health bars
    // player
    private final MovingImageUI playerHud;
    private final HealthBar playerHpBar;
    public Label playerHudLabel;
    // enemy
    private final MovingImageUI enemyHud;
    private final HealthBar enemyHpBar;
    private final Label enemyHudLabel;

    // Battle scene sprite positions
    private final Moving playerSprite;
    private final Moving enemySprite;
    private boolean renderPlayer = true;
    private boolean renderEnemy = true;

    // battle animations
    private final AnimationManager[] attackAnims;
    private final AnimationManager healAnim;

    // blinking hit animation
    private boolean showHitAnim = false;
    private float hitAnimDurationTimer = 0;
    private float hitAnimAlternateTimer = 0;
    private int lastHit = -1;

    // name colors based on enemy level
    /**
     * 3 or more levels lower than player = gray
     * 1 or 2 levels lower than player = green
     * same level as player = white
     * 1 or 2 levels higher than player = orange
     * 3 or more levels higher than player = red
     */
    private final Label.LabelStyle weakest;
    private final Label.LabelStyle weaker;
    private final Label.LabelStyle same;
    private final Label.LabelStyle stronger;
    private final Label.LabelStyle strongest;

    // weather conditions
    private final ParticleFactory factory;

    private boolean sfxPlaying = false;

    public BattleScene(GameScreen gameScreen, TileMap tileMap, Player player, Battle battle,
                       BattleUIHandler uiHandler, Stage stage, ResourceManager rm) {
        super(gameScreen, tileMap, player, battle, uiHandler, rm);

        this.stage = stage;

        BitmapFont font = rm.pixel10;
        Label.LabelStyle ls = new Label.LabelStyle(font, new Color(255, 255, 255, 255));

        weakest = new Label.LabelStyle(font, new Color(200 / 255.f, 200 / 255.f, 200 / 255.f, 1));
        weaker = new Label.LabelStyle(font, new Color(0, 225 / 255.f, 0, 1));
        same = new Label.LabelStyle(font, new Color(1, 1, 1, 1));
        stronger = new Label.LabelStyle(font, new Color(1, 175 / 255.f, 0, 1));
        strongest = new Label.LabelStyle(font, new Color(225 / 255.f, 0, 0, 1));

        // create player hud
        playerHud = new MovingImageUI(rm.playerhpbar145x40, new Vector2(-72, 200), new Vector2(0, 200), 100.f, 145, 40);
        playerHpBar = new HealthBar(player, stage, shapeRenderer, 96, 8, new Vector2(), new Color(0, 225 / 255.f, 0, 1));
        playerHudLabel = new Label("", ls);
        playerHudLabel.setFontScale(0.5f);
        playerHudLabel.setSize(98, 12);
        playerHudLabel.setTouchable(Touchable.disabled);

        // create enemy hud
        enemyHud = new MovingImageUI(rm.enemyhpbar145x40, new Vector2(400, 200), new Vector2(256, 200), 100.f, 145, 40);
        enemyHpBar = new HealthBar(null, stage, shapeRenderer, 96, 8, new Vector2(), new Color(225 / 255.f, 0, 0, 1));
        enemyHudLabel = new Label("", ls);
        enemyHudLabel.setFontScale(0.5f);
        enemyHudLabel.setSize(98, 12);
        enemyHudLabel.setTouchable(Touchable.disabled);

        // create player sprite
        Vector2 PLAYER_ORIGIN = new Vector2(-96, 100);
        playerSprite = new Moving(PLAYER_ORIGIN, new Vector2(70, 100), 75.f);
        // create enemy sprite
        Vector2 ENEMY_ORIGIN = new Vector2(400, 100);
        enemySprite = new Moving(ENEMY_ORIGIN, new Vector2(240, 100), 75.f);

        // create animations
        attackAnims = new AnimationManager[3];
        for (int i = 0; i < 3; i++) {
            attackAnims[i] = new AnimationManager(rm.battleAttacks64x64, 3, i, 1 / 6f);
            attackAnims[i].width = 64;
            attackAnims[i].height = 64;
        }
        healAnim = new AnimationManager(rm.battleHeal96x96, 3, 0, 1 / 5f);
        healAnim.width = 96;
        healAnim.height = 96;

        factory = new ParticleFactory((OrthographicCamera) stage.getCamera(), rm);

        stage.addActor(playerHud);
        stage.addActor(playerHudLabel);
        stage.addActor(enemyHud);
        stage.addActor(enemyHudLabel);
    }

    public void toggle(boolean toggle) {
        gameScreen.getGame().fps.setPosition(2, 2);
        stage.addActor(gameScreen.getGame().fps);

        playerHud.setVisible(toggle);
        playerHudLabel.setVisible(toggle);
        playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        playerHud.start();

        if (toggle) {
            enemyHpBar.setEntity(battle.opponent);

            if (battle.opponent.isBoss()) {
                // boss's name is always red
                enemyHudLabel.setStyle(strongest);
            }
            else {
                int diff = battle.opponent.getLevel() - player.getLevel();
                if (diff <= -3) enemyHudLabel.setStyle(weakest);
                else if (diff == -1 || diff == -2) enemyHudLabel.setStyle(weaker);
                else if (diff == 0) enemyHudLabel.setStyle(same);
                else if (diff == 1 || diff == 2) enemyHudLabel.setStyle(stronger);
                else enemyHudLabel.setStyle(strongest);
            }
            enemyHudLabel.setText(battle.opponent.getId());
        }

        enemyHud.setVisible(toggle);
        enemyHudLabel.setVisible(toggle);
        enemyHud.start();

        playerSprite.start();
        enemySprite.start();

        if (gameScreen.gameMap.weather == WeatherType.RAIN) {
            factory.set(Particle.STATIC_RAINDROP, 40, new Vector2(Util.RAINDROP_X, -200));
        } else if (gameScreen.gameMap.weather == WeatherType.HEAVY_RAIN ||
                gameScreen.gameMap.weather == WeatherType.THUNDERSTORM) {
            factory.set(Particle.STATIC_RAINDROP, 75, new Vector2(Util.RAINDROP_X, -240));
        } else if (gameScreen.gameMap.weather == WeatherType.SNOW) {
            factory.set(Particle.SNOWFLAKE, 100, new Vector2(Util.SNOWFLAKE_X, -120));
        } else if (gameScreen.gameMap.weather == WeatherType.BLIZZARD) {
            factory.set(Particle.SNOWFLAKE, 300, new Vector2(Util.SNOWFLAKE_X + 100, -160));
        }

    }

    /**
     * Resets all UI back to their starting point so the animations can begin
     * for a new battle
     */
    public void resetPositions() {
        playerHud.moving.origin.set(-144, 200);
        enemyHud.moving.origin.set(400, 200);

        playerSprite.origin.set(-96, 100);
        enemySprite.origin.set(400, 100);

        playerHud.setPosition(playerHud.moving.origin.x, playerHud.moving.origin.y);
        enemyHud.setPosition(enemyHud.moving.origin.x, enemyHud.moving.origin.y);
        playerSprite.position.set(playerSprite.origin);
        enemySprite.position.set(enemySprite.origin);
    }

    public void update(float dt) {
        playerHud.update(dt);
        enemyHud.update(dt);

        if (gameScreen.gameMap.weather != WeatherType.NORMAL) factory.update(dt);

        // entity sprite animations
        player.getBam().update(dt);
        if (battle.opponent.getBam() != null) battle.opponent.getBam().update(dt);

        playerSprite.update(dt);
        enemySprite.update(dt);

        // when enemy dies, its sprite falls off the screen
        if (player.isDead()) {
            playerSprite.position.y = playerSprite.position.y - 4;
            if (playerSprite.position.y < -96) playerSprite.position.y = -96;
        }
        if (battle.opponent.isDead()) {
            enemySprite.position.y = enemySprite.position.y - 4;
            if (enemySprite.position.y < -96) enemySprite.position.y = -96;
        }

        // render player and enemy sprites based on moving positions
        // hit animation
        if (showHitAnim) {
            hitAnimDurationTimer += dt;
            if (hitAnimDurationTimer < 0.7f) {
                hitAnimAlternateTimer += dt;
                if (hitAnimAlternateTimer > 0.1f) {
                    if (lastHit == 1) renderPlayer = !renderPlayer;
                    else renderEnemy = !renderEnemy;
                    hitAnimAlternateTimer = 0;
                }
            } else {
                hitAnimDurationTimer = 0;
                showHitAnim = false;
            }
        }
        else {
            renderPlayer = renderEnemy = true;
        }

        playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        if (player.settings.showEnemyLevels) {
            enemyHudLabel.setText("LV." + battle.opponent.getLevel() + " " + battle.opponent.getId());
        }
        else {
            enemyHudLabel.setText(battle.opponent.getId());
        }

        // show health bar animation after an entity uses its move
        playerHpBar.update(dt);
        enemyHpBar.update(dt);

        if (playerHud.getX() != playerHud.moving.target.x - 1 &&
            enemyHud.getX() != enemyHud.moving.target.x - 1) {
            // set positions relative to hud position
            playerHpBar.setPosition(playerHud.getX() + 40, playerHud.getY() + 8);
            playerHudLabel.setPosition(playerHud.getX() + 40, playerHud.getY() + 20);
            enemyHpBar.setPosition(enemyHud.getX() + 8, enemyHud.getY() + 8);
            enemyHudLabel.setPosition(enemyHud.getX() + 12, enemyHud.getY() + 20);
        }

        if (player.getMoveUsed() != -1) updateBattleAnimations(player, dt);
        if (battle.opponent.getMoveUsed() != -1) updateBattleAnimations(battle.opponent, dt);
    }

    /**
     * Update attack and heal animations after a move is used and its dialogue is finished
     *
     * @param entity either player or enemy
     * @param dt delta time
     */
    private void updateBattleAnimations(Entity entity, float dt) {
        // damaging moves
        if (entity.getMoveUsed() < 3 && entity.getMoveUsed() >= 0) {
            if (attackAnims[entity.getMoveUsed()].currentAnimation.isAnimationFinished()) {
                attackAnims[entity.getMoveUsed()].currentAnimation.stop();
                entity.setMoveUsed(-1);
                sfxPlaying = false;
                // start hit animation
                showHitAnim = true;
                if (!player.settings.muteSfx) rm.hit.play(player.settings.sfxVolume);
                if (entity == player) lastHit = 0;
                else lastHit = 1;
            } else {
                if (entity.getMoveUsed() == 0) {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.blueattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                else if (entity.getMoveUsed() == 1) {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.redattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                else {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.yellowattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                attackAnims[entity.getMoveUsed()].update(dt);
            }
        }
        // heal
        else if (entity.getMoveUsed() == 3) {
            if (healAnim.currentAnimation.isAnimationFinished()) {
                sfxPlaying = false;
                healAnim.currentAnimation.stop();
                entity.setMoveUsed(-1);
            } else {
                if (!player.settings.muteSfx && !sfxPlaying) {
                    rm.heal.play(player.settings.sfxVolume);
                    sfxPlaying = true;
                }
                healAnim.update(dt);
            }
        }
    }

    public void render(float dt) {
        gameScreen.getBatch().begin();
        if (renderPlayer) {
            gameScreen.getBatch().draw(player.getBam().getKeyFrame(true), playerSprite.position.x, playerSprite.position.y, player.getBam().width * 2, player.getBam().height * 2);
        }
        if (renderEnemy) {
            TextureRegion r = battle.opponent.getBam().getKeyFrame(true);
            if (battle.opponent.isBoss()) {
                gameScreen.getBatch().draw(r, enemySprite.position.x + (float)(48 - battle.opponent.battleSize) / 2, enemySprite.position.y,
                        battle.opponent.battleSize, battle.opponent.battleSize);
            }
            else {
                gameScreen.getBatch().draw(r, enemySprite.position.x, enemySprite.position.y, battle.opponent.battleSize, battle.opponent.battleSize);
            }
        }

        // render attack or heal animations
        // player side
        if (player.getMoveUsed() != -1) {
            if (player.getMoveUsed() < 3) {
                TextureRegion anim = attackAnims[player.getMoveUsed()].getKeyFrame(false);
                gameScreen.getBatch().draw(anim, 252, 114, anim.getRegionWidth() * 2, anim.getRegionHeight() * 2);
            } else if (player.getMoveUsed() == 3) {
                TextureRegion anim = healAnim.getKeyFrame(false);
                gameScreen.getBatch().draw(anim, 70, 100, anim.getRegionWidth() * 2, anim.getRegionHeight() * 2);
            }
        }
        // enemy side
        if (battle.opponent.getMoveUsed() != -1) {
            if (battle.opponent.getMoveUsed() < 3) {
                TextureRegion anim = attackAnims[battle.opponent.getMoveUsed()].getKeyFrame(false);
                gameScreen.getBatch().draw(anim, 84, 114, anim.getRegionWidth() * 2, anim.getRegionHeight() * 2);
            } else if (battle.opponent.getMoveUsed() == 3) {
                TextureRegion anim = healAnim.getKeyFrame(false);
                gameScreen.getBatch().draw(anim, 240, 100, anim.getRegionWidth() * 2, anim.getRegionHeight() * 2);
            }
        }

        // render weather and lighting conditions if any
        if (gameScreen.gameMap.weather != WeatherType.NORMAL) factory.render(gameScreen.getBatch());

        if (gameScreen.gameMap.isDark) {
            gameScreen.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA);
            gameScreen.getBatch().draw(rm.battledarkness, 0, 0, rm.battledarkness.getRegionWidth() * 2, rm.battledarkness.getRegionHeight() * 2);
            gameScreen.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        // render player and enemy status icons
        player.statusEffects.render(gameScreen.getBatch());
        battle.opponent.statusEffects.render(gameScreen.getBatch());
        gameScreen.getBatch().end();

        playerHpBar.render(dt);
        enemyHpBar.render(dt);
    }

}
