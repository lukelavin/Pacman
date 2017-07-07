package com.gamedesign.pacman;

import com.gamedesign.pacman.control.MoveMode;
import javafx.geometry.Point2D;
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
    
    //public static final String[] BLINKY_TEXTURES = {"blinky0.png", "blinky1.png"};
    public static final String[] BLINKY_DOWN_TEXTURES = {"blinkyBottom00.png", "blinkyBottom01.png"};
    public static final String[] BLINKY_UP_TEXTURES = {"blinkyTop00.png", "blinkyTop01.png"};
    public static final String[] BLINKY_RIGHT_TEXTURES = {"blinkyRight00.png", "blinkyRight01.png"};
    public static final String[] BLINKY_LEFT_TEXTURES = {"blinkyLeft00.png", "blinkyLeft01.png"};
    public static final MoveMode[] BLINKY_MODES = {MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKFOREVER};
    
    //public static final String[] PINKY_TEXTURES = {"pinky0.png", "pinky1.png"};
    public static final String[] PINKY_DOWN_TEXTURES = {"pinkyBottom00.png", "pinkyBottom01.png"};
    public static final String[] PINKY_UP_TEXTURES = {"pinkyTop00.png", "pinkyTop01.png"};
    public static final String[] PINKY_RIGHT_TEXTURES = {"pinkyRight00.png", "pinkyRight01.png"};
    public static final String[] PINKY_LEFT_TEXTURES = {"pinkyLeft00.png", "pinkyLeft01.png"};
    public static final MoveMode[] PINKY_MODES = {MoveMode.UNRELEASED, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKFOREVER};
    
    //public static final String[] INKY_TEXTURES = {"inky0.png", "inky1.png"};
    public static final String[] INKY_DOWN_TEXTURES = {"inkyBottom00.png", "inkyBottom01.png"};
    public static final String[] INKY_UP_TEXTURES = {"inkyTop00.png", "inkyTop01.png"};
    public static final String[] INKY_RIGHT_TEXTURES = {"inkyRight00.png", "inkyRight01.png"};
    public static final String[] INKY_LEFT_TEXTURES = {"inkyLeft00.png", "inkyLeft01.png"};
    public static final MoveMode[] INKY_MODES = {MoveMode.UNRELEASED, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKFOREVER};
    
    //public static final String[] CLYDE_TEXTURES = {"clyde0.png", "clyde1.png"};
    public static final String[] CLYDE_DOWN_TEXTURES = {"clydeBottom00.png", "clydeBottom01.png"};
    public static final String[] CLYDE_UP_TEXTURES = {"clydeTop00.png", "clydeTop01.png"};
    public static final String[] CLYDE_RIGHT_TEXTURES = {"clydeRight00.png", "clydeRight01.png"};
    public static final String[] CLYDE_LEFT_TEXTURES = {"clydeLeft00.png", "clydeLeft01.png"};
    public static final MoveMode[] CLYDE_MODES = {MoveMode.UNRELEASED, MoveMode.ATTACKFOREVER};

    
    public static final int PACMAN_SPEED = 2;
    public static final Point2D PACMAN_OFFSET = new Point2D(5, 5);
}
