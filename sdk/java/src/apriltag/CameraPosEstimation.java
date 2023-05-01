package apriltag;

public class CameraPosEstimation {
    // ID of the tag
    public int id;

    // 카메라 rotation
    public double[] rvecs = new double[3];

    // 카메라 translation
    public double[] tvecs = new double[3];

    // apriltag기준의 월드 좌표상에서의 카메라 좌표
    public double[] relativeCameraPos = new double[3];
}