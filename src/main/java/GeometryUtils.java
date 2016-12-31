import boofcv.struct.PointIndex_I32;

import java.util.List;

public class GeometryUtils {
    public static boolean isQuadrilateral(List<PointIndex_I32> vertexes) {
        return vertexes.size() == 4;
    }
}
