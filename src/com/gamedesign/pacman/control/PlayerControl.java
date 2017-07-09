package com.gamedesign.pacman.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.EntityFactory;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.SpawnPointComponent;
import com.gamedesign.pacman.control.ai.BlinkyControl;
import com.gamedesign.pacman.control.ai.ClydeControl;
import com.gamedesign.pacman.control.ai.InkyControl;
import com.gamedesign.pacman.control.ai.PinkyControl;
import com.gamedesign.pacman.type.EntityType;
import com.gamedesign.pacman.type.GhostType;
import com.gamedesign.pacman.type.GhostTypeComponent;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.PacmanApp.blockGridInitialized;

public class PlayerControl extends AbstractControl
{
    private GameEntity gameEntity;
    private MoveDirection moveDirection, prevDirection;
    private LocalTimer textureTimer;
    private int i;
    private String state;
    private LocalTimer energizedTimer;

    private int[][] playerGrid, aheadGrid;

    SpawnPointComponent spawnPointComponent(){
        return gameEntity.getComponentUnsafe(SpawnPointComponent.class);
    }

    private double v;

    @Override
    public void onAdded(Entity entity)
    {
        gameEntity = (GameEntity) entity;
        textureTimer = FXGL.newLocalTimer();
        textureTimer.capture();

        moveDirection = MoveDirection.LEFT;

        playerGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        aheadGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];

        spawnPointComponent().setSpawn(gameEntity.getPosition());
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        if(((PacmanApp) FXGL.getApp()).getGameState() == GameState.ACTIVE)
        {
            if (moveDirection != null)
            {
                move();
            } else // if Pacman is stopped, just set the texture to the default and repeatedly capture to stop animating
            {
                i = 0;
                gameEntity.getMainViewComponent().setView(new ImageView("assets/textures/" + PACMAN_TEXTURES[i]));
                textureTimer.capture();
            }

            if (blockGridInitialized && onTile())
                updateGrids();

            // every 50 ms, if Pacman is moving, switch to the next texture
            // 50 is arbitrary, could and probably should be derived from speed in the future
            if (textureTimer.elapsed(Duration.millis(100)))
            {
                i = (i + 1) % PACMAN_TEXTURES.length;

                gameEntity.getMainViewComponent().setView(new ImageView("assets/textures/" + PACMAN_TEXTURES[i]));
                handleTexture(); // make sure to update the rotation with the new view
                textureTimer.capture();
            }

            if (!getSide().isEmpty())
                gameEntity.setPosition(getPortal(getSide()).getPosition().add(PACMAN_OFFSET));

            if(energizedTimer != null && energizedTimer.elapsed(Duration.seconds(6)))
            {
                state = null;
                energizedTimer = null;
            }
        }
    }

    private String getSide(){
        if(gameEntity.getX() <= -gameEntity.getWidth())
            return "Left";
        else if(gameEntity.getX() >= MAP_SIZE_X * BLOCK_SIZE)
            return "Right";
        else if(gameEntity.getY() <= -gameEntity.getHeight())
            return "Up";
        else if(gameEntity.getY() >= MAP_SIZE_Y * BLOCK_SIZE)
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

    private void updateGrids()
    {
        // don't update the grid when you go off screen for teleporting
        if((int) (nearestTile().getY() / BLOCK_SIZE) >= 0 && (int) (nearestTile().getY() / BLOCK_SIZE) < MAP_SIZE_Y  &&
                (int) (nearestTile().getX() / BLOCK_SIZE) >= 0 && (int) (nearestTile().getX() / BLOCK_SIZE) < MAP_SIZE_X)

        { // if you are in the map, get the pre-generated A* grid corresponding to the current location
            playerGrid = ((PacmanApp) FXGL.getApp()).getGridStorage()
                    .getGrid((int) (nearestTile().getY() / BLOCK_SIZE), (int) (nearestTile().getX() / BLOCK_SIZE));
        }

        if(prevDirection != null &&
                nearestTile().getY() / BLOCK_SIZE > 1 && nearestTile().getY() / BLOCK_SIZE < aheadGrid.length - 3 &&
                nearestTile().getX() / BLOCK_SIZE > 1 && nearestTile().getX() / BLOCK_SIZE < aheadGrid[0].length - 3)
        {
            // if you're moving, get the pre-generated A* grid corresponding to the position 2 blocks ahead of the current location
            aheadGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getGrid((int) (nearestTile().getY() + Math.signum(prevDirection.getDY()) * BLOCK_SIZE * 2) / BLOCK_SIZE,
                    (int) (nearestTile().getX() + Math.signum(prevDirection.getDX()) * BLOCK_SIZE * 2) / BLOCK_SIZE);
        }

        List<Entity> ghosts = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.ENEMY);

        for(Entity e : ghosts)
        {
            if(e.getComponentUnsafe(GhostTypeComponent.class).getValue() == GhostType.BLINKY)
                e.getControlUnsafe(BlinkyControl.class).pushGridUpdate();
            else if(e.getComponentUnsafe(GhostTypeComponent.class).getValue() == GhostType.PINKY)
                e.getControlUnsafe(PinkyControl.class).pushGridUpdate();
            else if(e.getComponentUnsafe(GhostTypeComponent.class).getValue() == GhostType.INKY)
                e.getControlUnsafe(InkyControl.class).pushGridUpdate();
            else if(e.getComponentUnsafe(GhostTypeComponent.class).getValue() == GhostType.CLYDE)
                e.getControlUnsafe(ClydeControl.class).pushGridUpdate();
        }
    }

    public void up()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.UP;
    }

    public void left()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.LEFT;
    }

    public void down()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.DOWN;
    }

    public void right()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.RIGHT;
    }

    /*
    Every onUpdate(), if the Pacman should be moving, move() will get called. The move() method
    handles all movement based on the direction fields, and takes no parameters. The move() method
    also currently handles teleports (which are hard-coded in). The move() method detects possible
    collisions and will wait to change direction until a space is free, or if no space becomes free,
    the move() method will stop movement and the method won't be called until another key is pressed.

    Note: Compensation for v almost surely doesn't work right now. I can't really slow my computer down
    artificially to test it out, but I'm pretty sure I can say that there's no way it works.
     */
    private void move()
    {
        // if this is the first time moving after being stopped, save moveDirection to prevDirection
        // also, make sure the texture is updated
        if(prevDirection == null)
        {
            prevDirection = moveDirection;
            handleTexture();
        }

        // store the dx and dy of moveDirection for convenience
        double dx = moveDirection.getDX();
        double dy = moveDirection.getDY();

        // store the dx and dy of prevDirection for convenience
        double prevdx = prevDirection.getDX();
        double prevdy = prevDirection.getDY();

        /*
        onTile() checks are only necessary if moveDirection is a new direction not yet
        moved in. If moveDirection = prevDirection, onTile() is only needed to check for dead
        ends.
         */
        if(prevDirection != moveDirection)
        {
            // only try and move in the new direction if you're on a tile
            if (onTile())
            {
                // if the space in the new direction is open, move and then set prevDirection to the new direction
                if (!hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(dx), BLOCK_SIZE * Math.signum(dy))))
                {
                    gameEntity.getPositionComponent().translate(dx, dy);
                    prevDirection = moveDirection;
                    handleTexture();
                }
                else // if the space in the new direction is blocked, try and move in the previous direction
                {
                    if(!hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(prevdx), BLOCK_SIZE * Math.signum(prevdy))))
                        gameEntity.getPositionComponent().translate(prevdx, prevdy);
                    else // if you can't move in the previous direction, stop movement
                        stopMovement();
                }
            }
            else // keep moving in the same direction until you reach a tile, so you can check the new direction again
            {
                gameEntity.getPositionComponent().translate(prevdx, prevdy);
            }
        }
        else // if moveDirection == prevDirection, you only need to check if blocks are in one direction
        {
            // if you've reached the end of a tile and there's a block in your way, stop all movement
            if(onTile() && hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(dx), BLOCK_SIZE * Math.signum(dy))))
                stopMovement();
            else // if you can move, keep moving
                gameEntity.getPositionComponent().translate(dx, dy);
        }
    }

    private boolean onTile(){
        double x = gameEntity.getPosition().getX();
        double y = gameEntity.getPosition().getY();

        /*
        Sometimes, Pacman will travel past the onTile range within the span of one frame,
        going across a whole tile without every returning true to an onTile test.

        Because of this, You need to check a wider range depending on how fast
        Pacman's travels. The current code is functional only if Pacman's speed is
        less than or equal to 3 * 60. A faster moving Pacman seems excessive anyways
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

    /*
    Not actually used right now, but might be used for ghost path finding, so I still
    have it here. Uses simple integer math to find the origin point for the closest tile
    to Pacman.
     */
    public Point2D nearestTile(){
        //return the pixel origin of the nearest tile to Pacman

        return new Point2D(((int) gameEntity.getPosition().getX() - 5 + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE,
                ((int) gameEntity.getPosition().getY() - 5 + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE);
    }

    /*
    Takes in a Point2D, finds the tile origin point for that Point2D, then checks
    if that point is the origin point for any blocks. Used in collision prevention.
     */
    private boolean hasBlock(Point2D tile)
    {
        GameApplication app = FXGL.getApp();

        List<Entity> blocks = app.getGameWorld().getEntitiesByType(EntityType.BLOCK);
        tile = new Point2D(((int) tile.getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE, ((int) tile.getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE);

        for(Entity block : blocks){
            Point2D blockPos = ((GameEntity) block).getPosition();
            if(blockPos.equals(tile))
                return true;
        }

        return false;
    }

    private void stopMovement()
    {
        prevDirection = null;
        moveDirection = null;
    }

    /*
    Change the direction of the texture based on the prevDirection to make sure
    the texture isn't rotated before Pacman is actually moving in a new direction.

    Only is called when necessary for efficiency.
    */
    private void handleTexture()
    {
        if (prevDirection == MoveDirection.UP)
            gameEntity.getRotationComponent().setValue(270);
        else if (prevDirection == MoveDirection.LEFT)
            gameEntity.getRotationComponent().setValue(180);
        else if (prevDirection == MoveDirection.DOWN)
            gameEntity.getRotationComponent().setValue(90);
        else if (prevDirection == MoveDirection.RIGHT)
            gameEntity.getRotationComponent().setValue(0);

        gameEntity.getView().setScaleX(1);
    }

    public int[][] getPlayerGrid(){ return playerGrid; }
    public int[][] getAheadGrid(){ return aheadGrid; }

    public String getState()
    {
        return state;
    }
    public void setState(String state)
    {
        this.state = state;
    }

    public void energize()
    {
        state = "Energized";
        energizedTimer = FXGL.newLocalTimer();
        energizedTimer.capture();
    }

    public void respawn()
    {
        PacmanApp app = (PacmanApp) FXGL.getApp();
        app.getGameWorld().addEntity(EntityFactory.newPlayer((int) spawnPointComponent().getValue().getX() / BLOCK_SIZE, (int) spawnPointComponent().getValue().getY() / BLOCK_SIZE));
        app.getGameWorld().removeEntity(gameEntity);
    }


    public static String toString(int[][] arr)
    {
        String output = "";
        for(int r = 0; r < arr.length; r++)
        {
            for (int c = 0; c < arr[r].length; c++)
            {
                if(arr[r][c] < 10 && arr[r][c] >= 0)
                    output += "0";
                output += arr[r][c] + ", ";
            }
            output = output.substring(0, output.length() - 2) + "\n";
        }

        return output;
    }
}