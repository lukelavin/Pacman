package com.gamedesign.pacman.control.ai;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.Config;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.SpawnPointComponent;
import com.gamedesign.pacman.control.MoveDirection;
import com.gamedesign.pacman.control.MoveMode;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.type.EntityType;
import com.gamedesign.pacman.type.GhostType;
import com.gamedesign.pacman.type.GhostTypeComponent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.gamedesign.pacman.Config.*;

/**
 * Created by lukel on 1/29/2017.
 */
public abstract class GhostControl extends AbstractControl
{
    GameEntity ghost;
    MoveDirection moveDirection;
    int framesSinceDirectionChange;

    LocalTimer modeTimer;
    //final MoveMode[] mode = {MoveMode.UNRELEASED, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKFOREVER};
    MoveMode[] modes;
    int i;

    String[] textures;
    LocalTimer textureTimer;
    int texturei;
    private GhostType ghostType(){
        return ghost.getComponentUnsafe(GhostTypeComponent.class).getValue();
    }

    int[][] homeGrid;
    int[][] playerGrid;
    int[][] blockGrid;
    boolean gridsInitialized;

    double v;

    GameEntity player(){
        return (GameEntity) FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0);
    }

    PlayerControl playerControl(){
        return player().getControlUnsafe(PlayerControl.class);
    }

    SpawnPointComponent spawnPointComponent(){
        return ghost.getComponentUnsafe(SpawnPointComponent.class);
    }

    @Override
    public void onAdded(Entity entity)
    {
        ghost = (GameEntity) entity;
        homeGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        gridsInitialized = false;
        textureTimer = FXGL.newLocalTimer();
        texturei = 0;
        framesSinceDirectionChange = 5;

        spawnPointComponent().setSpawn(ghost.getPosition());
        System.out.println(spawnPointComponent().getValue().getX() / 40 + " | " + spawnPointComponent().getValue().getY() / 40);
        moveDirection = MoveDirection.RIGHT;

        modeTimer = FXGL.newLocalTimer();
        modeTimer.capture();
        i = 0;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        if(((PacmanApp) FXGL.getApp()).getGameState() == GameState.ACTIVE && gridsInitialized)
        {
            if (playerControl().getState() == "Energized")
            {
                ghost.getMainViewComponent().setView(new ImageView(new Image("assets/textures/" + "scaredghost00.png")));
                scatter();
            }
            else
            {
                this.v = v;
                if (textureTimer.elapsed(Duration.millis(75)) && moveDirection != null)
                {
                    updateTexture();
                    textureTimer.capture();
                    texturei = (texturei + 1) % textures.length;
                }

                if (!getSide().isEmpty())
                    ghost.setPosition(getPortal(getSide()).getPosition().add(PACMAN_OFFSET));

        /*
        Ghosts in Pacman do not, in fact, chase Pacman the entire time. Ghosts alternate between
        periods of attacking Pacman and periods of "scattering" back to their home corners. This
        block of code uses the sequence defined in mode[] to determine the right mode for the ghosts.
         */
                System.out.println(i);
                System.out.println(modes[i]);
                switch (modes[i])
                {
                    case UNRELEASED:
                        System.out.println("idle");
                        idle();
                        break;

                    case SCATTERLONG:
                        scatter();
                        System.out.println("scatter");
                        break;

                    case SCATTERSHORT:
                        System.out.println("scatter");
                        scatter();
                        break;

                    case ATTACKLONG:
                        System.out.println("attack");
                        attack();
                        break;

                    case ATTACKFOREVER:
                        System.out.println("attack");
                        attack();
                        break;
                }

                if (modeTimer.elapsed(Duration.millis(modes[i].getDuration())))
                {
                    if (i < modes.length - 1)
                    {
                        i++;
                        modeTimer.capture();
                    }
                }
            }
        }
    }

    private String getSide(){
        if(ghost.getX() <= -ghost.getWidth())
            return "Left";
        else if(ghost.getX() >= MAP_SIZE_X * BLOCK_SIZE)
            return "Right";
        else if(ghost.getY() <= -ghost.getHeight())
            return "Up";
        else if(ghost.getY() >= MAP_SIZE_Y * BLOCK_SIZE)
            return "Down";
        return "";
    }

    private List<GameEntity> portals(){
        return FXGL.getApp()
                .getGameWorld()
                .getEntitiesByType(EntityType.TELEPORTER)
                .stream()
                .map(e -> (GameEntity) e)
                .collect(Collectors.toList());
    }

    private GameEntity getPortal(String side){
        HashMap<String, GameEntity> portalMap = new HashMap();

        for(GameEntity e : portals())
        {
            if(e.getPositionComponent().getX() == 0)
                portalMap.put("Left", e);
            else if(e.getPositionComponent().getX() == (MAP_SIZE_X - 1) * BLOCK_SIZE)
                portalMap.put("Right", e);
            else if(e.getPositionComponent().getY() == 0)
                portalMap.put("Up", e);
            else if(e.getPositionComponent().getY() == (MAP_SIZE_Y - 1) * BLOCK_SIZE)
                portalMap.put("Down", e);
            else{System.out.println(e.getX() + "   " + e.getY());}
        }

        switch (side)
        {
            case "Left" : return portalMap.get("Right");
            case "Right" : return portalMap.get("Left");
            case "Up" : return portalMap.get("Down");
            case "Down" : return portalMap.get("Up");
        }

        return null;
    }

    void updateTexture()
    {
        String str = ghostType() + "_" + moveDirection + "_TEXTURES";
        try {
            Field field = Config.class.getField(str);
            textures = (String[]) field.get(new Config());
        } catch (Exception e) {}

        ghost.getMainViewComponent().setView(new ImageView(new Image("assets/textures/" + textures[texturei])));
    }

    public abstract void pushGridUpdate();

    void idle()
    {
        if(moveDirection == null)
            moveDirection = MoveDirection.RIGHT;

        int[] rightCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDY()))),
                (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDX())))};
        int[] leftCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDY()))),
                (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDX())))};

        if(!onTile()){
            ghost.getPositionComponent().translate(moveDirection.getDX(), moveDirection.getDY());
        }
        else
        {
            switch (moveDirection)
            {
                case RIGHT:
                    if (blockGrid[rightCoordinates[0]][rightCoordinates[1]] == 0) {
                        ghost.getPositionComponent().translate(moveDirection.getDX(), moveDirection.getDY());
                    } else {
                        moveDirection = MoveDirection.LEFT;
                    }
                    break;

                case LEFT:
                    if (blockGrid[leftCoordinates[0]][leftCoordinates[1]] == 0) {
                        ghost.getPositionComponent().translate(moveDirection.getDX(), moveDirection.getDY());
                    } else {
                        moveDirection = MoveDirection.RIGHT;
                    }
                    break;
            }
        }
    }

    public abstract void attack();

    public void scatter(){
        int min = Integer.MAX_VALUE; // initialize min as a value that won't interfere with real results
        MoveDirection minDirection = null; // null is fine for minDirection because it will be checked later regardless

        /*
        A ghost can not make 180 degree turns, so checking for changes in moveDirection is only
        necessary when onTile. If not onTile, keep moving in the current direction.
         */
        if (!onTile())
        {
            ghost.getPositionComponent().translate(moveDirection.getDX(), moveDirection.getDY());
        }
        else
        {
            /*
            Check if the block above the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in homeGrid). Make sure not to allow a direction change if previously moving down (no 180
            degree turns).
             */
            int[] upCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

            // avoid indexOutOfBounds
            if (upCoordinates[0] > 0 && moveDirection != MoveDirection.DOWN && blockGrid[upCoordinates[0]][upCoordinates[1]] == 0)
            {
                int upDistance = homeGrid[upCoordinates[0]][upCoordinates[1]];
                if (upDistance < min)
                {
                    min = upDistance;
                    minDirection = MoveDirection.UP;
                }
            }

            /*
            Check if the block to the right of the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in homeGrid). Make sure not to allow a direction change if previously moving left (no 180
            degree turns).
             */
            int[] rightCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDX())))};

            // avoid indexOutOfBounds
            if (rightCoordinates[1] < homeGrid[0].length - 1 && moveDirection != MoveDirection.LEFT && blockGrid[rightCoordinates[0]][rightCoordinates[1]] == 0)
            {
                int rightDistance = homeGrid[rightCoordinates[0]][rightCoordinates[1]];
                if (rightDistance < min)
                {
                    min = rightDistance;
                    minDirection = MoveDirection.RIGHT;
                }
            }

            /*
            Check if the block to the left of the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in homeGrid). Make sure not to allow a direction change if previously moving right (no 180
            degree turns).
             */
            int[] leftCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDX())))};

            // avoid indexOutOfBounds
            if (leftCoordinates[1] > 0 && moveDirection != MoveDirection.RIGHT &&
                    blockGrid[leftCoordinates[0]][leftCoordinates[1]] == 0)
            {
                int leftDistance = homeGrid[leftCoordinates[0]][leftCoordinates[1]];
                if (leftDistance < min)
                {
                    min = leftDistance;
                    minDirection = MoveDirection.LEFT;
                }
            }

            /*
            Check if the block below the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in homeGrid). Make sure not to allow a direction change if previously moving up (no 180
            degree turns).
             */
            int[] downCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDX())))};

            // avoid indexOutOfBounds
            if (downCoordinates[0] < homeGrid.length - 1 && moveDirection != MoveDirection.UP &&
                    blockGrid[downCoordinates[0]][downCoordinates[1]] == 0)
            {
                int downDistance = homeGrid[downCoordinates[0]][downCoordinates[1]];
                if (downDistance < min)
                {
                    min = downDistance;
                    minDirection = MoveDirection.DOWN;
                }
            }

            /*
            It's possible that a move while on a tile will still leave you onTile(). Because of this,
            the ghosts were able to do 360 turns within a single pathway. The easiest way to stop this
            was just to make sure the ghost can't immediately change directions.
             */
            if(framesSinceDirectionChange > 5)
            {
                if(minDirection != moveDirection)
                    framesSinceDirectionChange = 0;
                moveDirection = minDirection;
            }

            // move in the newly assigned direction
            if(moveDirection != null)
                ghost.getPositionComponent().translate(moveDirection.getDX(),moveDirection.getDY());
        }
    }

    boolean onTile()
    {
        double x = ghost.getPosition().getX();
        double y = ghost.getPosition().getY();

        /*
        Sometimes, entities will travel past the onTile range within the span of one frame,
        going across a whole tile without every returning true to an onTile test.

        Because of this, You need to check a wider range depending on how fast
        entities travel. The current code is functional only if an entity's speed is
        less than or equal to 3 * 60. A faster moving entity seems excessive anyways
        if not for just a gimmicky "Speed Pacman" game.
        */

        //check if Pacman is on a tile (with a margin of error of 3 pixels)
        boolean xOnTile = (((int) x - 5) % BLOCK_SIZE == 39) ||
                (((int) x - 5) % BLOCK_SIZE == 0) ||
                (((int) x - 5) % BLOCK_SIZE == 1);
        boolean yOnTile = (((int) y - 5) % BLOCK_SIZE == 39) ||
                (((int) y - 5) % BLOCK_SIZE == 0) ||
                (((int) y - 5) % BLOCK_SIZE == 1);

        return xOnTile && yOnTile;
    }

    public abstract void respawn();
}
