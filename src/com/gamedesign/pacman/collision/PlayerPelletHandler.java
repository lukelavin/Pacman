package com.gamedesign.pacman.collision;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.physics.CollisionHandler;
import com.gamedesign.pacman.EntityFactory;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.type.EntityType;
import javafx.util.Duration;

/**
 * Created by 1072524 on 3/6/2017.
 */
public class PlayerPelletHandler extends CollisionHandler
{
    public PlayerPelletHandler(EntityType player, EntityType pellet) {
        super(player, pellet);
    }

    @Override
    protected void onCollisionBegin(Entity player, Entity pellet) {
        PacmanApp app = (PacmanApp) FXGL.getApp();
        app.setScore(app.getScore() + 10);
        //app.getGameWorld().addEntity(EntityFactory.newPopUp("+10", Duration.millis(100)));

        pellet.removeFromWorld();
        app.decrementPellets();
        app.checkLevelAdvance();
    }
}
