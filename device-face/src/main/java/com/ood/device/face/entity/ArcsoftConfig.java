package com.ood.device.face.entity;

import lombok.Data;

/**
 * 虹软配置
 *
 * @author 西某川
 * @date 2024/03/27
 */
@Data
public class ArcsoftConfig implements IFaceEngineConfig{

    private String appId;
    private String sdkKey;
    private String libPath;
}
