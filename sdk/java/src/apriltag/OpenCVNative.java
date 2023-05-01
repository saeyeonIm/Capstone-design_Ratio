package apriltag;

public class OpenCVNative {
    public static native void draw_polylines(long matAddrInput, double[] polygonVertexArr);

    public static native double[] find_camera_focal_center(double[] arr, int[] imageSize);

    public static native void draw_polylines_on_apriltag(long matAddrInput, double[] cameraMatrixData, double[] tagVertexArr, double[] polygonVertexArr);

    public static native void put_text(long matAddrInput, long matAddrOutput, int[] textPosArr);

    public static native CameraPosEstimation draw_and_estimate_camera_position(long matAddrInput, double[] cameraMatrixData, double[] tagVertexArr, double[] polygonVertexArr);

    public static native CameraPosEstimation estimate_camera_position(double[] cameraMatrixData, double[] tagVertexArr);
}
