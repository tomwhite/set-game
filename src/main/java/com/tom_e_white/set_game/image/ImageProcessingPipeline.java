package com.tom_e_white.set_game.image;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F32;
import georegression.struct.shapes.Polygon2D_F32;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.RectangleLength2D_F32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageProcessingPipeline {

    private final BufferedImage original;

    private final ListDisplayPanel panel;

    private ImageProcessingPipeline(BufferedImage original, ListDisplayPanel panel) {
        this.original = original;
        this.panel = panel;
        if (panel != null) {
            this.panel.addImage(original, "Original");
        }
    }

    public static BufferedImageProcessor fromBufferedImage(BufferedImage original, ListDisplayPanel panel) {
        return new ImageProcessingPipeline(original, panel).new BufferedImageProcessor();
    }

    public class BufferedImageProcessor {
        public GrayImageProcessor gray() {
            GrayU8 image = ConvertBufferedImage.convertFromSingle(original, null, GrayU8.class);
            addImageToPanel(image, "Gray");
            return new GrayImageProcessor(image);
        }
    }

    public class GrayImageProcessor {
        private final GrayU8 image;

        public GrayImageProcessor(GrayU8 image) {
            this.image = image;
        }

        public GrayImageProcessor medianBlur(int radius) {
            GrayU8 newImage = ImageUtils.medianBlur(image, radius);
            addImageToPanel(newImage, String.format("Median blur (radius %d)", radius));
            return new GrayImageProcessor(newImage);
        }

        public GrayImageProcessor binarize(int minValue, int maxValue) {
            return binarize(minValue, maxValue, false);
        }

        public GrayImageProcessor binarize(int minValue, int maxValue, boolean down) {
            GrayU8 newImage = ImageUtils.binarize(image, minValue, maxValue, down);
            addImageToPanel(newImage, String.format("Binarize (min %d, max %d, down %s)", minValue, maxValue, down));
            return new GrayImageProcessor(newImage);
        }

        public GrayImageProcessor edges() {
            GrayU8 newImage = ImageUtils.edges(image);
            addImageToPanel(newImage, String.format("Edges"));
            return new GrayImageProcessor(newImage);
        }

        public GrayImageProcessor erode() {
            GrayU8 newImage = BinaryImageOps.erode8(image, 1, null);
            addImageToPanel(newImage, String.format("Erode"));
            return new GrayImageProcessor(newImage);
        }

        public GrayImageProcessor dilate() {
            GrayU8 newImage = BinaryImageOps.dilate8(image, 1, null);
            addImageToPanel(newImage, String.format("Dilate"));
            return new GrayImageProcessor(newImage);
        }

        public ContoursProcessor contours() {
            // Detect blobs inside the image using an 8-connect rule
            GrayS32 label = new GrayS32(image.width, image.height);
            List<Contour> contours = BinaryImageOps.contour(image, ConnectRule.EIGHT, label);
            if (panel != null) {
                panel.addImage(VisualizeBinaryData.renderLabeledBG(label, contours.size(), null),
                        String.format("Labelled contours (n=%d)", contours.size()));
            }
            return new ContoursProcessor(contours, image.width, image.height);
        }

    }

    public class ContoursProcessor {
        private final List<Contour> contours;
        private final int width;
        private final int height;

        public ContoursProcessor(List<Contour> contours, int width, int height) {
            this.contours = contours;
            this.width = width;
            this.height = height;
        }

        public ContourPolygonsProcessor polygons(double splitFraction, double minimumSideFraction) {
            List<List<PointIndex_I32>> externalContours = GeometryUtils.getExternalContours(contours, splitFraction, minimumSideFraction);
            List<List<PointIndex_I32>> internalContours = GeometryUtils.getInternalContours(contours, splitFraction, minimumSideFraction);

            if (panel != null) {
                BufferedImage polygon = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                // Fit a polygon to each shape and draw the results
                Graphics2D g2 = polygon.createGraphics();
                g2.setStroke(new BasicStroke(2));

                for (List<PointIndex_I32> vertexes : externalContours) {
                    g2.setColor(Color.RED);
                    VisualizeShapes.drawPolygon(vertexes, true, g2);
                }
                for (List<PointIndex_I32> vertexes : internalContours) {
                    // handle internal contours now
                    g2.setColor(Color.BLUE);
                    VisualizeShapes.drawPolygon(vertexes, true, g2);
                }
                panel.addImage(polygon, "External (red) and internal (blue) contours");
            }

            return new ContourPolygonsProcessor(externalContours, internalContours);
        }


    }

    public class ContourPolygonsProcessor {
        private List<List<PointIndex_I32>> externalContours;
        private List<List<PointIndex_I32>> internalContours;

        public ContourPolygonsProcessor(List<List<PointIndex_I32>> externalContours, List<List<PointIndex_I32>> internalContours) {
            this.externalContours = externalContours;
            this.internalContours = internalContours;
        }

        public List<List<PointIndex_I32>> getExternalContours() {
            return externalContours;
        }

        public List<List<PointIndex_I32>> getInternalContours() {
            return internalContours;
        }

        public List<Quadrilateral_F64> getExternalQuadrilaterals() {
            return externalContours.stream()
                    .filter(GeometryUtils::isQuadrilateral)
                    .map(GeometryUtils::toQuadrilateral)
                    .collect(Collectors.toList());
        }

        public List<Polygon2D_F32> getExternalPolygons() {
            return getExternalShapes().stream().map(Shape::getPolygon).collect(Collectors.toList());
        }

        public List<RectangleLength2D_F32> getExternalBoundingBoxes() {
            return getExternalShapes().stream().map(Shape::getBoundingBox).collect(Collectors.toList());
        }

        public List<Shape> getExternalShapes() {
            return externalContours.stream()
                    .filter(c -> c.size() > 1)
                    .map(c -> {
                        List<Point2D_F32> points = new ArrayList<>();
                        for (PointIndex_I32 p : c) {
                            points.add(new Point2D_F32(p.x, p.y));
                        }
                        return new Shape(points);
                    })
                    .collect(Collectors.toList());
        }
    }

    private void addImageToPanel(GrayU8 image, String name) {
        if (panel != null) {
            panel.addImage(VisualizeBinaryData.renderBinary(image, false, null), name);
        }
    }
}
