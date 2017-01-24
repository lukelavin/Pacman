package com.gamedesign.pacman;

import javafx.scene.input.KeyCode;

public class Config
{
    public static final KeyCode UP_KEY = KeyCode.W;
    public static final KeyCode LEFT_KEY = KeyCode.A;
    public static final KeyCode DOWN_KEY = KeyCode.S;
    public static final KeyCode RIGHT_KEY = KeyCode.D;

    public static final int BLOCK_SIZE = 40;
    public static final int MAP_SIZE = 21;
    public static final int UI_SIZE = 200;

    public static final int PACMAN_SPEED = 5 * 60;
}
