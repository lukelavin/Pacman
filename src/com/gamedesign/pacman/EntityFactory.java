package com.gamedesign.pacman;

import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.EntityView;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.RenderLayer;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.control.ai.BlinkyControl;
import com.gamedesign.pacman.control.ai.ClydeControl;
import com.gamedesign.pacman.control.ai.InkyControl;
import com.gamedesign.pacman.control.ai.PinkyControl;
import com.gamedesign.pacman.type.EntityType;
import com.gamedesign.pacman.type.GhostType;
import com.gamedesign.pacman.type.GhostTypeComponent;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

import static com.gamedesign.pacman.Config.*;

public class EntityFactory
{
    public static GameEntity newPlayer(double x, double y)
    {
        return Entities.builder()
                .at(new Point2D(x * BLOCK_SIZE, y * BLOCK_SIZE).add(PACMAN_OFFSET))
                .type(EntityType.PLAYER)
                .bbox(new HitBox("BODY", BoundingShape.circle(BLOCK_SIZE / 2 - 5)))
                .viewFromTexture(PACMAN_TEXTURES[0])
                //.viewFromNode(new Circle(BLOCK_SIZE / 2 - 5, Color.YELLOW))
                .with(new CollidableComponent(true))
                .with(new SpawnPointComponent())
                .with(new PlayerControl())
                .build();
    }

    public static GameEntity newBlinky(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE + 5, y * BLOCK_SIZE + 5)
                .type(EntityType.ENEMY)
                .bbox(new HitBox("BODY", BoundingShape.box(BLOCK_SIZE - 10, BLOCK_SIZE - 10)))
                .viewFromTexture(BLINKY_DOWN_TEXTURES[0])
                .with(new GhostTypeComponent(GhostType.BLINKY))
                .with(new CollidableComponent(true))
                .with(new SpawnPointComponent())
                .with(new BlinkyControl())
                .build();
    }

    public static GameEntity newPinky(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE + 5, y * BLOCK_SIZE + 5)
                .type(EntityType.ENEMY)
                .bbox(new HitBox("BODY", BoundingShape.box(BLOCK_SIZE - 10, BLOCK_SIZE - 10)))
                .viewFromTexture(PINKY_DOWN_TEXTURES[0])
                .with(new GhostTypeComponent(GhostType.PINKY))
                .with(new CollidableComponent(true))
                .with(new SpawnPointComponent())
                .with(new PinkyControl())
                .build();
    }

    public static GameEntity newInky(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE + 5, y * BLOCK_SIZE + 5)
                .type(EntityType.ENEMY)
                .bbox(new HitBox("BODY", BoundingShape.box(BLOCK_SIZE - 10, BLOCK_SIZE - 10)))
                .viewFromTexture(INKY_DOWN_TEXTURES[0])
                .with(new GhostTypeComponent(GhostType.INKY))
                .with(new CollidableComponent(true))
                .with(new SpawnPointComponent())
                .with(new InkyControl())
                .build();
    }

    public static GameEntity newClyde(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE + 5, y * BLOCK_SIZE + 5)
                .type(EntityType.ENEMY)
                .bbox(new HitBox("BODY", BoundingShape.box(BLOCK_SIZE - 10, BLOCK_SIZE - 10)))
                .viewFromTexture(CLYDE_DOWN_TEXTURES[0])
                .with(new GhostTypeComponent(GhostType.CLYDE))
                .with(new CollidableComponent(true))
                .with(new SpawnPointComponent())
                .with(new ClydeControl())
                .build();
    }

    public static GameEntity makeBlock(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE, y * BLOCK_SIZE)
                .type(EntityType.BLOCK)
                .bbox(new HitBox("BODY", BoundingShape.box(BLOCK_SIZE, BLOCK_SIZE)))
                .viewFromNode(new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.BLUE))
                .with(new CollidableComponent())
                .build();
    }

    public static GameEntity newPellet(double x, double y)
    {
        EntityView entityView = new EntityView(new Circle(BLOCK_SIZE / 8, Color.YELLOW));
        entityView.setRenderLayer(new RenderLayer()
        {
            @Override
            public String name()
            {
                return "BACKGROUND2";
            }

            @Override
            public int index()
            {
                return 1001;
            }
        });

        return Entities.builder()
                .at(x * BLOCK_SIZE + BLOCK_SIZE / 2 - BLOCK_SIZE / 8, y * BLOCK_SIZE + BLOCK_SIZE / 2 - BLOCK_SIZE / 8)
                .type(EntityType.PELLET)
                .bbox(new HitBox("BODY", BoundingShape.circle(BLOCK_SIZE / 8)))
                .viewFromNode(entityView)
                .with(new CollidableComponent(true))
                .build();
    }

    public static GameEntity newPowerPellet(double x, double y)
    {
        EntityView entityView = new EntityView(new Circle(BLOCK_SIZE / 4, Color.YELLOW));
        entityView.setRenderLayer(new RenderLayer()
        {
            @Override
            public String name()
            {
                return "BACKGROUND2";
            }

            @Override
            public int index()
            {
                return 1001;
            }
        });

        return Entities.builder()
                .at(x * BLOCK_SIZE + BLOCK_SIZE / 4, y * BLOCK_SIZE + BLOCK_SIZE / 4)
                .type(EntityType.POWERPELLET)
                .bbox(new HitBox("BODY", BoundingShape.circle(BLOCK_SIZE / 4)))
                .viewFromNode(entityView)
                .with(new CollidableComponent(true))
                .build();
    }

    public static GameEntity newTeleporter(double x, double y)
    {
        return Entities.builder()
                .at(x * BLOCK_SIZE, y * BLOCK_SIZE)
                .type(EntityType.TELEPORTER)
                .bbox(new HitBox("BODY", BoundingShape.circle(BLOCK_SIZE / 2)))
                .with(new CollidableComponent(true))
                .build();
    }
}




