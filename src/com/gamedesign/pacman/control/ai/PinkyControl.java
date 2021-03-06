package com.gamedesign.pacman.control.ai;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.gamedesign.pacman.EntityFactory;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.control.MoveDirection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.PacmanApp.blockGridInitialized;

/**
 * Created by lukel on 1/29/2017.
 */
public class PinkyControl extends GhostControl
{
    private final int[] homeCoordinates = {0, 0 + 3}; // Blinky's "home corner", an unreachable spot that he will try to move towards in scatter mode.
    private int[][] aheadGrid;

    @Override
    public void onAdded(Entity entity) {
        super.onAdded(entity);
        modes = PINKY_MODES;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        if(((PacmanApp) FXGL.getApp()).getGameState() == GameState.ACTIVE && ghost != null){
        /*
        The blocks aren't in the game world when onAdded is called, so the grids actually need to be initialized
        on the first frame.
         */
            if(blockGridInitialized && !gridsInitialized){
                blockGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getBlockGrid();
                playerGrid = playerControl().getPlayerGrid();
                aheadGrid = playerControl().getAheadGrid();
                homeGrid = ((PacmanApp) FXGL.getApp()).getGridStorage().getGrid(homeCoordinates[0], homeCoordinates[1]);

                gridsInitialized = true;
            }


            framesSinceDirectionChange++;

            if(gridsInitialized)
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
            boolean outOfBounds = true;
            /*
            Check if the block above the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in aheadGrid). Make sure not to allow a direction change if previously moving down (no 180
            degree turns).
             */
            int[] upCoordinates = {(int) ((ghost.getPosition().getY()  / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

            // avoid indexOutOfBounds
            if (upCoordinates[0] >= 0 && moveDirection != MoveDirection.DOWN && blockGrid[upCoordinates[0]][upCoordinates[1]] == 0)
            {
                int upDistance = aheadGrid[upCoordinates[0]][upCoordinates[1]];
                outOfBounds = false;
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
                outOfBounds = false;
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
                outOfBounds = false;
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
                outOfBounds = false;
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
                if(minDirection != null && minDirection != moveDirection) {
                    framesSinceDirectionChange = 0;
                    moveDirection = minDirection;
                }
            }

            if(minDirection == null)
                moveDirection = moveDirection.inverse();

            // move in the newly assigned direction
            if(moveDirection != null)
                ghost.getPositionComponent().translate(moveDirection.getDX(),moveDirection.getDY());
        }
    }

    @Override
    public void respawn()
    {
        PacmanApp app = (PacmanApp) FXGL.getApp();
        app.getGameWorld().addEntity(EntityFactory.newPinky((int) spawnPointComponent().getValue().getX() / BLOCK_SIZE, (int) spawnPointComponent().getValue().getY() / BLOCK_SIZE));
        app.getGameWorld().removeEntity(ghost);
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
