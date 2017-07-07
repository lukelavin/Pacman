package com.gamedesign.pacman;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.time.UpdateEvent;
import com.almasb.fxgl.time.UpdateEventListener;
import com.almasb.fxgl.ui.UIController;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static com.gamedesign.pacman.Config.*;

public class PacmanUIController implements UIController, UpdateEventListener
{
    @FXML
    private Label title, score;

    @FXML
    private Canvas canvas;

    private GraphicsContext graphicsContext;

    @Override
    public void init()
    {
        title.setFont(Font.font(48));
        title.setText("Pac-man");
        title.setEffect(new DropShadow(0.5, 0.5, 1.0, Color.BLACK));
        title.setTextAlignment(TextAlignment.CENTER);

        score.setFont(new Font(24));

        graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setFill(Color.YELLOW);
    }

    @Override
    public void onUpdateEvent(UpdateEvent updateEvent)
    {

    }

    public Label getScore()
    {
        return score;
    }
}