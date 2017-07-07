package com.gamedesign.pacman;

import com.almasb.fxgl.app.FXGL;
import com.gamedesign.pacman.type.EntityType;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.PacmanApp.hasBlock;

/**
 * Created by lukel on 2/7/2017.
 */
public class AStarGridStorage
{
    private int[][] blockGrid;
    private int[][][][] grids;

    public AStarGridStorage()
    {
        blockGrid = new int[MAP_SIZE_Y][MAP_SIZE_X];
        grids = new int[MAP_SIZE_Y][MAP_SIZE_X][MAP_SIZE_Y][MAP_SIZE_X];
    }

    public void makeBlockGrid()
    {
        System.out.println("Making Block Grid...");
        for(int r = 0; r < blockGrid.length; r++)
        {
            for(int c = 0; c < blockGrid[r].length; c++)
            {
                if(hasBlock(new Point2D(c * BLOCK_SIZE, r * BLOCK_SIZE)))
                    blockGrid[r][c] = 1;
            }
        }
        System.out.println("Done");
    }

    public void makeGrids()
    {
        System.out.println("Making Target Grids...");
        for(int storageRow = 0; storageRow < grids.length; storageRow++)
        {
            for(int storageCol = 0; storageCol < grids[storageRow].length; storageCol++)
            {
                // fill the array with a sentinel value
                for (int i = 0; i < grids[storageRow][storageCol].length; i++)
                    for (int j = 0; j < grids[storageRow][storageCol][i].length; j++)
                        grids[storageRow][storageCol][i][j] = -1;

                int distance = 0;
                List<int[]> recentlyMarked = new ArrayList<int[]>();

                grids[storageRow][storageCol][storageRow][storageCol] = distance;
                distance++;
                recentlyMarked.add(new int[] {storageRow, storageCol});
                int numberOfSentinels = grids[storageRow][storageCol].length * grids[storageRow][storageCol][0].length - 1;

                int numberOfBlocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK).size();

                while(numberOfSentinels > numberOfBlocks && !recentlyMarked.isEmpty())
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
                            if (grids[storageRow][storageCol][r - 1][c] == -1 && blockGrid[r - 1][c] == 0)
                            {
                                grids[storageRow][storageCol][r - 1][c] = distance;
                                numberOfSentinels--;
                                temp.add(new int[] {r - 1, c});
                            }
                        }

                        //check if the tile to the right exists and does not have a block
                        //if so, then its distance from the goal
                        if (c < grids[storageRow][storageCol][r].length - 1)
                        {
                            if (grids[storageRow][storageCol][r][c + 1] == -1 && blockGrid[r][c + 1] == 0)
                            {
                                grids[storageRow][storageCol][r][c + 1] = distance;
                                numberOfSentinels--;
                                temp.add(new int[] {r, c + 1});
                            }
                        }

                        //check if the tile below exists and does not have a block
                        //if so, mark its distance from the goal
                        if (r < grids[storageRow][storageCol].length - 1)
                        {
                            if (grids[storageRow][storageCol][r + 1][c] == -1 && blockGrid[r + 1][c] == 0)
                            {
                                grids[storageRow][storageCol][r + 1][c] = distance;
                                numberOfSentinels--;
                                temp.add(new int[] {r + 1, c});
                            }
                        }

                        //check if the tile to the left exists and does not have a block
                        //if so, then mark its distance from the goal
                        if (c > 0)
                        {
                            if (grids[storageRow][storageCol][r][c - 1] == -1 && blockGrid[r][c - 1] == 0)
                            {
                                grids[storageRow][storageCol][r][c - 1] = distance;
                                numberOfSentinels--;
                                temp.add(new int[] {r, c - 1});
                            }
                        }
                    }
                    recentlyMarked = temp;
                    distance++;
                }
            }
        }
        System.out.println("Done");
    }

    public int[][] getBlockGrid()
    {
        return blockGrid;
    }

    public int[][] getGrid(int x, int y)
    {
        return grids[x][y];
    }


    // quick toString method for debugging
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
