package com.gamedesign.pacman.collision;

import com.almasb.ents.Control;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.physics.CollisionHandler;
import com.gamedesign.pacman.GameState;
import com.gamedesign.pacman.PacmanApp;
import com.gamedesign.pacman.control.PlayerControl;
import com.gamedesign.pacman.control.ai.GhostControl;
import com.gamedesign.pacman.type.EntityType;

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

        if(player.getControlUnsafe(PlayerControl.class).getState() == "Energized")
        {
            for(Control c : ghost.getControls())
            {
                if(c instanceof GhostControl)
                {
                    ((GhostControl) c).respawn();
                }
            }
            app.setScore((int) (app.getScore() + 200 * Math.pow(2, app.getGhostMultiplier())));
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
