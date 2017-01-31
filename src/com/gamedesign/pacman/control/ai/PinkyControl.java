package com.gamedesign.pacman.control.ai;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.EntityView;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.control.MoveDirection;
import com.gamedesign.pacman.control.MoveMode;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.type.EntityType;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.PacmanApp.blockGridInitialized;

/**
 * Created by lukel on 1/29/2017.
 */
public class PinkyControl extends GhostControl
{
    private final int[] homeCoordinates = {0, 0 + 3}; // Blinky's "home corner", an unreachable spot that he will try to move towards in scatter mode.
    private int[][] aheadGrid;
    private LocalTimer textureTimer;
    private int texturei;
    
    @Override
    public void onAdded(Entity entity)
    {
        homeGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        textureTimer = FXGL.newLocalTimer();
        texturei = 0;

        super.onAdded(entity);
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        /*
        The blocks aren't in the game world when onAdded is called, so the grids actually need to be initialized
        on the first frame.
         */
        if(blockGridInitialized && !gridsInitialized){
            blockGrid = PacmanApp.blockGrid;
            playerGrid = playerControl().playerGrid;
            aheadGrid = playerControl().aheadGrid;
            initHomeGrid();

            gridsInitialized = true;
        }

        if(textureTimer.elapsed(Duration.millis(75)))
        {
            texturei = (texturei + 1) % PINKY_TEXTURES.length;
            ghost.getMainViewComponent().setView(new ImageView(new Image("assets/textures/" + PINKY_TEXTURES[texturei])));
            textureTimer.capture();
        }

        framesSinceDirectionChange++;

        if(gridsInitialized)
            super.onUpdate(entity, v);
    }

    private void initHomeGrid()
    {
        // fill the array with a sentinel value
        for (int i = 0; i < homeGrid.length; i++)
            for (int j = 0; j < homeGrid[i].length; j++)
                homeGrid[i][j] = -1;

        int distance = 0;
        List<int[]> recentlyMarked = new ArrayList<int[]>();

        homeGrid[homeCoordinates[0]][homeCoordinates[1]] = distance;
        distance++;
        recentlyMarked.add(new int[] {homeCoordinates[0], homeCoordinates[1]});
        int numberOfSentinels = homeGrid.length * homeGrid[0].length;

        int numberOfBlocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK).size();

        while(numberOfSentinels > numberOfBlocks)
        {
            List<int[]> temp = new ArrayList<int[]>();

            for(int[] coordinates : recentlyMarked)
            {
                int r = coordinates[0];
                int c = coordinates[1];

                //check if the tile above exists and does not have a block
                //if so, then mark its distance from the goal
                if (r > 0)
                {
                    if (homeGrid[r - 1][c] == -1 && blockGrid[r - 1][c] == 0)
                    {
                        homeGrid[r - 1][c] = distance;
                        numberOfSentinels--;
                        temp.add(new int[] {r - 1, c});
                    }
                }

                //check if the tile to the right exists and does not have a block
                //if so, then its distance from the goal
                if (c < homeGrid[r].length - 1)
                {
                    if (homeGrid[r][c + 1] == -1 && blockGrid[r][c + 1] == 0)
                    {
                        homeGrid[r][c + 1] = distance;
                        numberOfSentinels--;
                        temp.add(new int[] {r, c + 1});
                    }
                }

                //check if the tile below exists and does not have a block
                //if so, mark its distance from the goal
                if (r < homeGrid.length - 1)
                {
                    if (homeGrid[r + 1][c] == -1 && blockGrid[r + 1][c] == 0)
                    {
                        homeGrid[r + 1][c] = distance;
                        numberOfSentinels--;
                        temp.add(new int[] {r + 1, c});
                    }
                }

                //check if the tile to the left exists and does not have a block
                //if so, then mark its distance from the goal
                if (c > 0)
                {
                    if (homeGrid[r][c - 1] == -1 && blockGrid[r][c - 1] == 0)
                    {
                        homeGrid[r][c - 1] = distance;
                        numberOfSentinels--;
                        temp.add(new int[] {r, c - 1});
                    }
                }
                //System.out.println(toString(homeGrid));
            }
            recentlyMarked = temp;
            distance++;
        }
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
            ghost.getPositionComponent().translate(v * moveDirection.getDX(), v * moveDirection.getDY());
        }
        else
        {
            /*
            Check if the block above the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in aheadGrid). Make sure not to allow a direction change if previously moving down (no 180
            degree turns).
             */
            int[] upCoordinates = {(int) ((ghost.getPosition().getY()  / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

            // avoid indexOutOfBounds
            if (upCoordinates[0] > 0 && moveDirection != MoveDirection.DOWN && blockGrid[upCoordinates[0]][upCoordinates[1]] == 0)
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
            if (rightCoordinates[1] < aheadGrid[0].length - 1 && moveDirection != MoveDirection.LEFT && blockGrid[rightCoordinates[0]][rightCoordinates[1]] == 0)
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
            if (leftCoordinates[1] > 0 && moveDirection != MoveDirection.RIGHT && blockGrid[leftCoordinates[0]][leftCoordinates[1]] == 0)
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
            if (downCoordinates[0] < aheadGrid.length - 1 && moveDirection != MoveDirection.UP && blockGrid[downCoordinates[0]][downCoordinates[1]] == 0)
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
            if(framesSinceDirectionChange > 3)
            {
                if(minDirection != moveDirection)
                    framesSinceDirectionChange = 0;
                moveDirection = minDirection;
            }

            // move in the newly assigned direction
            if(moveDirection != null)
                ghost.getPositionComponent().translate(v * moveDirection.getDX(),v * moveDirection.getDY());
        }
    }
//
//    public static String toString(int[][] arr)
//    {
//        String output = "";
//        for(int r = 0; r < arr.length; r++)
//        {
//            for (int c = 0; c < arr[r].length; c++)
//            {
//                if(arr[r][c] < 10)
//                    output += "0";
//                output += arr[r][c] + ", ";
//            }
//            output = output.substring(0, output.length() - 2) + "\n";
//        }
//
//        return output;
//    }
}
