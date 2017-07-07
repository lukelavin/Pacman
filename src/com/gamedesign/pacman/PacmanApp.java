package com.gamedesign.pacman;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.RenderLayer;
import com.almasb.fxgl.gameplay.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.parser.TextLevelParser;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import com.almasb.fxgl.ui.UI;
import com.gamedesign.pacman.collision.PlayerGhostHandler;
import com.gamedesign.pacman.collision.PlayerPelletHandler;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.control.ai.*;
import com.gamedesign.pacman.type.EntityType;
import com.gamedesign.pacman.type.GhostType;
import com.gamedesign.pacman.type.GhostTypeComponent;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.gamedesign.pacman.Config.*;
import static com.gamedesign.pacman.type.GhostType.*;

public class PacmanApp extends GameApplication
{
    private GameState gameState;

    private GameEntity player()
    {
        return (GameEntity) getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0);
    }
    private PlayerControl playerControl()
    {
        return getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0).getControlUnsafe(PlayerControl.class);
    }

    private IntegerProperty score;

    private AStarGridStorage gridStorage;

    @Override
    protected void initSettings(GameSettings gameSettings)
    {
        gameSettings.setWidth(MAP_SIZE_X * BLOCK_SIZE + UI_SIZE);
        gameSettings.setHeight(MAP_SIZE_Y * BLOCK_SIZE);
        gameSettings.setTitle("Pacman");
        gameSettings.setVersion("0.8");

        gameSettings.setFullScreen(false);
        gameSettings.setIntroEnabled(false);
        gameSettings.setMenuEnabled(false);
        gameSettings.setProfilingEnabled(false);    // disables FPS
        gameSettings.setCloseConfirmation(false);

        gameSettings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initInput()
    {
        getInput().addAction(new UserAction("Up")
        {
            @Override
            protected void onActionBegin()
            {
                playerControl().up();
            }
        }, UP_KEY);
        getInput().addAction(new UserAction("Left")
        {
            @Override
            protected void onActionBegin()
            {
                playerControl().left();
            }
        }, LEFT_KEY);
        getInput().addAction(new UserAction("Down")
        {
            @Override
            protected void onActionBegin()
            {
                playerControl().down();
            }
        }, DOWN_KEY);
        getInput().addAction(new UserAction("Right")
        {
            @Override
            protected void onActionBegin()
            {
                playerControl().right();
            }
        }, RIGHT_KEY);
    }

    @Override
    protected void initAssets()
    {

    }

    public void setGameState(GameState newState)
    {
        gameState = newState;
    }

    public GameState getGameState()
    {
        return gameState;
    }

    public static boolean blockGridInitialized;
    public static boolean gridsInitialized;

    @Override
    protected void initGame()
    {
        gameState = GameState.LOADING;

        getGameWorld().addEntity(EntityFactory.newPlayer(2, 2));

        TextLevelParser parser = new TextLevelParser();
        parser.setEmptyChar(' ');
        parser.addEntityProducer('P', EntityFactory::newPlayer);
        parser.addEntityProducer('B', EntityFactory::makeBlock);
        parser.addEntityProducer('.', EntityFactory::newPellet);
        parser.addEntityProducer('T', EntityFactory::newTeleporter);
        parser.addEntityProducer('b', EntityFactory::newBlinky);
        parser.addEntityProducer('p', EntityFactory::newPinky);
        parser.addEntityProducer('i', EntityFactory::newInky);
        parser.addEntityProducer('c', EntityFactory::newClyde);

        Level level = parser.parse("levels/level.txt");

        getGameWorld().setLevel(level);

        blockGridInitialized = false;

        gridStorage = new AStarGridStorage();

        GameEntity background = Entities.builder()
                .type(EntityType.BACKGROUND)
                .viewFromNode(new Rectangle(getWidth(), getHeight(), Color.BLACK)) //use the height and width of CURRENT SCREEN
                .buildAndAttach(getGameWorld());

        background.setRenderLayer(RenderLayer.BACKGROUND);

        score = new SimpleIntegerProperty();
    }

    public void respawn()
    {
        playerControl().respawn();
        List<Entity> ghosts = getGameWorld().getEntitiesByType(EntityType.ENEMY);
        for(int i = ghosts.size() - 1; i >= 0; i--)
        {
            System.out.println(i);
            GhostType ghostType = ghosts.get(i).getComponentUnsafe(GhostTypeComponent.class).getValue();
            System.out.println(ghostType);
            switch (ghostType)
            {
                case BLINKY:
                    ghosts.get(i).getControlUnsafe(BlinkyControl.class).respawn();
                    break;
                case PINKY:
                    ghosts.get(i).getControlUnsafe(PinkyControl.class).respawn();
                    break;
                case INKY:
                    ghosts.get(i).getControlUnsafe(InkyControl.class).respawn();
                    break;
                case CLYDE:
                    ghosts.get(i).getControlUnsafe(ClydeControl.class).respawn();
                    break;
            }
        }
    }

    public static boolean hasBlock(Point2D tile)
    {
        List<Entity> blocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK);
        tile = new Point2D( tile.getX(),  tile.getY());

        for (Entity block : blocks)
        {
            Point2D blockPos = ((GameEntity) block).getPosition();
            if (blockPos.equals(tile))
                return true;
        }

        return false;
    }

    @Override
    protected void initPhysics()
    {
        getPhysicsWorld().addCollisionHandler(new PlayerPelletHandler(EntityType.PLAYER, EntityType.PELLET));
        getPhysicsWorld().addCollisionHandler(new PlayerGhostHandler(EntityType.PLAYER, EntityType.ENEMY));
    }

    private ArrayList<Circle> lives;
    @Override
    protected void initUI()
    {
        // I have no idea whats going on here.
        PacmanUIController pacmanUIController = new PacmanUIController();
        getMasterTimer().addUpdateListener(pacmanUIController);

        UI ui = getAssetLoader().loadUI("pacman_ui.fxml", pacmanUIController);
        ui.getRoot().setTranslateX(MAP_SIZE_X * BLOCK_SIZE);

        pacmanUIController.getScore()
                .textProperty()
                .bind(score.asString("Score: %d"));

        getGameScene().addUI(ui);

        Label livesLabel = new Label("Lives:");
        livesLabel.setFont(new Font(24));
        livesLabel.setTranslateX(BLOCK_SIZE * MAP_SIZE_X + 5);
        livesLabel.setTranslateY(BLOCK_SIZE * MAP_SIZE_Y - 45 - 24);
        livesLabel.setTextFill(Color.WHITE);
        getGameScene().addUINode(livesLabel);

        lives = new ArrayList<Circle>();
        lives.add(new Circle(15, Color.YELLOW));
        lives.get(0).setStroke(Color.BLACK);
        lives.add(new Circle(15, Color.YELLOW));
        lives.get(1).setStroke(Color.BLACK);
        lives.add(new Circle(15, Color.YELLOW));
        lives.get(2).setStroke(Color.BLACK);
        for(int i = 0; i < lives.size(); i++){
            lives.get(i).setTranslateX(BLOCK_SIZE * MAP_SIZE_X + 20 + (i * 35));
            lives.get(i).setTranslateY(BLOCK_SIZE * MAP_SIZE_Y - 20);
            getGameScene().addUINode(lives.get(i));
        }
    }

    public void loseLife()
    {
        if(lives.size() == 0)
            gameState = GameState.OVER;
        else
        {
            getGameScene().removeUINode(lives.get(lives.size() - 1));
            lives.remove(lives.size() - 1);
        }
    }

    int pausedI = 0;

    @Override
    protected void onUpdate(double v)
    {
        if(!blockGridInitialized)
        {
            gridStorage.makeBlockGrid();
            blockGridInitialized = true;
        }
        else if(!gridsInitialized)
        {
            gridStorage.makeGrids();
            gridsInitialized = true;
            gameState = GameState.ACTIVE;
        }

        if(gameState == GameState.PAUSED)
        {
            pausedI++;
            if(pausedI >= 60)
            {
                gameState = GameState.ACTIVE;
                pausedI = 0;
            }
        }

        if(gameState == GameState.OVER)
            gameOver();
    }

    public void gameOver()
    {
        Label gameOver = new Label("Game Over!");
        gameOver.setFont(new Font(48));
        gameOver.setTextFill(Color.WHITE);
        gameOver.setTranslateX(getWidth() / 2 - gameOver.getText().length() * 24);

        getGameScene().addUINode(gameOver);
    }

    public AStarGridStorage getGridStorage()
    {
        return gridStorage;
    }

    public int getScore() {
        return score.get();
    }

    public void setScore(int score) {
        this.score.set(score);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}









