package apriltag;

import java.util.ArrayList;

/**
 * Interface to native C AprilTag library.
 */

public class ApriltagNative {

    public static native void apriltag_native_init();

    public static native void apriltag_init(String tagFamily, int errorBits, double decimateFactor,
                                            double blurSigma, int nthreads);

    public static native ArrayList<ApriltagDetection> apriltag_detect(byte[] src, int width, int height);
}
