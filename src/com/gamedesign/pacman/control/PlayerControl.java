package com.gamedesign.pacman.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.type.EntityType;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;

import static com.gamedesign.pacman.Config.*;

public class PlayerControl extends AbstractControl
{
    private GameEntity gameEntity;
    private MoveDirection moveDirection;
    private MoveDirection prevDirection;
    private LocalTimer textureTimer;
    private int i;
    private double v;

    @Override
    public void onAdded(Entity entity)
    {
        gameEntity = (GameEntity) entity;
        textureTimer = FXGL.newLocalTimer();
        textureTimer.capture();
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        this.v = v;
        if (moveDirection != null)
        {
            move();
        }
        else // if Pacman is stopped, just set the texture to the default and repeatedly capture to stop animating
        {
            i = 0;
            gameEntity.getMainViewComponent().setView(new ImageView("assets/textures/" + PACMAN_TEXTURES[i]));
            textureTimer.capture();
        }

        // every 50 ms, if Pacman is moving, switch to the next texture
        // 50 is arbitrary, could and probably should be derived from speed in the future
        if(textureTimer.elapsed(Duration.millis(50))){
            i = (i + 1) % (PACMAN_TEXTURES.length);

            gameEntity.getMainViewComponent().setView(new ImageView("assets/textures/" + PACMAN_TEXTURES[i]));
            handleTexture(); // make sure to update the rotation with the new view
            textureTimer.capture();
        }
    }

    public void up()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.UP;
    }

    public void left()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.LEFT;
    }

    public void down()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.DOWN;
    }

    public void right()
    {
        // only store the current direction in prevDirection if you've actually moved in that direction
        if(prevDirection == moveDirection)
            prevDirection = moveDirection;

        moveDirection = MoveDirection.RIGHT;
    }

    /*
    Every onUpdate(), if the Pacman should be moving, move() will get called. The move() method
    handles all movement based on the direction fields, and takes no parameters. The move() method
    also currently handles teleports (which are hard-coded in). The move() method detects possible
    collisions and will wait to change direction until a space is free, or if no space becomes free,
    the move() method will stop movement and the method won't be called until another key is pressed.

    Note: Compensation for v almost surely doesn't work right now. I can't really slow my computer down
    artificially to test it out, but I'm pretty sure I can say that there's no way it works.
     */
    private void move()
    {
        // if this is the first time moving after being stopped, save moveDirection to prevDirection
        // also, make sure the texture is updated
        if(prevDirection == null)
        {
            prevDirection = moveDirection;
            handleTexture();
        }

        // store the dx and dy of moveDirection for convenience
        double dx = moveDirection.getDX();
        double dy = moveDirection.getDY();

        // store the dx and dy of prevDirection for convenience
        double prevdx = prevDirection.getDX();
        double prevdy = prevDirection.getDY();

        /*
        onTile() checks are only necessary if moveDirection is a new direction not yet
        moved in. If moveDirection = prevDirection, onTile() is only needed to check for dead
        ends.
         */
        if(prevDirection != moveDirection)
        {
            // only try and move in the new direction if you're on a tile
            if (onTile())
            {
                // if the space in the new direction is open, move and then set prevDirection to the new direction
                if (!hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(dx), BLOCK_SIZE * Math.signum(dy))))
                {
                    gameEntity.getPositionComponent().translate(v * dx, v * dy);
                    prevDirection = moveDirection;
                    handleTexture();
                }
                else // if the space in the new direction is blocked, try and move in the previous direction
                {
                    if(!hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(prevdx), BLOCK_SIZE * Math.signum(prevdy))))
                        gameEntity.getPositionComponent().translate(v * prevdx, v * prevdy);
                    else // if you can't move in the previous direction, stop movement
                        stopMovement();
                }
            }
            else // keep moving in the same direction until you reach a tile, so you can check the new direction again
            {
                gameEntity.getPositionComponent().translate(v * prevdx, v * prevdy);
            }
        }
        else // if moveDirection == prevDirection, you only need to check if blocks are in one direction
        {
            // if you've reached the end of a tile and there's a block in your way, stop all movement
            if(onTile() && hasBlock(gameEntity.getPosition().add(BLOCK_SIZE * Math.signum(dx), BLOCK_SIZE * Math.signum(dy))))
                stopMovement();
            else // if you can move, keep moving
                gameEntity.getPositionComponent().translate(v * dx, v * dy);
        }

        Point2D leftTeleport = new Point2D(0, 9 * BLOCK_SIZE);
        Point2D rightTeleport = new Point2D(18 * BLOCK_SIZE, 9 * BLOCK_SIZE);

        if(((int) gameEntity.getPosition().getX() / BLOCK_SIZE * BLOCK_SIZE == leftTeleport.getX()) &&
            (int) gameEntity.getPosition().getY() / BLOCK_SIZE * BLOCK_SIZE == leftTeleport.getY() &&
                (moveDirection == MoveDirection.LEFT || prevDirection == MoveDirection.LEFT))
        {
            gameEntity.getPositionComponent().setValue(rightTeleport.add(5, 5));
        }
        else if(((int) gameEntity.getPosition().getX() / BLOCK_SIZE * BLOCK_SIZE == rightTeleport.getX()) &&
                (int) gameEntity.getPosition().getY() / BLOCK_SIZE * BLOCK_SIZE == rightTeleport.getY() &&
                (moveDirection == MoveDirection.RIGHT || prevDirection == MoveDirection.RIGHT))
        {
            gameEntity.getPositionComponent().setValue(leftTeleport.add(5, 5));
        }
    }

    private boolean onTile(){
        double x = gameEntity.getPosition().getX();
        double y = gameEntity.getPosition().getY();

        /*
        Sometimes, Pacman will travel past the onTile range within the span of one frame,
        going across a whole tile without every returning true to an onTile test.

        Because of this, You need to check a wider range depending on how fast
        Pacman's travels. The current code is functional only if Pacman's speed is
        less than or equal to 3 * 60. A faster moving Pacman seems excessive anyways
        if not for just a gimmicky "Speed Pacman" game.
        */

        //check if Pacman is on a tile (with a margin of error of 3 pixels)
        boolean xOnTile = (((int) x - 5) % BLOCK_SIZE == 39) ||
                            (((int) x - 5) % BLOCK_SIZE == 0) ||
                            (((int) x - 5) % BLOCK_SIZE == 1);
        boolean yOnTile = (((int) y - 5) % BLOCK_SIZE == 39) ||
                            (((int) y - 5) % BLOCK_SIZE == 0) ||
                            (((int) y - 5) % BLOCK_SIZE == 1);

        return xOnTile && yOnTile;
    }

    /*
    Not actually used right now, but might be used for ghost path finding, so I still
    have it here. Uses simple integer math to find the origin point for the closest tile
    to Pacman.
     */
    public Point2D nearestTile(){
        //return the pixel origin of the nearest tile to Pacman

        return new Point2D(((int) gameEntity.getPosition().getX() - 5 + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE,
                ((int) gameEntity.getPosition().getY() - 5 + BLOCK_SIZE / 2) / 40 * BLOCK_SIZE);
    }

    /*
    Takes in a Point2D, finds the tile origin point for that Point2D, then checks
    if that point is the origin point for any blocks. Used in collision prevention.
     */
    private boolean hasBlock(Point2D tile)
    {
        GameApplication app = FXGL.getApp();

        List<Entity> blocks = app.getGameWorld().getEntitiesByType(EntityType.BLOCK);
        tile = new Point2D(((int) tile.getX() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE, ((int) tile.getY() + BLOCK_SIZE / 2 - 5) / 40 * BLOCK_SIZE);

        for(Entity block : blocks){
            Point2D blockPos = ((GameEntity) block).getPosition();
            if(blockPos.equals(tile))
                return true;
        }

        return false;
    }

    private void stopMovement()
    {
        prevDirection = null;
        moveDirection = null;
    }

    /*
    Change the direction of the texture based on the prevDirection to make sure
    the texture isn't rotated before Pacman is actually moving in a new direction.

    Only is called when necessary for efficiency.
    */
    private void handleTexture()
    {
        if (prevDirection == MoveDirection.UP)
            gameEntity.getRotationComponent().setValue(270);
        else if (prevDirection == MoveDirection.LEFT)
            gameEntity.getRotationComponent().setValue(180);
        else if (prevDirection == MoveDirection.DOWN)
            gameEntity.getRotationComponent().setValue(90);
        else if (prevDirection == MoveDirection.RIGHT)
            gameEntity.getRotationComponent().setValue(0);

        gameEntity.getView().setScaleX(1);
    }
}








