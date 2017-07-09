package com.gamedesign.pacman.collision;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.physics.CollisionHandler;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.type.EntityType;

/**
 * Created by 1072524 on 3/6/2017.
 */
public class PlayerPowerPelletHandler extends CollisionHandler
{
    public PlayerPowerPelletHandler(EntityType player, EntityType powerPellet) {
        super(player, powerPellet);
    }

    @Override
    protected void onCollisionBegin(Entity player, Entity powerPellet) {
        PacmanApp app = (PacmanApp) FXGL.getApp();
        app.setScore(app.getScore() + 50);
        app.energizePlayer();

        powerPellet.removeFromWorld();
    }
}
