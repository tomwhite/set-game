package com.tom_e_white.set_game;

import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.geometry.UtilPoint2D_F64;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Polygon2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.Rectangle2D_F64;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeometryUtils {
    public static boolean isQuadrilateral(List<PointIndex_I32> vertexes) {
        return vertexes.size() == 4;
    }

    public static Quadrilateral_F64 toQuadrilateral(List<PointIndex_I32> quadrilateral) {
      if (!isQuadrilateral(quadrilateral)) {
        throw new IllegalArgumentException("Not a quadrilateral: " + quadrilateral);
      }

      // Specify the corners in the input image of the region.
      // Order matters! top-left, top-right, bottom-right, bottom-left
      Point2D_I32 p0 = quadrilateral.get(0);
      Point2D_I32 p1 = quadrilateral.get(1);
      Point2D_I32 p2 = quadrilateral.get(2);
      Point2D_I32 p3 = quadrilateral.get(3);
      Polygon2D_F64 poly = new Polygon2D_F64(p0.getX(), p0.getY(), p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
      if (poly.isCCW()) {
        poly.flip();
      }
      Quadrilateral_F64 quad = new Quadrilateral_F64();
      UtilPolygons2D_F64.convert(poly, quad);

      return quad;
    }

    public static Planar<GrayF32> removePerspectiveDistortion(BufferedImage image, Quadrilateral_F64 quad, int targetWidth, int targetHeight) {

      // see http://boofcv.org/index.php?title=Example_Remove_Perspective_Distortion
      Planar<GrayF32> input2 = ConvertBufferedImage.convertFromMulti(image, null, true, GrayF32.class);

      RemovePerspectiveDistortion<Planar<GrayF32>> removePerspective =
              new RemovePerspectiveDistortion<>(targetWidth, targetHeight, ImageType.pl(3, GrayF32.class));

      double len1 = quad.getSideLength(0) + quad.getSideLength(2);
      double len2 = quad.getSideLength(1) + quad.getSideLength(3);
      Point2D_F64 corner0, corner1, corner2, corner3;
      if (targetWidth < targetHeight) {
          if (len1 > len2) {
              corner0 = quad.b;
              corner1 = quad.c;
              corner2 = quad.d;
              corner3 = quad.a;
          } else {
              corner0 = quad.a;
              corner1 = quad.b;
              corner2 = quad.c;
              corner3 = quad.d;
          }
      } else {
          if (len1 < len2) {
              corner0 = quad.b;
              corner1 = quad.c;
              corner2 = quad.d;
              corner3 = quad.a;
          } else {
              corner0 = quad.a;
              corner1 = quad.b;
              corner2 = quad.c;
              corner3 = quad.d;
          }
      }
  //    System.out.println(p0);
  //    System.out.println(p1);
  //    System.out.println(p2);
  //    System.out.println(p3);
      if(!removePerspective.apply(input2, corner0, corner1, corner2, corner3)){
        throw new RuntimeException("Failed!?!?");
      }

      return removePerspective.getOutput();
    }

    public static List<Quadrilateral_F64> sortRowWise(List<Quadrilateral_F64> quads) {
        List<Quadrilateral_F64> copy = new ArrayList<>(quads); // this will be mutated as we find rows
        List<Quadrilateral_F64> sorted = new ArrayList<>();

        while (!copy.isEmpty()) {
            // find centers so we can find quadrilateral that is closest to the origin
            List<Point2D_F64> centers = copy.stream().map(q -> UtilPolygons2D_F64.center(q, null)).collect(Collectors.toList());
            Optional<Point2D_F64> min = centers.stream().min(Comparator.comparingDouble(GeometryUtils::distance));
            int minIndex = -1;
            for (int i = 0; i < centers.size(); i++) {
                if (centers.get(i).equals(min.get())) {
                    minIndex = i;
                    break;
                }
            }
            if (minIndex == -1) {
                throw new IllegalArgumentException("Cannot find quadrilateral closest to origin.");
            }
            Quadrilateral_F64 firstInRow = copy.remove(minIndex);
            Rectangle2D_F64 bounds = new Rectangle2D_F64();
            UtilPolygons2D_F64.bounding(firstInRow, bounds);
            sorted.add(firstInRow);

            // find the rest of the quadrilaterals in the row - these are the ones whose centers are
            // between the first card’s min and max y, then sort by x coordinate
            List<Quadrilateral_F64> row = copy.stream().filter(q -> {
                Point2D_F64 center = UtilPolygons2D_F64.center(q, null);
                return bounds.getP0().getY() <= center.getY() && center.getY() <= bounds.getP1().getY();
            }).sorted(Comparator.comparingDouble(q -> UtilPolygons2D_F64.center(q, null).getX()))
                    .collect(Collectors.toList());
            copy.removeAll(row);
            sorted.addAll(row);
        }

        return sorted;
    }

    private static double distance(Point2D_F64 p) {
        return p.distance(0, 0);
    }
}
