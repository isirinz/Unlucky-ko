package com.trialmobile.unlucky.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.trialmobile.unlucky.animation.AnimationManager;
import com.trialmobile.unlucky.entity.Entity;
import com.trialmobile.unlucky.resource.ResourceManager;
import com.trialmobile.unlucky.resource.Util;

/**
 * Creates a tilemap from a text file.
 * A map file has the format:
 *
 * mapWidth
 * mapHeight
 * playerSpawn.x
 * playerSpawn.y
 * light (0 - daytime, 1 - nighttime)
 * weather (0 - none, 1 - rain, 2 - heavy rain, 3 - thunderstorm, 4 - snow, 5 - blizzard)
 * bottomLayer
 * s, s, s, s, ... -> mapWidth length
 * s, ...
 * s, ...
 * s, ...
 * mapHeight length
 * t, t, t, t, ... -> mapWidth length
 * t, ...
 * t, ...
 * t, ...
 * mapHeight length
 * topLayer (0 - no top layer, 1 - top layer)
 * (if top layer)
 * s, s, s, s, ... -> mapWidth length
 * s, ...
 * s, ...
 * s, ...
 * mapHeight length
 *
 * t can be one of the following:
 * - 0 for no tile
 * - tileID
 * - e[entityID] (means an Entity is placed on top of a tile)
 * - a[animIndex]|[numFrames]|[framesPerSecond] (an animated tile)
 *
 * s is the tileID of a NON-BLOCKED tile
 * if s is 0 then there is no tile
 *
 * A map will always have a bottom layer and tile map layer but does
 * not need to have a top layer
 *
 * @author Ming Li
 */
public class TileMap {

    // Tiles
    public int tileSize;

    // Map
    public String mapInfo;
    public String[] mapInfoLines;
    // array containing map information
    public Tile[] tileMap;
    public TextureRegion[] bottomLayer;
    public TextureRegion[] topLayer;
    public boolean hasTopLayer;
    public int mapWidth;
    public int mapHeight;
    public boolean[] collisionMap;

    public Vector2 origin;
    public Vector2 playerSpawn;

    public boolean dark;
    public int weather;

    // res
    private ResourceManager rm;

    public TileMap(int tileSize, String path, Vector2 origin, ResourceManager rm) {
        this.tileSize = tileSize;
        this.origin = origin;
        this.rm = rm;

        playerSpawn = new Vector2();

        // read file into a String
        FileHandle file = Gdx.files.internal(path);
        mapInfo = file.readString();
        // split string by newlines
        mapInfoLines = mapInfo.split("\\r?\\n");
        mapWidth = Integer.parseInt(mapInfoLines[0]);
        mapHeight = Integer.parseInt(mapInfoLines[1]);

        playerSpawn.set(Integer.parseInt(mapInfoLines[2]), Integer.parseInt(mapInfoLines[3]));

        dark = Integer.parseInt(mapInfoLines[4]) == 1;
        weather = Integer.parseInt(mapInfoLines[5]);

        bottomLayer = new TextureRegion[mapWidth * mapHeight];
        tileMap = new Tile[mapWidth * mapHeight];
        topLayer = new TextureRegion[mapWidth * mapHeight];

        createBottomLayer();
        createTileMap();
        createTopLayer();

        collisionMap = new boolean[mapWidth * mapHeight];
        for (int i = 0; i < collisionMap.length; i++) {
            collisionMap[i] = tileMap[i].isBlocked();
        }
    }

    /**
     * Converts lines 6->n of the mapInfo into a 1d array of TextureRegions
     * representing a non collidable bottom layer
     */
    private void createBottomLayer() {
        for (int i = 6; i < mapHeight + 6; i++) {
            String[] row = mapInfoLines[i].split(",");
            String[] trimmed = new String[row.length];
            for (int j = 0; j < row.length; j++) {
                trimmed[j] = row[j].replaceAll(" ", "");
            }
            for (int j = 0; j < mapWidth; j++) {
                int k = (mapWidth * mapHeight - 1) - ((i - 6) * mapWidth + j);
                int l = rm.tiles16x16[0].length;

                // index of -1 is null
                // bottom layer is null in cases where an upper tile will
                // completely replace the texture
                int index = Integer.parseInt(trimmed[row.length - 1 - j]) - 1;
                if (index == -1) bottomLayer[k] = null;
                else bottomLayer[k] = rm.tiles16x16[index / l][index % l];
            }
        }
    }

    /**
     * Converts lines mapHeight + 7->n of the mapInfo to a 1d array of Tiles
     * representing a tile for each element
     */
    private void createTileMap() {
        for (int i = mapHeight + 6; i < 2 * mapHeight + 6; i++) {
            String[] row = mapInfoLines[i].split(",");
            // remove all whitespace from text so the map files can be more readable with spaces
            String[] trimmed = new String[row.length];
            for (int j = 0; j < row.length; j++) {
                trimmed[j] = row[j].replaceAll(" ", "");
            }
            for (int j = 0; j < mapWidth; j++) {
                String temp = getAnimatedTileConversion(trimmed[row.length - 1 - j]);

                int k = (mapWidth * mapHeight - 1) - ((i - mapHeight - 6) * mapWidth + j);
                int l = rm.tiles16x16[0].length;

                int y = k / mapWidth;
                int x = k % mapWidth;

                Tile t;

                // check for Entity and tile format "e[Entity ID]" meaning
                // an Entity is placed on a certain tile
                if (temp.startsWith("e")) {
                    String removeSymbol = temp.substring(1, temp.length());
                    int entityID = Integer.parseInt(removeSymbol);

                    TextureRegion none = null;
                    // an entity is placed onto a tile with id -1 meaning empty tile with no texture
                    t = new Tile(-1, none, new Vector2(x, y));
                    t.addEntity(Util.getEntity(entityID, toMapCoords(x, y), this, rm));
                }
                // check for animated tile format
                else if (temp.startsWith("a")) {
                    String removeSymbol = temp.substring(1, temp.length());
                    String[] trivalue = removeSymbol.split("\\|");

                    int animIndex = Integer.parseInt(trivalue[0]);
                    int numFrames = Integer.parseInt(trivalue[1]);
                    int fps = Integer.parseInt(trivalue[2]);

                    AnimationManager anim = new AnimationManager(rm.atiles16x16, numFrames, animIndex, (float) 1 / fps);

                    t = new Tile(animIndex + 96, anim, new Vector2(x, y));
                }
                else {
                    int index = Integer.parseInt(trimmed[row.length - 1 - j]) - 1;
                    if (index == -1) {
                        TextureRegion none = null;
                        t = new Tile(-1, none, new Vector2(x, y));
                    }
                    else {
                        t = new Tile(index, rm.tiles16x16[index / l][index % l], new Vector2(x, y));
                    }
                }
                tileMap[k] = t;
            }
        }
    }

    /**
     * Creates the top layer of the map if there is one,
     * converting lines 2 * mapHeight + 7 -> n into a 1d array of TextureRegions
     */
    private void createTopLayer() {
        // get boolean hasTopLayer
        int h = Integer.parseInt(mapInfoLines[2 * mapHeight + 6]);
        hasTopLayer = h == 1 ? true : false;
        if (!hasTopLayer) return;

        for (int i = 2 * mapHeight + 7; i < 3 * mapHeight + 7; i++) {
            String[] row = mapInfoLines[i].split(",");
            String[] trimmed = new String[row.length];
            for (int j = 0; j < trimmed.length; j++) {
                trimmed[j] = row[j].replaceAll(" ", "");
            }
            for (int j = 0; j < mapWidth; j++) {
                int k = (mapWidth * mapHeight - 1) - ((i - (2 * mapHeight + 7)) * mapWidth + j);
                int l = rm.tiles16x16[0].length;

                // index of -1 is null
                int index = Integer.parseInt(trimmed[row.length - 1 - j]) - 1;

                if (index == -1) topLayer[k] = null;
                else topLayer[k] = rm.tiles16x16[index / l][index % l];
            }
        }
    }

    public void update(float dt) {
        for (int i = 0; i < tileMap.length; i++) {
            if (tileMap[i].containsEntity()) {
                tileMap[i].getEntity().update(dt);
            }
            if (tileMap[i].animated) {
                tileMap[i].anim.update(dt);
            }
        }
    }

    /**
     * Renders the bottom layer as the deepest part of the map
     *
     * @param batch SpriteBatch
     */
    public void renderBottomLayer(SpriteBatch batch, OrthographicCamera cam) {
        for (int i = 0; i < bottomLayer.length; i++) {
            if (tileInsideCamera(tileMap[i], cam)) {
                int r = i / mapWidth;
                int c = i % mapWidth;

                if (bottomLayer[i] != null)
                    batch.draw(bottomLayer[i], origin.x + c * tileSize, origin.y + r * tileSize, tileSize, tileSize);
                // render animated tiles below the player
                if (tileMap[i].animated) {
                    batch.draw(tileMap[i].anim.getKeyFrame(true), origin.x + c * tileSize, origin.y + r * tileSize, tileSize, tileSize);
                }
                // rendering non animated special tiles
                if (!tileMap[i].animated && tileMap[i].isSpecial() && tileMap[i].sprite != null) {
                    batch.draw(tileMap[i].sprite, origin.x + c * tileSize, origin.y + r * tileSize, tileSize, tileSize);
                }
                // drawing an entity on a Tile
                if (tileMap[i].containsEntity()) {
                    tileMap[i].getEntity().render(batch, true);
                }
            }
        }
    }

    /**
     * Renders the image representation of the map
     *
     * @param batch
     */
    public void render(SpriteBatch batch, OrthographicCamera cam) {
        for (int i = 0; i < tileMap.length; i++) {
            if (tileInsideCamera(tileMap[i], cam)) {
                int r = i / mapWidth;
                int c = i % mapWidth;

                if (!tileMap[i].animated && !tileMap[i].isSpecial() && tileMap[i].sprite != null) {
                    batch.draw(tileMap[i].sprite, origin.x + c * tileSize, origin.y + r * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    /**
     * Renders the top layer on top of the map
     *
     * @param batch
     */
    public void renderTopLayer(SpriteBatch batch, OrthographicCamera cam) {
        for (int i = 0; i < topLayer.length; i++) {
            if (tileInsideCamera(tileMap[i], cam)) {
                int r = i / mapWidth;
                int c = i % mapWidth;

                if (topLayer[i] != null)
                    batch.draw(topLayer[i], origin.x + c * tileSize, origin.y + r * tileSize, tileSize, tileSize);
            }
        }
    }

    /**
     * Converts a tile id corresponding to an animated tile to its animated tile format
     *
     * @param id
     * @return a String with the animated tile id in the correct format
     */
    private String getAnimatedTileConversion(String id) {
        if (id.startsWith("e")) return id;
        int ret = Integer.parseInt(id);
        if (ret == 64) return "a3|2|2";
        if (ret == 80) return "a4|4|3";
        if (ret == 96) return "a5|2|2";
        if (ret == 112) return "a6|3|3";
        if (ret == 128) return "a7|3|3";
        if (ret == 144) return "a8|3|3";
        if (ret == 160) return "a9|3|3";
        if (ret == 176) return "a10|2|2";
        if (ret == 192) return "a0|2|2";
        if (ret == 208) return "a1|2|2";
        if (ret == 224) return "a2|4|3";
        if (ret == 240) return "a11|3|3";
        if (ret == 63) return "a12|2|2";
        if (ret == 79) return "a13|6|4";
        if (ret == 94 || ret == 95) return "a14|2|2";
        return id;
    }

    /**
     * Determines if a certain tile is within the camera rendering distance.
     *
     * 6 tile from player x
     * 4 tile from player y
     *
     * @param t tile
     * @param cam map camera
     * @return visible
     */
    public boolean tileInsideCamera(Tile t, OrthographicCamera cam) {
        int x = (int) t.tilePosition.x * tileSize;
        int y = (int) t.tilePosition.y * tileSize;
        int xOffset = tileSize * 7;
        int yOffset = tileSize * 5;
        return x >= cam.position.x - xOffset - tileSize && x <= cam.position.x + xOffset &&
                y >= cam.position.y - yOffset && y <= cam.position.y + yOffset;
    }

    /**
     * Adds an Entity to a specific tile on the map
     *
     * @param entity
     * @param tileX
     * @param tileY
     */
    public void addEntity(Entity entity, int tileX, int tileY) {
        tileMap[tileY * mapWidth + tileX].addEntity(entity);
    }

    /**
     * Vector2 version
     *
     * @param entity
     * @param coords
     */
    public void addEntity(Entity entity, Vector2 coords) {
        tileMap[(int) (coords.y * mapWidth + coords.x)].addEntity(entity);
    }

    /**
     * Removes an Entity at a specific tile on the map
     *
     * @param tileX
     * @param tileY
     */
    public void removeEntity(int tileX, int tileY) {
        tileMap[tileY * mapWidth + tileX].removeEntity();
    }

    /**
     * Vector2 version
     *
     * @param coords
     */
    public void removeEntity(Vector2 coords) {
        tileMap[(int) (coords.y * mapWidth + coords.x)].removeEntity();
    }

    /**
     * Gets an Entity from a specific tile on the map
     *
     * @param tileX
     * @param tileY
     * @return
     */
    public Entity getEntity(int tileX, int tileY) {
        return tileMap[tileY * mapWidth + tileX].getEntity();
    }

    /**
     * Vector2 version
     *
     * @param coords
     * @return
     */
    public Entity getEntity(Vector2 coords) {
        return tileMap[(int) (coords.y * mapWidth + coords.x)].getEntity();
    }

    /**
     * Determines if there's an Entity on a specific tile on the map
     *
     * @param tileX
     * @param tileY
     * @return
     */
    public boolean containsEntity(int tileX, int tileY) {
        return tileMap[tileY * mapWidth + tileX].containsEntity();
    }

    /**
     * Vector2 version
     *
     * @param coords
     * @return
     */
    public boolean containsEntity(Vector2 coords) {
        return tileMap[(int) (coords.y * mapWidth + coords.x)].containsEntity();
    }

    /**
     * Replaces a Tile on a tile map
     *
     * @param tileX
     * @param tileY
     */
    public void setTile(int tileX, int tileY, Tile tile) {
        tileMap[tileY * mapWidth + tileX] = tile;
    }

    /**
     * Replaces a Tile on the map
     *
     * @param tilePosition
     * @param tile
     */
    public void setTile(Vector2 tilePosition, Tile tile) {
        tileMap[(int) (tilePosition.y * mapWidth + tilePosition.x)] = tile;
    }

    /**
     * Replaces a Tile by tile id
     *
     * @param tileX
     * @param tileY
     * @param id
     */
    public void setTile(int tileX, int tileY, int id) {
        int r = id / rm.tiles16x16[0].length;
        int c = id % rm.tiles16x16.length;
        tileMap[tileY * mapWidth + tileX] = new Tile(id, rm.tiles16x16[r][c], new Vector2(tileX, tileY));
    }

    /**
     * Converts tile coordinates to map coordinates
     *
     * @param tileX
     * @param tileY
     * @return
     */
    public Vector2 toMapCoords(int tileX, int tileY) {
        return new Vector2(tileX * tileSize, tileY * tileSize);
    }

    public Vector2 toMapCoords(Vector2 coords) {
        return new Vector2(coords.x * tileSize, coords.y * tileSize);
    }

    /**
     * Converts map coordinates to tile coordinates
     *
     * @param mapX
     * @param mapY
     * @return
     */
    public Vector2 toTileCoords(int mapX, int mapY) {
        return new Vector2(mapX / tileSize, mapY / tileSize);
    }

    public Vector2 toTileCoords(Vector2 coords) {
        return new Vector2(coords.x / tileSize, coords.y / tileSize);
    }

    /**
     * Returns a tile from the tile map at (x,y) tile position
     *
     * @return Tile
     */
    public Tile getTile(int tileX, int tileY) {
        return tileMap[tileY * mapWidth + tileX];
    }

    public Tile getTile(Vector2 coords) {
        return tileMap[(int) (coords.y * mapWidth + coords.x)];
    }

    /**
     * Does the tile map contain some Tile?
     *
     * @param tile
     * @return Boolean
     */
    public boolean mapContains(Tile tile) {
        for (int i = 0; i < tileMap.length; i++) {
            if (tileMap[i].equals(tile)) return true;
        }
        return false;
    }

    /**
     * Does the tile map contain a Tile that has a given id?
     *
     * @param id
     * @return Boolean
     */
    public boolean mapContains(int id) {
        for (int i = 0; i < tileMap.length; i++) {
            if (tileMap[i].id == id) return true;
        }
        return false;
    }

    /**
     * Returns a list of teleportation tiles on the map not including
     * the one the player is currently standing on
     *
     * @param currentTile
     * @return
     */
    public Array<Tile> getTeleportationTiles(Tile currentTile) {
        Array<Tile> ret = new Array<Tile>();
        for (int i = 0; i < tileMap.length; i++) {
            if (tileMap[i].isTeleport() && !tileMap[i].equals(currentTile)) {
                ret.add(tileMap[i]);
            }
        }
        return ret;
    }

}