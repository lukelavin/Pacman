package com.gamedesign.pacman.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.time.LocalTimer;
import com.gamedesign.pacman.type.EntityType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Created by lukel on 4/23/2017.
 */
public class PopUpControl extends AbstractControl
{
    private GameEntity popUp;
    private Label popUpText;
    private LocalTimer popUpTimer;
    private Duration duration;

    private GameEntity player(){
        return (GameEntity) FXGL.getApp().getGameWorld().getEntitiesByType(EntityType.PLAYER).get(0);
    }
    private PlayerControl playerControl;

    public PopUpControl(String popUpText, Duration duration)
    {
        popUpTimer = FXGL.newLocalTimer();
        popUpTimer.capture();

        this.popUpText = new Label(popUpText);
        this.popUpText.setTextFill(Color.WHITE);
        this.popUpText.setTranslateX(player().getX() - 20);
        this.popUpText.setTranslateY(player().getY() - 20);

        playerControl = player().getControlUnsafe(PlayerControl.class);

        this.duration = duration;
    }

    @Override
    public void onAdded(Entity entity)
    {
        popUp = (GameEntity) entity;
        FXGL.getApp().getGameScene().addUINode(popUpText);
    }

    @Override
    public void onUpdate(Entity entity, double v)
    {
        popUpText.setTranslateX(player().getX() - 20);
        popUpText.setTranslateY(player().getY() - 20);

        if(popUpTimer.elapsed(duration))
        {
            popUp.removeFromWorld();
            FXGL.getApp().getGameScene().removeUINode(popUpText);
        }
    }

    public void setPopUpText(Label popUpText)
    {
        this.popUpText = popUpText;
    }

    public void setPopUpText(String text)
    {
        popUpText.setText(text);
    }

    public void setDuration(Duration duration)
    {
        this.duration = duration;
    }
}
