package com.gamedesign.pacman.collision;

import com.almasb.ents.Control;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.physics.CollisionHandler;
import com.gamedesign.pacman.EntityFactory;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.component.EnergizedComponent;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.control.ai.GhostControl;
import com.gamedesign.pacman.type.EntityType;
import javafx.util.Duration;

/**
 * Created by 1072524 on 3/6/2017.
 */
public class PlayerGhostHandler extends CollisionHandler
{
    public PlayerGhostHandler(EntityType player, EntityType ghost) {
        super(player, ghost);
    }

    @Override
    protected void onCollisionBegin(Entity player, Entity ghost) {
        PacmanApp app = (PacmanApp) FXGL.getApp();

        if(ghost.getComponentUnsafe(EnergizedComponent.class).getValue() == true)
        {
            for(Control c : ghost.getControls())
            {
                if(c instanceof GhostControl)
                {
                    ((GhostControl) c).respawn();
                }
            }
            int scoreToAdd = (int) (200 * Math.pow(2, app.getGhostMultiplier()));
            app.getGameWorld().addEntity(EntityFactory.newPopUp("+" + scoreToAdd, Duration.millis(500)));
            app.setScore(app.getScore() + scoreToAdd);
            app.setGhostMultiplier(app.getGhostMultiplier() + 1);
        }
        else
        {
            app.setGameState(GameState.PAUSED);
            app.loseLife();
            app.respawn();
        }
    }
}
