package com.gamedesign.pacman;

import static com.gamedesign.pacman.Config.PACMAN_SPEED;

public enum MoveDirection
{
    UP(0, -PACMAN_SPEED), LEFT(-PACMAN_SPEED, 0), DOWN(0, PACMAN_SPEED), RIGHT(PACMAN_SPEED, 0);

    int dx, dy;

    MoveDirection(int dx, int dy){
        this.dx = dx;
        this.dy = dy;
    }

    public int getDX()
    {
        return dx;
    }

    public int getDY()
    {
        return dy;
    }

    MoveDirection next()
    {
        return values()[(ordinal() + 1) % values().length];
    }

    MoveDirection previous()
    {
        return values()[(ordinal() - 1) % values().length];
    }
}














