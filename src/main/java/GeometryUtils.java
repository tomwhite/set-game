import boofcv.alg.distort.RemovePerspectiveDistortion;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.PointIndex_I32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Polygon2D_F64;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.image.BufferedImage;
import java.util.List;

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
}
