#include "opencv2/opencv.hpp"
#include <jni.h>
#include <android/log.h>

#define SQUARE_LENGTH 6.8f // 태그의 사각형에서 사각형의 길이입니다.

using namespace cv;
using namespace std;

static struct {
    jclass camera_pos_esti_cls;
    jmethodID camera_pos_esti_constructor;
    jfieldID cpe_id_field, cpe_rvecs_field, cpe_tvecs_field, cpe_relative_camera_pos_field;
} state;


/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    draw_polylines
 * openCV polyline을 그립니다.
 */
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_draw_1polylines(JNIEnv *env, jclass clazz, jlong mat_addr_input,
                                           jdoubleArray polygon_vertex_arr) {
    Mat &matInput = *(Mat *) mat_addr_input;
    jdouble *vertices_arr = (*env).GetDoubleArrayElements(polygon_vertex_arr, nullptr);
    int len = (*env).GetArrayLength(polygon_vertex_arr);

// 정점에 따라 polyline을 그립니다.
    vector<Point2i> vector_pts;
    for (int i = 0; i < len; i++) {
        vector_pts.emplace_back(vertices_arr[2 * i], vertices_arr[2 * i + 1]);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 20);

    (*env).ReleaseDoubleArrayElements(polygon_vertex_arr, vertices_arr, 0);
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    find_camera_focal_length
 * Signature: cv::initCameraMatrix2D
 * 먼저 CameraPosEstimation 클래스를 전역 레퍼런스로 지정하고,
 * 카메라의 내부 파라미터를 구한 뒤, focal center(Cx, Cy)를 반환합니다.
 */
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_apriltag_OpenCVNative_find_1camera_1focal_1center(JNIEnv *env, jclass clazz,
                                                       jdoubleArray tag_corner_arr,
                                                       jintArray image_size) {
// Get CameraPosEstimation Class
    jclass apd_cls = (*env).FindClass("apriltag/CameraPosEstimation");

    if (!apd_cls) {
        __android_log_write(ANDROID_LOG_ERROR, "opencv_native_lib",
                            "couldn't find CameraPosEstimation class");
        return nullptr;
    }

    state.camera_pos_esti_cls = reinterpret_cast<jclass>((*env).NewGlobalRef(apd_cls));

    state.camera_pos_esti_constructor = (*env).GetMethodID(apd_cls, "<init>", "()V");
    if (!state.camera_pos_esti_constructor) {
        __android_log_write(ANDROID_LOG_ERROR, "opencv_native_lib",
                            "couldn't find CameraPosEstimation constructor");
        return nullptr;
    }

    state.cpe_id_field = (*env).GetFieldID(apd_cls, "id", "I");
    state.cpe_rvecs_field = (*env).GetFieldID(apd_cls, "rvecs", "[D");
    state.cpe_tvecs_field = (*env).GetFieldID(apd_cls, "tvecs", "[D");
    state.cpe_relative_camera_pos_field = (*env).GetFieldID(apd_cls, "relativeCameraPos", "[D");

    if (!state.cpe_id_field ||
        !state.cpe_rvecs_field ||
        !state.cpe_tvecs_field ||
        !state.cpe_relative_camera_pos_field) {
        __android_log_write(ANDROID_LOG_ERROR, "opencv_native_lib",
                            "couldn't find ApriltagPosDetection fields");
        return nullptr;
    }

    jdouble *tag_corner = (*env).GetDoubleArrayElements(tag_corner_arr, nullptr);
    jint *image_size_arr = (*env).GetIntArrayElements(image_size, nullptr);

// Creating vector to store vectors of 3D points for each apriltag image
    std::vector<std::vector<cv::Point3f> > obj_points;

// Creating vector to store vectors of 2D points for each apriltag image
    std::vector<std::vector<cv::Point2f> > img_points;

    vector<Point2f> image_point = {Point2f((float) tag_corner[0], (float) tag_corner[1]),
                                   Point2f((float) tag_corner[2], (float) tag_corner[3]),
                                   Point2f((float) tag_corner[4], (float) tag_corner[5]),
                                   Point2f((float) tag_corner[6], (float) tag_corner[7])};

    vector<Point3f> object_points = {Point3f(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                     Point3f(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                     Point3f(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0),
                                     Point3f(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0)};
    obj_points.push_back(object_points);
    img_points.push_back(image_point);

    Mat cameraM;
    cameraM = initCameraMatrix2D(obj_points, img_points,
                                 cv::Size(image_size_arr[0], image_size_arr[1]));

    (*env).ReleaseDoubleArrayElements(tag_corner_arr, tag_corner, 0);
    (*env).ReleaseIntArrayElements(image_size, image_size_arr, 0);

// Camera Matrix에서 Cx, Cy
    double focal_center[2] = {cameraM.at<double>(0, 2), cameraM.at<double>(1, 2)};
    jdoubleArray focal_center_arr = (*env).NewDoubleArray(2);
    (*env).SetDoubleArrayRegion(focal_center_arr, 0, 2, focal_center);

    return focal_center_arr;
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    draw_polylines_on_apriltag
 * Signature: cv::solvePnP, cv::projectPoints
 * apriltag 위에 openCV polyline을 그립니다.
 */
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_draw_1polylines_1on_1apriltag(JNIEnv *env, jclass clazz,
                                                         jlong mat_addr_input,
                                                         jdoubleArray camera_matrix_data,
                                                         jdoubleArray tag_corner_arr,
                                                         jdoubleArray polygon_vertex_arr) {
    Mat &matInput = *(Mat *) mat_addr_input;

    jdouble *tag_corner = (*env).GetDoubleArrayElements(tag_corner_arr, nullptr);

// Flattened to [fx, 0, cx, 0, fy, cy, 0, 0, 1] for JNI convenience
    jdouble *jni_cameraM_data = (*env).GetDoubleArrayElements(camera_matrix_data, nullptr);

// Flattened to [x0 y0 z0 x1 y1 z2 ...] for JNI convenience
    jdouble *polygon_vertex = (*env).GetDoubleArrayElements(polygon_vertex_arr, nullptr);

// 다각형의 정점의 개수
    int vertex_num = (*env).GetArrayLength(polygon_vertex_arr);

    Mat cameraM = Mat(3, 3, CV_64FC1, jni_cameraM_data);
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1); // 왜곡 계수

    vector<Point2f> image_point = {Point2f((float) tag_corner[0], (float) tag_corner[1]),
                                   Point2f((float) tag_corner[2], (float) tag_corner[3]),
                                   Point2f((float) tag_corner[4], (float) tag_corner[5]),
                                   Point2f((float) tag_corner[6], (float) tag_corner[7])};

    vector<Point3f> object_point = {Point3f(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0),
                                    Point3f(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0)};

// 카메라 rotation과 translation 벡터 찾기
    Mat rvecs, tvecs; // 카메라 rotation, translation
    solvePnP(object_point, image_point, cameraM, distortionC, rvecs, tvecs, false,
             SOLVEPNP_IPPE_SQUARE);

// 3D 포인터를 이미지 평면에 투영
    vector<cv::Point3f> obj_pts;
    for (int i = 0; i < vertex_num / 3; i++) {
        obj_pts.emplace_back(SQUARE_LENGTH * (float) polygon_vertex[3 * i],
                             SQUARE_LENGTH * (float) polygon_vertex[3 * i + 1],
                             SQUARE_LENGTH * (float) polygon_vertex[3 * i + 2]);
    }
    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, image_point);

    vector<Point2i> vector_pts;
// 이미지 평면에 투영 시킨 점들을 가지고 polyline을 그립니다.
    for (Point2f &i: image_point) {
        vector_pts.push_back(i);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);

    (*env).ReleaseDoubleArrayElements(tag_corner_arr, tag_corner, 0);
    (*env).ReleaseDoubleArrayElements(polygon_vertex_arr, polygon_vertex, 0);
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    put_text
 * Signature: cv::rotate
 * openCV를 이용해 text를 image 위에 띄웁니다.
 */
extern "C"
JNIEXPORT void JNICALL
Java_apriltag_OpenCVNative_put_1text(JNIEnv *env, jclass clazz, jlong mat_addr_input,
                                     jlong mat_addr_output, jintArray text_pos_arr) {
    Mat &matInput = *(Mat *) mat_addr_input;
    Mat &matResult = *(Mat *) mat_addr_output;
    jint *text_pos = (*env).GetIntArrayElements(text_pos_arr, nullptr);

    string text = "Hello, apriltag";
    rotate(matInput, matResult, ROTATE_90_CLOCKWISE);
    putText(matResult, text, Point(text_pos[0], text_pos[1]), FONT_HERSHEY_COMPLEX, 1,
            Scalar(0.0, 0.0, 255.0), 3);
    rotate(matResult, matInput, ROTATE_90_COUNTERCLOCKWISE);

    (*env).ReleaseIntArrayElements(text_pos_arr, text_pos, 0);
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    draw_and_estimate_camera_position
 * Signature: cv::solvePnP, cv::projectPoints, cv::polylines, cv::Rodrigues
 * apriltag 위에 openCV polyline을 그리고,
 * apriltag기준의 월드 좌표상에서의 카메라 좌표를 구하고 CameraPosEstimation 클래스를 반환합니다.
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_apriltag_OpenCVNative_draw_1and_1estimate_1camera_1position(JNIEnv *env, jclass clazz,
                                                                 jlong mat_addr_input,
                                                                 jdoubleArray camera_matrix_data,
                                                                 jdoubleArray tag_corner_arr,
                                                                 jdoubleArray polygon_vertex_arr) {
// Image data
    Mat &matInput = *(Mat *) mat_addr_input;

// The corners of the tag in image pixel coordinates. [x0, y0, x1, y1, ...] for JNI convenience
    jdouble *tag_corner = (*env).GetDoubleArrayElements(tag_corner_arr, nullptr);

// cameraMatrix : Flattened to [fx, 0, cx, 0, fy, cy, 0, 0, 1] for JNI convenience
    jdouble *jni_cameraM_data = (*env).GetDoubleArrayElements(camera_matrix_data, nullptr);

// polygonVertex : Flattened to [x0, y0, z0, x1, y1, z1, ...] for JNI convenience
    jdouble *polygon_vertex = (*env).GetDoubleArrayElements(polygon_vertex_arr, nullptr);

// 다각형의 정점의 개수
    int vertex_num = (*env).GetArrayLength(polygon_vertex_arr);

    Mat cameraM = Mat(3, 3, CV_64FC1, jni_cameraM_data);   // 카메라 내부 파라미터
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1);               // 왜곡 계수

    vector<Point2f> image_point = {Point2f((float) tag_corner[0], (float) tag_corner[1]),
                                   Point2f((float) tag_corner[2], (float) tag_corner[3]),
                                   Point2f((float) tag_corner[4], (float) tag_corner[5]),
                                   Point2f((float) tag_corner[6], (float) tag_corner[7])};

    vector<Point3f> object_point = {Point3f(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0),
                                    Point3f(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0)};

// 카메라 rotation과 translation 벡터 찾기
    Mat rvecs, tvecs; // 카메라 rotation, translation
    solvePnP(object_point, image_point, cameraM, distortionC, rvecs, tvecs, false,
             SOLVEPNP_IPPE_SQUARE);

// 3D 포인터를 이미지 평면에 투영
    vector<cv::Point3f> obj_pts;
    for (int i = 0; i < vertex_num / 3; i++) {
        obj_pts.emplace_back(SQUARE_LENGTH * (float) polygon_vertex[3 * i],
                             SQUARE_LENGTH * (float) polygon_vertex[3 * i + 1],
                             SQUARE_LENGTH * (float) polygon_vertex[3 * i + 2]);
    }

    projectPoints(obj_pts, rvecs, tvecs, cameraM, distortionC, image_point);

// 이미지 평면에 투영 시킨 점들을 가지고 polyline을 그립니다.
    vector<Point2i> vector_pts;
    for (Point2f &i: image_point) {
        vector_pts.push_back(i);
    }
    polylines(matInput, vector_pts, true, Scalar(255.0, 0.0, 0.0), 2);

    (*env).ReleaseDoubleArrayElements(tag_corner_arr, tag_corner, 0);
    (*env).ReleaseDoubleArrayElements(camera_matrix_data, jni_cameraM_data, 0);

// extract rotation & translation matrix
// solvePnP함수에서 반환되는 rvec matrix는 Rodrigues를 compact하게 표현한 vector이기에
// Rodrigues formula로 변환
    Mat rt;
    Rodrigues(rvecs, rt);
    Mat rt_inv = rt.inv();
    Mat camera_pos = -rt_inv * tvecs;

// camera_pos_estimation = new CameraPosEstimation();
    jobject camera_pos_estimation = (*env).NewObject(state.camera_pos_esti_cls,
                                                     state.camera_pos_esti_constructor);

    jdoubleArray cpe_rvecs = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_rvecs_field));
    (*env).SetDoubleArrayRegion(cpe_rvecs, 0, 3, (double *) rvecs.data);

    jdoubleArray cpe_tvecs = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_tvecs_field));
    (*env).SetDoubleArrayRegion(cpe_tvecs, 0, 3, (double *) tvecs.data);

    jdoubleArray cpe_pos = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_relative_camera_pos_field));
    (*env).SetDoubleArrayRegion(cpe_pos, 0, 3, (double *) camera_pos.data);

    return camera_pos_estimation;
}

/*
 * Class:     Java_apriltag_OpenCVNative
 * Method:    estimate_camera_position
 * Signature: cv::solvePnP, cv::Rodrigues
 * apriltag기준의 월드 좌표상에서의 카메라 좌표를 구하고 CameraPosEstimation 클래스를 반환합니다.
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_apriltag_OpenCVNative_estimate_1camera_1position(JNIEnv *env, jclass clazz,
                                                      jdoubleArray camera_matrix_data,
                                                      jdoubleArray tag_corner_arr) {
// The corners of the tag in image pixel coordinates. [x0 y0 x1 y1 ...] for JNI convenience
    jdouble *tag_corner = (*env).GetDoubleArrayElements(tag_corner_arr, nullptr);

// cameraMatrix element : [fx, 0, cx, 0, fy, cy, 0, 0, 1] for JNI convenience
    jdouble *jni_cameraM_data = (*env).GetDoubleArrayElements(camera_matrix_data, nullptr);

    Mat cameraM = Mat(3, 3, CV_64FC1, jni_cameraM_data);   // 카메라 내부 파라미터
    Mat distortionC = Mat::zeros(5, 1, CV_64FC1);          // 왜곡 계수

    vector<Point2f> image_point = {Point2f((float) tag_corner[0], (float) tag_corner[1]),
                                   Point2f((float) tag_corner[2], (float) tag_corner[3]),
                                   Point2f((float) tag_corner[4], (float) tag_corner[5]),
                                   Point2f((float) tag_corner[6], (float) tag_corner[7])};

    vector<Point3f> object_point = {Point3f(-SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, SQUARE_LENGTH / 2, 0),
                                    Point3f(SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0),
                                    Point3f(-SQUARE_LENGTH / 2, -SQUARE_LENGTH / 2, 0)};

// 카메라 rotation과 translation 벡터 찾기
    Mat rvecs, tvecs; // 카메라 rotation, translation
    solvePnP(object_point, image_point, cameraM, distortionC, rvecs, tvecs, false,
             SOLVEPNP_IPPE_SQUARE);

    (*env).ReleaseDoubleArrayElements(tag_corner_arr, tag_corner, 0);
    (*env).ReleaseDoubleArrayElements(camera_matrix_data, jni_cameraM_data, 0);

// extract rotation & translation matrix
// solvePnP함수에서 반환되는 rvec matrix는 Rodrigues를 compact하게 표현한 vector이기에
// Rodrigues formula로 변환
    Mat rt;
    Rodrigues(rvecs, rt);
    Mat rt_inv = rt.inv();
    Mat camera_pos = -rt_inv * tvecs;

// camera_pos_estimation = new CameraPosEstimation();
    jobject camera_pos_estimation = (*env).NewObject(state.camera_pos_esti_cls,
                                                     state.camera_pos_esti_constructor);

    jdoubleArray cpe_rvecs = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_rvecs_field));
    (*env).SetDoubleArrayRegion(cpe_rvecs, 0, 3, (double *) rvecs.data);

    jdoubleArray cpe_tvecs = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_tvecs_field));
    (*env).SetDoubleArrayRegion(cpe_tvecs, 0, 3, (double *) tvecs.data);

    jdoubleArray cpe_pos = reinterpret_cast<jdoubleArray>((*env).GetObjectField(
            camera_pos_estimation, state.cpe_relative_camera_pos_field));
    (*env).SetDoubleArrayRegion(cpe_pos, 0, 3, (double *) camera_pos.data);

    return camera_pos_estimation;
}