package com.gamedesign.pacman.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.EntityFactory;
import javafx.util.Duration;

/**
 * Created by lukel on 7/9/2017.
 */
public class PointBoostSpawnControl extends AbstractControl
{
    private LocalTimer spawnTimer;
    private GameEntity spawnPoint;

    @Override
    public void onAdded(Entity entity)
    {
        spawnTimer = FXGL.newLocalTimer();
        spawnTimer.capture();

        spawnPoint = (GameEntity) entity;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        if(spawnTimer.elapsed(Duration.seconds(20)))
        {
            FXGL.getApp().getGameWorld().addEntity(EntityFactory.newPointBoost(spawnPoint.getX(), spawnPoint.getY()));
            spawnPoint.removeFromWorld();
        }
    }
}
