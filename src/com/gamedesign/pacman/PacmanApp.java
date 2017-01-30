package com.gamedesign.pacman;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.RenderLayer;
import com.almasb.fxgl.gameplay.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.parser.TextLevelParser;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.type.EntityType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import static com.gamedesign.pacman.Config.*;

public class PacmanApp extends GameApplication
{

    private PlayerControl playerControl()
    {
        return getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0).getControlUnsafe(PlayerControl.class);
    }

    private IntegerProperty score;


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

    @Override
    protected void initGame()
    {
        getGameWorld().addEntity(EntityFactory.newPlayer(2, 2));

        TextLevelParser parser = new TextLevelParser();
        parser.setEmptyChar(' ');
        parser.addEntityProducer('P', EntityFactory::newPlayer);
        parser.addEntityProducer('B', EntityFactory::makeBlock);
        parser.addEntityProducer('.', EntityFactory::newPellet);
        parser.addEntityProducer('b', EntityFactory::newBlinky);

        Level level = parser.parse("levels/level.txt");

        getGameWorld().setLevel(level);

        GameEntity background = Entities.builder()
                .type(EntityType.BACKGROUND)
                .viewFromNode(new Rectangle(getWidth(), getHeight(), Color.BLACK)) //use the height and width of CURRENT SCREEN
                .buildAndAttach(getGameWorld());

        background.setRenderLayer(RenderLayer.BACKGROUND);

        score = new SimpleIntegerProperty();
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

    }

    @Override
    protected void onUpdate(double v)
    {

    }

    public static void main(String[] args)
    {
        launch(args);
    }
}









