# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.
project("indoor-navigation-app")

get_filename_component(pathPROJECT "../../../.." ABSOLUTE)
set(pathOPENCV ${pathPROJECT}/sdk)
set(pathLIBOPENCV_JAVA ${pathOPENCV}/native/libs/${ANDROID_ABI}/libopencv_java4.so)

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathOPENCV}/native/jni/include)
include_directories(apriltag/)

add_library( # Sets the name of the library.
        opencv_native_lib
        SHARED
        ${pathPROJECT}/app/src/main/cpp/opencv_native_lib.cpp)

add_library(
        lib_opencv

        SHARED

        IMPORTED)

add_library( # Sets the name of the library.
        apriltag_native_lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        apriltag/apriltag.c
        apriltag/apriltag_quad_thresh.c
        apriltag/apriltag_math.h
        apriltag/common/g2d.c
        apriltag/common/getopt.c
        apriltag/common/homography.c
        apriltag/common/image_u8.c
        apriltag/common/image_u8x3.c
        apriltag/common/matd.c
        apriltag/common/pnm.c
        apriltag/common/string_util.c
        apriltag/common/svd22.c
        apriltag/common/time_util.c
        apriltag/common/unionfind.c
        apriltag/common/workerpool.c
        apriltag/common/zarray.c
        apriltag/common/zhash.c
        apriltag/common/zmaxheap.c
        apriltag/tag16h5.c
        apriltag/tag25h9.c
        apriltag/tag36h10.c
        apriltag/tag36h11.c
        apriltag/tagCircle21h7.c
        apriltag/tagCircle49h12.c
        apriltag/tagCustom48h12.c
        apriltag/tagStandard41h12.c
        apriltag/tagStandard52h13.c
        apriltag_native_lib.c)

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})

find_library( # Sets the name of the path variable.
        log-lib

        log)
find_library(jnigraphics-lib

        jnigraphics)


target_link_libraries( # Specifies the target library.
        opencv_native_lib
        lib_opencv
        ${log-lib}
        )

target_link_libraries(
        apriltag_native_lib
        ${jnigraphics-lib}
        ${log-lib}
        )