package com.gamedesign.pacman;

import javafx.scene.input.KeyCode;

public class Config
{
    public static final KeyCode UP_KEY = KeyCode.W;
    public static final KeyCode LEFT_KEY = KeyCode.A;
    public static final KeyCode DOWN_KEY = KeyCode.S;
    public static final KeyCode RIGHT_KEY = KeyCode.D;

    public static final int BLOCK_SIZE = 40;
    public static final int MAP_SIZE_X = 19;
    public static final int MAP_SIZE_Y = 21;
    public static final int UI_SIZE = 200;

    public static final String[] PACMAN_TEXTURES = {"pacman0.png", "pacman1.png", "pacman2.png", "pacman1.png"};
    public static final String[] BLINKY_TEXTURES = {"blinky0.png", "blinky1.png"};
    public static final String[] PINKY_TEXTURES = {"pinky0.png", "pinky1.png"};

    public static final int PACMAN_SPEED = 2 * 60;
}
