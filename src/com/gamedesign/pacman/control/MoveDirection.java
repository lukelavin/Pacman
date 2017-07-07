package com.gamedesign.pacman.control;

import static com.gamedesign.pacman.Config.PACMAN_SPEED;

public enum MoveDirection
{
    UP(0, -PACMAN_SPEED),
    LEFT(-PACMAN_SPEED, 0),
    DOWN(0, PACMAN_SPEED),
    RIGHT(PACMAN_SPEED, 0);

    final int dx, dy;

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

    public MoveDirection inverse(){
        if(values()[ordinal()] == UP)
            return DOWN;
        else if (values()[ordinal()] == DOWN)
            return UP;
        else if (values()[ordinal()] == LEFT)
            return RIGHT;
        else
            return LEFT;
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














