package com.gamedesign.pacman.control.ai;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.gamedesign.pacman.AStarGridStorage;
import com.gamedesign.pacman.EntityFactory;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.control.MoveDirection;
import com.gamedesign.pacman.type.EntityType;
import com.gamedesign.pacman.type.GhostType;
import com.gamedesign.pacman.type.GhostTypeComponent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.Config.BLOCK_SIZE;
import static com.gamedesign.pacman.PacmanApp.blockGridInitialized;

/**
 * Created by lukel on 1/31/2017.
 */
public class InkyControl extends GhostControl
{
    private final int[] homeCoordinates = {MAP_SIZE_Y - 1, MAP_SIZE_X - 3};
    private int[][] aheadGrid;

    private GameEntity blinky(){
        List<Entity> ghosts = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.ENEMY);
        for(Entity e : ghosts){
            if(((GameEntity) e).getComponentUnsafe(GhostTypeComponent.class).getValue() == GhostType.BLINKY)
                return (GameEntity) e;
        }

        return null;
    }


    @Override
    public void onAdded(Entity entity) {
        System.out.println("inky");
        super.onAdded(entity);
        modes = INKY_MODES;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        if(((PacmanApp) FXGL.getApp()).getGameState() == GameState.ACTIVE && ghost != null)
        {
            if (blockGridInitialized && !gridsInitialized)
            {
                blockGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getBlockGrid();
                playerGrid = playerControl().getPlayerGrid();
                aheadGrid = playerControl().getAheadGrid();
                homeGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getGrid(homeCoordinates[0], homeCoordinates[1]);

                gridsInitialized = true;
            }


            framesSinceDirectionChange++;

            if (gridsInitialized)
                super.onUpdate(entity, v);
        }
    }

    @Override
    public void pushGridUpdate()
    {
        playerGrid = playerControl().getPlayerGrid();
        aheadGrid = playerControl().getAheadGrid();
    }

    @Override
    public void attack()
    {
        /*
        Clyde's movement differs from the other ghosts in that when attacking Pacman, he will
        immediately stop trying to attack Pacman and scatter if he ever gets too close. Note
        that Clyde still, however, will scatter when other ghosts scatter.
         */
        double dx = player().getPosition().getX() - blinky().getPosition().getX();
        double dy = player().getPosition().getY() - blinky().getPosition().getY();
        Point2D blinkyPacmanVector = new Point2D((dx + player().getX()) / BLOCK_SIZE, (dy + player().getY()) / BLOCK_SIZE);
//        if(blinky().getX() > player().getX())
//        {
//            dx = blinky().getPosition().getX() - player().getPosition().getX();
//        }
//        else
//        {
//            dx = player().getPosition().getX() - blinky().getPosition().getX();
//        }
//        if(blinky().getY() > player().getY())
//        {
//            dy = blinky().getPosition().getY() - player().getPosition().getY();
//        }
//        else
//        {
//            dy = player().getPosition().getY() - blinky().getPosition().getY();
//        }

//        blinkyPacmanVector = new Point2D((int) (dx * 2 / BLOCK_SIZE), (int) (dy * 2 / BLOCK_SIZE));

        double xDist = ghost.getX() - player().getX();
        double yDist = ghost.getY() - player().getY();
        double distance = Math.sqrt(xDist * xDist + yDist * yDist);

        boolean outOfBounds = blinkyPacmanVector.getX() < 0 ||
                              blinkyPacmanVector.getY() < 0 ||
                              blinkyPacmanVector.getX() > MAP_SIZE_X - 1 ||
                              blinkyPacmanVector.getY() > MAP_SIZE_Y - 1;
        System.out.println(blinkyPacmanVector + "   " + outOfBounds);

        if(distance < 8 || outOfBounds)
        {
            scatter();
        }
        else
        {
            aheadGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getGrid((int) blinkyPacmanVector.getY(), (int) blinkyPacmanVector.getX());

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
                stored in aheadGrid). Make sure not to allow a direction change if previously moving down (no 180
                degree turns).
                */
                int[] upCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                        (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

                // avoid indexOutOfBounds
                if (upCoordinates[0] >= 0 && moveDirection != MoveDirection.DOWN && blockGrid[upCoordinates[0]][upCoordinates[1]] == 0)
                {
                    int upDistance = aheadGrid[upCoordinates[0]][upCoordinates[1]];
                    if (upDistance < min)
                    {
                        min = upDistance;
                        minDirection = MoveDirection.UP;
                    }
                }

                /*
                Check if the block to the right of the ghost does not contain a block. Then, if it doesn't, check if
                it has the shortest distance to the target tile. (The distance to the target tile is the value
                stored in aheadGrid). Make sure not to allow a direction change if previously moving left (no 180
                degree turns).
                */
                int[] rightCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDY()))),
                        (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDX())))};

                // avoid indexOutOfBounds
                if (rightCoordinates[1] <= aheadGrid[0].length - 1 && moveDirection != MoveDirection.LEFT && blockGrid[rightCoordinates[0]][rightCoordinates[1]] == 0)
                {
                    int rightDistance = aheadGrid[rightCoordinates[0]][rightCoordinates[1]];
                    if (rightDistance < min)
                    {
                        min = rightDistance;
                        minDirection = MoveDirection.RIGHT;
                    }
                }

                /*
                Check if the block to the left of the ghost does not contain a block. Then, if it doesn't, check if
                it has the shortest distance to the target tile. (The distance to the target tile is the value
                stored in aheadGrid). Make sure not to allow a direction change if previously moving right (no 180
                degree turns).
                 */
                int[] leftCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDY()))),
                        (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDX())))};

                // avoid indexOutOfBounds
                if (leftCoordinates[1] >= 0 && moveDirection != MoveDirection.RIGHT && blockGrid[leftCoordinates[0]][leftCoordinates[1]] == 0)
                {
                    int leftDistance = aheadGrid[leftCoordinates[0]][leftCoordinates[1]];
                    if (leftDistance < min)
                    {
                        min = leftDistance;
                        minDirection = MoveDirection.LEFT;
                    }
                }

                /*
                Check if the block below the ghost does not contain a block. Then, if it doesn't, check if
                it has the shortest distance to the target tile. (The distance to the target tile is the value
                stored in aheadGrid). Make sure not to allow a direction change if previously moving up (no 180
                degree turns).
                */
                int[] downCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDY()))),
                        (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDX())))};

                // avoid indexOutOfBounds
                if (downCoordinates[0] <= aheadGrid.length - 1 && moveDirection != MoveDirection.UP && blockGrid[downCoordinates[0]][downCoordinates[1]] == 0)
                {
                    int downDistance = aheadGrid[downCoordinates[0]][downCoordinates[1]];
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
                if (framesSinceDirectionChange > 5)
                {
                    if(minDirection != null && minDirection != moveDirection) {
                        framesSinceDirectionChange = 0;
                        moveDirection = minDirection;
                    }
                }

                // move in the newly assigned direction
                if (moveDirection != null)
                    ghost.getPositionComponent().translate(moveDirection.getDX(), moveDirection.getDY());
            }
        }
    }

    @Override
    public void respawn()
    {
        PacmanApp app = (PacmanApp) FXGL.getApp();
        app.getGameWorld().addEntity(EntityFactory.newInky((int) spawnPointComponent().getValue().getX() / BLOCK_SIZE, (int) spawnPointComponent().getValue().getY() / BLOCK_SIZE));
        app.getGameWorld().removeEntity(ghost);
    }
}
