package com.gamedesign.pacman;

import com.almasb.ents.component.ObjectComponent;
import javafx.geometry.Point2D;

/**
 * Created by 1072524 on 3/9/2017.
 */
public class SpawnPointComponent extends ObjectComponent<Point2D>
{
    public SpawnPointComponent(Point2D initialValue) {
        super(initialValue);
    }

    public SpawnPointComponent()
    {
        super(new Point2D(0, 0));
    }

    public void setSpawn(Point2D point2D)
    {
        setValue(point2D);
    }
}
