package com.ood.device.face.core;

import com.ood.core.entity.ResultData;
import com.ood.device.face.entity.IFaceEngineConfig;

import java.io.File;
import java.io.InputStream;

/**
 * 人脸
 *
 * @author 西某川
 * @date 2024/03/27
 */
public abstract class FaceEngineAbstractFactory implements IFaceEngine {
    @Override
    public String getType() {
        return "默认";
    }

    /**
     * 初始化
     *
     * @return 初始化
     */
    @Override
    public ResultData init() {
        return null;
    }

    /**
     * 引擎卸载
     *
     * @return 引擎卸载
     */
    @Override
    public ResultData unInit() {
        return null;
    }

    /**
     * 配置
     *
     * @return 配置
     */
    @Override
    public ResultData setConfig(IFaceEngineConfig faceEngineConfig) {
        return null;
    }

    /**
     * 人脸识别
     *
     * @param face
     * @return 人脸识别
     */
    @Override
    public ResultData detectFace(File face) {
        return null;
    }

    /**
     * 特征提取
     *
     * @return 特征提取
     */
    @Override
    public ResultData extractFace(File face) {
        return null;
    }

    /**
     * 特征比对
     *
     * @return 特征比对
     */
    @Override
    public ResultData compareFace(String faceId1, String faceId2) {
        return null;
    }

    @Override
    public ResultData detectFace(byte[] face) {
        return null;
    }

    @Override
    public ResultData detectFace(InputStream face) {
        return null;
    }

    @Override
    public ResultData extractFace(byte[] face) {
        return null;
    }

    @Override
    public ResultData extractFace(InputStream face) {
        return null;
    }

}
