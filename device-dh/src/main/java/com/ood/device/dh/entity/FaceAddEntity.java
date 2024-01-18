package com.ood.device.dh.entity;

import lombok.Data;

@Data
public class FaceAddEntity {

    private String filePath;
    public String userId; // 用户ID
    public byte[] szFacePhotoData; // 图片数据，目前一个用户ID只支持添加一张

    public FaceAddEntity(String userId, String filePath) {
        this.filePath = filePath;
        this.userId = userId;
    }

    public FaceAddEntity() {
    }
}
