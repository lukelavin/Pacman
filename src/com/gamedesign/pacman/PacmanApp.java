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
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.type.EntityType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static com.gamedesign.pacman.Config.*;

public class PacmanApp extends GameApplication
{
    private GameState gameState;

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
        gameSettings.setVersion("0.1");

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

//    private void updateBlockGrid(){
//        for(int r = 0; r < blockGrid.length; r++)
//        {
//            for(int c = 0; c < blockGrid[r].length; c++)
//            {
//                if(hasBlock(new Point2D(c * BLOCK_SIZE, r * BLOCK_SIZE)))
//                    blockGrid[r][c] = 1;
//            }
//        }
//    }

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
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.PELLET)
        {
            @Override
            protected void onCollisionBegin(Entity player, Entity pellet)
            {
                score.add(10);
                pellet.removeFromWorld();
            }
        });
    }

    @Override
    protected void initUI()
    {
        PacmanUIController pacmanUIController = new PacmanUIController();
        getMasterTimer().addUpdateListener(pacmanUIController);

        UI ui = getAssetLoader().loadUI("pacman_ui.fxml", pacmanUIController);
        ui.getRoot().setTranslateX(MAP_SIZE_X * BLOCK_SIZE);

        pacmanUIController.getScore()
                .textProperty()
                .bind(score.asString("Score: %d"));

        getGameScene().addUI(ui);
    }

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
    }

    public void blockGridsDone()
    {
        blockGridInitialized = true;
    }

    public void targetGridsDone()
    {
        gridsInitialized = true;
    }

    public AStarGridStorage getGridStorage()
    {
        return gridStorage;
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}









