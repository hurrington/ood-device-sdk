package com.ood.device.face.utils;

import com.ood.device.face.constant.FaceEngineConstant;
import com.ood.device.face.core.ArcsoftFaceEngine;
import com.ood.device.face.core.IFaceEngine;

/**
 * 人脸识别工具
 *
 * @author 西某川
 * @date 2024/03/27
 */
public class FaceEngineUtil {
    public static IFaceEngine getFaceEngine(String type) {
        switch (type) {
            case FaceEngineConstant.TYPE_ARCSOFT:
                return ArcsoftFaceEngine.getInstance();
            case FaceEngineConstant.TYPE_BAIDU:
                return null;
            default:
                return null;
        }
    }
}
