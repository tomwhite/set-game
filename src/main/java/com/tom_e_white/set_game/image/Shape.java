package com.tom_e_white.set_game.image;

import georegression.geometry.UtilPoint2D_F32;
import georegression.struct.point.Point2D_F32;
import georegression.struct.shapes.Polygon2D_F32;
import georegression.struct.shapes.RectangleLength2D_F32;

import java.util.List;

public class Shape {

    private final Polygon2D_F32 polygon;
    private final RectangleLength2D_F32 boundingBox;

    public Shape(List<Point2D_F32> points) {
        int i = 0;
        float[] floatPoints = new float[points.size() * 2];
        for (Point2D_F32 p : points) {
            floatPoints[i++] = p.x;
            floatPoints[i++] = p.y;
        }
        this.polygon = new Polygon2D_F32(floatPoints);
        this.boundingBox = UtilPoint2D_F32.bounding(points, new RectangleLength2D_F32());
    }

    public Polygon2D_F32 getPolygon() {
        return polygon;
    }

    public RectangleLength2D_F32 getBoundingBox() {
        return boundingBox;
    }
}
