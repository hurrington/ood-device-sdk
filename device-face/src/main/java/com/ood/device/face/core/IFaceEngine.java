package com.ood.device.face.core;

import com.ood.core.entity.ResultData;
import com.ood.device.face.entity.IFaceEngineConfig;

import java.io.File;

/**
 * 人脸
 *
 * @author dbg
 * @date 2024/03/27
 */
public interface IFaceEngine {


    String getType();

    /**
     * 初始化
     *
     * @return 初始化
     */
    ResultData init();

    /**
     * 引擎卸载
     *
     * @return 引擎卸载
     */
    ResultData unInit();

    /**
     * 配置
     *
     * @return 配置
     */
    ResultData setConfig(IFaceEngineConfig faceEngineConfig);

    /**
     * 人脸识别
     *
     * @param face
     * @return 人脸识别
     */
    ResultData detectFace(File face);

    /**
     * 特征提取
     *
     * @return 特征提取
     */
    ResultData extractFace(File face);

    /**
     * 特征比对
     *
     * @return 特征比对
     */
    ResultData compareFace(String faceId1, String faceId2);
}