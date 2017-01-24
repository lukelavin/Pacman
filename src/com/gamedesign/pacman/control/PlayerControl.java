package com.gamedesign.pacman.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.GameEntity;
import com.gamedesign.pacman.MoveDirection;
import com.gamedesign.pacman.type.EntityType;
import javafx.geometry.Point2D;

import java.util.List;

import static com.gamedesign.pacman.Config.*;

public class PlayerControl extends AbstractControl
{
    private GameEntity gameEntity;
    private MoveDirection moveDirection;
    private double v;

    @Override
    public void onAdded(Entity entity)
    {
        gameEntity = (GameEntity) entity;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        this.v = v;
        if(moveDirection != null)
            move(moveDirection.getDX(), moveDirection.getDY());
    }

    public void up()
    {
        if(gameEntity.getPosition().getX() % BLOCK_SIZE != 0 && gameEntity.getPosition().getY() % BLOCK_SIZE != 0)
        {
            gameEntity.getPositionComponent().setX(((int) gameEntity.getPosition().getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            gameEntity.getPositionComponent().setY(((int) gameEntity.getPosition().getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
        }
        moveDirection = MoveDirection.UP;
        //move(0, -5 * speed);
        //move(0, -1 * v * PACMAN_SPEED);
        gameEntity.getRotationComponent().setValue(270);
        gameEntity.getView().setScaleX(1);
    }

    public void left()
    {
        if(gameEntity.getPosition().getX() % BLOCK_SIZE != 0 && gameEntity.getPosition().getY() % BLOCK_SIZE != 0)
        {
            gameEntity.getPositionComponent().setX(((int) gameEntity.getPosition().getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            gameEntity.getPositionComponent().setY(((int) gameEntity.getPosition().getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
        }
        moveDirection = MoveDirection.LEFT;
        //move(0, -5 * speed);
        //move(0, -1 * v * PACMAN_SPEED);
        gameEntity.getRotationComponent().setValue(270);
        gameEntity.getView().setScaleX(1);
    }

    public void down()
    {
        if(gameEntity.getPosition().getX() % BLOCK_SIZE != 0 && gameEntity.getPosition().getY() % BLOCK_SIZE != 0)
        {
            gameEntity.getPositionComponent().setX(((int) gameEntity.getPosition().getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            gameEntity.getPositionComponent().setY(((int) gameEntity.getPosition().getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
        }
        moveDirection = MoveDirection.DOWN;
        //move(0, -5 * speed);
        //move(0, -1 * v * PACMAN_SPEED);
        gameEntity.getRotationComponent().setValue(270);
        gameEntity.getView().setScaleX(1);
    }

    public void right()
    {
        if(gameEntity.getPosition().getX() % BLOCK_SIZE != 0 && gameEntity.getPosition().getY() % BLOCK_SIZE != 0)
        {
            gameEntity.getPositionComponent().setX(((int) gameEntity.getPosition().getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            gameEntity.getPositionComponent().setY(((int) gameEntity.getPosition().getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
        }
        moveDirection = MoveDirection.RIGHT;
        //move(0, -5 * speed);
        //move(0, -1 * v * PACMAN_SPEED);
        gameEntity.getRotationComponent().setValue(270);
        gameEntity.getView().setScaleX(1);
    }

    private List<Entity> blocks;
    private boolean hasTeleported;

    private void move(double dx, double dy)
    {
        gameEntity.getPositionComponent().translate(v * dx, v * dy);

        if(blocks == null)
            blocks = FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.BLOCK);

        boolean collision = false;

        for (Entity block : blocks)
        {
            if(Entities.getBBox(block)
                    .isCollidingWith(gameEntity.getBoundingBoxComponent()))
            {
                collision = true;
                break;
            }
        }

        if(collision)
        {
            gameEntity.getPositionComponent().translate(-v * dx, -v * dy);
            gameEntity.getPositionComponent().setX(((int) gameEntity.getPosition().getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            gameEntity.getPositionComponent().setY(((int) gameEntity.getPosition().getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE + 5);
            moveDirection = null;
        }

        Point2D leftTeleport = new Point2D(0, 9 * BLOCK_SIZE);
        Point2D rightTeleport = new Point2D(18 * BLOCK_SIZE, 9 * BLOCK_SIZE);

        if(((int) gameEntity.getPosition().getX() / BLOCK_SIZE * BLOCK_SIZE == leftTeleport.getX()) &&
            (int) gameEntity.getPosition().getY() / BLOCK_SIZE * BLOCK_SIZE == leftTeleport.getY() &&
            moveDirection == MoveDirection.LEFT &&
                !hasTeleported)
        {
            gameEntity.getPositionComponent().setValue(rightTeleport.add(5, 5));
            hasTeleported = true;
        }
        else if(((int) gameEntity.getPosition().getX() / BLOCK_SIZE * BLOCK_SIZE == rightTeleport.getX()) &&
                (int) gameEntity.getPosition().getY() / BLOCK_SIZE * BLOCK_SIZE == rightTeleport.getY() &&
                moveDirection == MoveDirection.RIGHT && !hasTeleported)
        {
            gameEntity.getPositionComponent().setValue(leftTeleport.add(5, 5));
            hasTeleported = true;
        }
        else
            hasTeleported = false;
//        if(!getEntity().isActive())
//            return;
//
//        if(blocks == null)
//            blocks = FXGL.getApp().getGameWorld()
//                    .getEntitiesByType(EntityType.BLOCK);
//
//        double magnitude = Math.sqrt(dx * dx + dy * dy);
//        long length = Math.round(magnitude);
//
//        double x = dx / magnitude;
//        double y = dy / magnitude;
//
//        for(int i = 0; i < length; i++)
//        {
//            gameEntity.getPositionComponent().translate(x, y);
//
//            boolean collision = false;
//
//            for (Entity block : blocks)
//            {
//                if(Entities.getBBox(block)
//                        .isCollidingWith(gameEntity.getBoundingBoxComponent())
//                {
//                    collision = true;
//                    break;
//                }
//            }
//
//            if(collision)
//            {
//                gameEntity.getPositionComponent().translate(-x, -y);
//                break;
//            }
//        }
    }
}








