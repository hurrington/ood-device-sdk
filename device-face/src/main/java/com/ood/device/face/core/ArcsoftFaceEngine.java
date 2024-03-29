package com.ood.device.face.core;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.toolkit.ImageInfo;
import com.ood.core.entity.ResultData;
import com.ood.device.face.entity.ArcsoftConfig;
import com.ood.device.face.entity.IFaceEngineConfig;

import java.io.File;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;

/**
 * 虹软人脸
 *
 * @author 西某川
 * @date 2024/03/27
 */
public class ArcsoftFaceEngine extends FaceEngineAbstractFactory {

    private FaceEngine faceEngine;
    private String appId;
    private String sdkKey;
    private String libPath;

    private EngineConfiguration engineConfiguration;

    private final HashMap<String, byte[]> faceFeatureMap = new HashMap<>();
    private static final ArcsoftFaceEngine singleton = new ArcsoftFaceEngine();

    public static ArcsoftFaceEngine getInstance() {
        return singleton;
    }

    private Object readResolve() throws ObjectStreamException {
        return singleton;
    }

    @Override
    public String getType() {
        //虹软
        return "arcsoft";
    }

    /**
     * 初始化
     *
     * @return 初始化
     */
    @Override
    public ResultData init() {
        if (StrUtil.hasBlank(appId, sdkKey, libPath) || engineConfiguration == null) {
            return ResultData.error("请先执行setConfig配置参数");
        }
        faceEngine = new FaceEngine(libPath);
        //激活引擎
        int errorCode = faceEngine.activeOnline(appId, sdkKey);

        if (errorCode != ErrorInfo.MOK.getValue() && errorCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            return ResultData.error("引擎激活失败");
        }
        errorCode = faceEngine.init(engineConfiguration);

        return ResultData.success(errorCode);
    }

    /**
     * 引擎卸载
     *
     * @return 引擎卸载
     */
    @Override
    public ResultData unInit() {
        int errorCode = faceEngine.unInit();
        return ResultData.success(errorCode);
    }

    /**
     * 配置
     *
     * @return 配置
     */
    @Override
    public ResultData setConfig(IFaceEngineConfig faceEngineConfig) {
        ArcsoftConfig arcsoftConfig = (ArcsoftConfig) faceEngineConfig;
        appId = arcsoftConfig.getAppId();
        sdkKey = arcsoftConfig.getSdkKey();
        libPath = arcsoftConfig.getLibPath();

        //引擎配置
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        engineConfiguration.setDetectFaceMaxNum(10);
        engineConfiguration.setDetectFaceScaleVal(16);
        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);

        return ResultData.success();
    }

    /**
     * 人脸识别
     *
     * @param face 人脸
     * @return 人脸识别
     */
    @Override
    public ResultData detectFace(File face) {
        ImageInfo imageInfo = getRGBData(face);
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        int errorCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
        Log.get().debug("人脸识别结果：{}", errorCode);
        return ResultData.success(faceInfoList);
    }

    /**
     * 特征提取
     *
     * @return 特征提取
     */
    @Override
    public ResultData extractFace(File face) {
        ImageInfo imageInfo = getRGBData(face);
        FaceFeature faceFeature = new FaceFeature();
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        int detectFacesFlag = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList);
        Log.get().debug("人脸识别结果：{}", detectFacesFlag);
        int extractFaceFeatureFlag = faceEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getImageFormat(), faceInfoList.get(0), faceFeature);
        Log.get().debug("特征值获取结果：{}", extractFaceFeatureFlag);
        Log.get().debug("特征值大小：{}", faceFeature.getFeatureData().length);
        if (ObjectUtil.isNotEmpty(faceFeature.getFeatureData())) {
            String faceId = IdUtil.nanoId();
            faceFeatureMap.put(faceId, faceFeature.getFeatureData());
            return ResultData.success((Object) faceId);
        }
        return ResultData.error("特征提取失败");
    }

    /**
     * 特征比对
     *
     * @return 特征比对
     */
    @Override
    public ResultData compareFace(String faceId1, String faceId2) {
        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(faceFeatureMap.get(faceId1));
        FaceFeature sourceFaceFeature = new FaceFeature();
        sourceFaceFeature.setFeatureData(faceFeatureMap.get(faceId2));
        FaceSimilar faceSimilar = new FaceSimilar();
        int compareFaceFeatureFlag = faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
        Log.get().debug("特征比对结果：{}", compareFaceFeatureFlag);
        Log.get().debug("相似度：{}", faceSimilar.getScore());
        return ResultData.success(faceSimilar.getScore());
    }
}
