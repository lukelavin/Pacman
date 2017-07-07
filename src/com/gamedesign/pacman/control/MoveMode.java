package com.gamedesign.pacman.control;

/**
 * Created by lukel on 1/29/2017.
 */
public enum MoveMode
{
    UNRELEASED(4000), SCATTERLONG(7000), ATTACKLONG(20000), SCATTERSHORT(5000), ATTACKFOREVER(-1);

    MoveMode(int duration){
        this.duration = duration;
    }

    int duration; // how long the mode should last (in milliseconds)

    public int getDuration()
    {
        return duration;
    }
}
