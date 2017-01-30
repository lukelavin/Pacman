package com.gamedesign.pacman.control.ai;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.control.MoveDirection;
import com.gamedesign.pacman.control.MoveMode;
import com.gamedesign.pacman.type.EntityType;
import javafx.util.Duration;

/**
 * Created by lukel on 1/29/2017.
 */
public abstract class GhostControl extends AbstractControl
{
    MoveDirection moveDirection;
    LocalTimer modeTimer;
    final MoveMode[] mode = {MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERLONG, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKLONG, MoveMode.SCATTERSHORT, MoveMode.ATTACKFOREVER};
    int i;
    double v;

    GameEntity player(){
        return (GameEntity) FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0);
    }

    @Override
    public void onAdded(Entity entity)
    {
        modeTimer = FXGL.newLocalTimer();
        i = 0;
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        this.v = v;

        /*
        Ghosts in Pacman do not, in fact, chase Pacman the entire time. Ghosts alternate between
        periods of attacking Pacman and periods of "scattering" back to their home corners. This
        block of code uses the sequence defined in mode[] to determine the right mode for the ghosts.
         */
        switch (mode[i])
        {
            case SCATTERLONG:
                scatter();
                if(modeTimer.elapsed(Duration.millis(mode[i].getDuration())))
                {
                    i++;
                    modeTimer.capture();
                }
                break;

            case SCATTERSHORT:
                scatter();
                if(modeTimer.elapsed(Duration.millis(mode[i].getDuration())))
                {
                    i++;
                    modeTimer.capture();
                }
                break;

            case ATTACKLONG:
                attack();
                if(modeTimer.elapsed(Duration.millis(mode[i].getDuration())))
                {
                    i++;
                    modeTimer.capture();
                }
                break;

            case ATTACKFOREVER:
                attack();
                break;
        }
    }

    public abstract void attack();

    public abstract void scatter();
}
