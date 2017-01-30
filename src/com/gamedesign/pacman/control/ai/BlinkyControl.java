package com.gamedesign.pacman.control.ai;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.GameEntity;
import com.gamedesign.pacman.control.MoveDirection;
import com.gamedesign.pacman.control.MoveMode;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.type.EntityType;
import javafx.geometry.Point2D;

import java.util.List;

import static com.gamedesign.pacman.Config.*;

/**
 * Created by lukel on 1/29/2017.
 */
public class BlinkyControl extends GhostControl
{
    private final int[] homeCoordinates = {0, MAP_SIZE_X - 3}; // Blinky's "home corner", an unreachable spot that he will try to move towards in scatter mode.
    private int[] playerCoordinates;
    private int[][] homeGrid;
    private int[][] playerGrid;
    GameEntity ghost;

    private int framesSinceDirectionChange;

    @Override
    public void onAdded(Entity entity)
    {
        ghost = (GameEntity) entity;
        homeGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        playerGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        playerCoordinates = new int[2];


        super.onAdded(entity);
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        /*
        Updating the A* grids take a lot of iteration (iterating through the whole game world until every tile is marked),
        and I wasn't really sure of how fast it would go, so I tried to update the grid only when absolutely necessary.
         */
        if (onTile())
        {
            updateGrids();
        }

        framesSinceDirectionChange++;

        playerCoordinates[0] = (int) (player().getControlUnsafe(PlayerControl.class).nearestTile().getY() / BLOCK_SIZE);
        playerCoordinates[1] = (int) (player().getControlUnsafe(PlayerControl.class).nearestTile().getX() / BLOCK_SIZE);

        super.onUpdate(entity, v);
    }

    private void updateGrids()
    {
        // The player grid only needs to be updated if you're moving towards Pacman
        if (mode[i] == MoveMode.ATTACKLONG || mode[i] == MoveMode.ATTACKFOREVER){
            updatePlayerGrid();
            //System.out.println(toString(playerGrid));
        }
        // likewise, the homeGrid only needs to be updated if you're scattering
        else{
            updateHomeGrid();
            //System.out.println(toString(homeGrid));
        }

    }

    private void updateHomeGrid()
    {
        // fill the array with a sentinel value
        for (int i = 0; i < homeGrid.length; i++)
            for (int j = 0; j < homeGrid[i].length; j++)
                homeGrid[i][j] = -1;

        int distance = 0;

        homeGrid[homeCoordinates[0]][homeCoordinates[1]] = 0;
        int numberOfSentinels = homeGrid.length * homeGrid[0].length - 1;

        int numberOfBlocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK).size();

        while (numberOfSentinels > numberOfBlocks)
        {
            for (int r = 0; r < homeGrid.length; r++)
            {
                for (int c = 0; c < homeGrid[r].length; c++)
                {
                    //check and mark the blocks around the most recently marked blocks
                    if (homeGrid[r][c] == distance)
                    {
                        //check if the tile above exists and does not have a block
                        //if so, then mark its distance from the goal
                        if (r > 0)
                        {
                            if (homeGrid[r - 1][c] == -1 && !hasBlock(new Point2D(c * BLOCK_SIZE,(r - 1) * BLOCK_SIZE)))
                            {
                                homeGrid[r - 1][c] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile to the right exists and does not have a block
                        //if so, then its distance from the goal
                        if (c < homeGrid[r].length - 1)
                        {
                            if (homeGrid[r][c + 1] == -1 && !hasBlock(new Point2D((c + 1) * BLOCK_SIZE,r * BLOCK_SIZE)))
                            {
                                homeGrid[r][c + 1] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile below exists and does not have a block
                        //if so, mark its distance from the goal
                        if (r < homeGrid.length - 1)
                        {
                            if (homeGrid[r + 1][c] == -1 && !hasBlock(new Point2D(c * BLOCK_SIZE, (r + 1) * BLOCK_SIZE)))
                            {
                                homeGrid[r + 1][c] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile to the left exists and does not have a block
                        //if so, then mark its distance from the goal
                        if (c > 0)
                        {
                            if (homeGrid[r][c - 1] == -1 && !hasBlock(new Point2D((c - 1) * BLOCK_SIZE, r * BLOCK_SIZE)))
                            {
                                homeGrid[r][c - 1] = distance + 1;
                                numberOfSentinels--;
                            }
                        }
                    }
                }
            }
            distance++;
        }
    }

    private void updatePlayerGrid()
    {
        // fill the array with a sentinel value
        for (int i = 0; i < playerGrid.length; i++)
            for (int j = 0; j < playerGrid[i].length; j++)
                playerGrid[i][j] = -1;

        int distance = 0;

        /*
        Mark the goal point as distance 0 to start the path from there.
        Also, decrement numberOfSentinels accordingly.
         */
        playerGrid[playerCoordinates[0]]
                [playerCoordinates[1]] = 0;
        int numberOfSentinels = playerGrid.length * playerGrid[0].length - 1;

        int numberOfBlocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK).size();

        while (numberOfSentinels > numberOfBlocks)
        {
            for (int r = 0; r < playerGrid.length; r++)
            {
                for (int c = 0; c < playerGrid[r].length; c++)
                {
                    //check and mark the blocks around the most recently marked blocks
                    if (playerGrid[r][c] == distance)
                    {
                        //check if the tile above exists and does not have a block
                        //if so, then mark its distance from the goal
                        if (r > 0)
                        {
                            if (playerGrid[r - 1][c] == -1 && !hasBlock(new Point2D(c * BLOCK_SIZE,(r - 1) * BLOCK_SIZE)))
                            {
                                playerGrid[r - 1][c] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile to the right exists and does not have a block
                        //if so, then its distance from the goal
                        if (c < playerGrid[r].length - 1)
                        {
                            if (playerGrid[r][c + 1] == -1 && !hasBlock(new Point2D((c + 1) * BLOCK_SIZE,r * BLOCK_SIZE)))
                            {
                                playerGrid[r][c + 1] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile below exists and does not have a block
                        //if so, mark its distance from the goal
                        if (r < playerGrid.length - 1)
                        {
                            if (playerGrid[r + 1][c] == -1 && !hasBlock(new Point2D(c * BLOCK_SIZE, (r + 1) * BLOCK_SIZE)))
                            {
                                playerGrid[r + 1][c] = distance + 1;
                                numberOfSentinels--;
                            }
                        }

                        //check if the tile to the left exists and does not have a block
                        //if so, then mark its distance from the goal
                        if (c > 0)
                        {
                            if (playerGrid[r][c - 1] == -1 && !hasBlock(new Point2D((c - 1) * BLOCK_SIZE, r * BLOCK_SIZE)))
                            {
                                playerGrid[r][c - 1] = distance + 1;
                                numberOfSentinels--;
                            }
                        }
                    }
                }
            }
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
            stored in playerGrid). Make sure not to allow a direction change if previously moving down (no 180
            degree turns).
             */
            int[] upCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

            // avoid indexOutOfBounds
            if (upCoordinates[0] > 0 && moveDirection != MoveDirection.DOWN && !hasBlock(new Point2D(upCoordinates[1] * BLOCK_SIZE, upCoordinates[0] * BLOCK_SIZE)))
            {
                int upDistance = playerGrid[upCoordinates[0]][upCoordinates[1]];
                if (upDistance < min)
                {
                    min = upDistance;
                    minDirection = MoveDirection.UP;
                }
            }

            /*
            Check if the block to the right of the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in playerGrid). Make sure not to allow a direction change if previously moving left (no 180
            degree turns).
             */
            int[] rightCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.RIGHT.getDX())))};

            // avoid indexOutOfBounds
            if (rightCoordinates[1] < playerGrid[0].length - 1 && moveDirection != MoveDirection.LEFT && !hasBlock(new Point2D(rightCoordinates[1] * BLOCK_SIZE, rightCoordinates[0] * BLOCK_SIZE)))
            {
                int rightDistance = playerGrid[rightCoordinates[0]][rightCoordinates[1]];
                if (rightDistance < min)
                {
                    min = rightDistance;
                    minDirection = MoveDirection.RIGHT;
                }
            }

            /*
            Check if the block to the left of the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in playerGrid). Make sure not to allow a direction change if previously moving right (no 180
            degree turns).
             */
            int[] leftCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.LEFT.getDX())))};

            // avoid indexOutOfBounds
            if (leftCoordinates[1] > 0 && moveDirection != MoveDirection.RIGHT && !hasBlock(new Point2D(leftCoordinates[1] * BLOCK_SIZE, leftCoordinates[0] * BLOCK_SIZE)))
            {
                int leftDistance = playerGrid[leftCoordinates[0]][leftCoordinates[1]];
                if (leftDistance < min)
                {
                    min = leftDistance;
                    minDirection = MoveDirection.LEFT;
                }
            }

            /*
            Check if the block below the ghost does not contain a block. Then, if it doesn't, check if
            it has the shortest distance to the target tile. (The distance to the target tile is the value
            stored in playerGrid). Make sure not to allow a direction change if previously moving up (no 180
            degree turns).
             */
            int[] downCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.DOWN.getDX())))};

            // avoid indexOutOfBounds
            if (downCoordinates[0] < playerGrid.length - 1 && moveDirection != MoveDirection.UP && !hasBlock(new Point2D(downCoordinates[1] * BLOCK_SIZE, downCoordinates[0] * BLOCK_SIZE)))
            {
                int downDistance = playerGrid[downCoordinates[0]][downCoordinates[1]];
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

    @Override
    public void scatter()
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
            stored in homeGrid). Make sure not to allow a direction change if previously moving down (no 180
            degree turns).
             */
            int[] upCoordinates = {(int) ((ghost.getPosition().getY() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDY()))),
                    (int) ((ghost.getPosition().getX() / BLOCK_SIZE + Math.signum(MoveDirection.UP.getDX())))};

            // avoid indexOutOfBounds
            if (upCoordinates[0] > 0 && moveDirection != MoveDirection.DOWN && !hasBlock(new Point2D(upCoordinates[1] * BLOCK_SIZE, upCoordinates[0] * BLOCK_SIZE)))
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
            if (rightCoordinates[1] < homeGrid[0].length - 1 && moveDirection != MoveDirection.LEFT && !hasBlock(new Point2D(rightCoordinates[1] * BLOCK_SIZE, rightCoordinates[0] * BLOCK_SIZE)))
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
            if (leftCoordinates[1] > 0 && moveDirection != MoveDirection.RIGHT && !hasBlock(new Point2D(leftCoordinates[1] * BLOCK_SIZE, leftCoordinates[0] * BLOCK_SIZE)))
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
            if (downCoordinates[0] < homeGrid.length - 1 && moveDirection != MoveDirection.UP && !hasBlock(new Point2D(downCoordinates[1] * BLOCK_SIZE, downCoordinates[0] * BLOCK_SIZE)))
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

    // Identical to the hasBlock() method in the PlayerControl class.
    private boolean hasBlock(Point2D tile)
    {
        GameApplication app = FXGL.getApp();

        List<Entity> blocks = app.getGameWorld().getEntitiesByType(EntityType.BLOCK);
        tile = new Point2D(((int) tile.getX() + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE, ((int) tile.getY() + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE);

        for (Entity block : blocks)
        {
            Point2D blockPos = ((GameEntity) block).getPosition();
            if (blockPos.equals(tile))
                return true;
        }

        return false;
    }

    private boolean onTile()
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

    /*
    A toString() for a 2D int array that I used to test the A* grids. Formats single digit numbers
    such as "1" to become "01" in order to maintain alignment.
     */

//    private static String toString(int[][] arr)
//    {
//        String output = "";
//        for(int r = 0; r < arr.length; r++)
//        {
//            for(int c = 0; c < arr[r].length; c++)
//            {
//                if(arr[r][c] < 10 && arr[r][c] != -1)
//                    output += "0";
//                output += arr[r][c] + ", ";
//            }
//            output = output.substring(0, output.length() - 2);
//            output += "\n";
//        }
//        return output;
//    }
}
