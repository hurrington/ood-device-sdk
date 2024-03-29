package com.ood.device.hik.utils;

import com.ood.device.hik.core.HikDeviceFactory;
import com.ood.device.hik.core.IHikAccessDeviceClient;
import com.ood.device.hik.core.IHikWebcamClient;

/**
 * 海康设备
 *
 * @author 西某川
 * @date 2024/01/10
 */
public class HikDeviceUtil {

    /**
     * 门禁设备
     *
     * @return 门禁设备中心
     */
    public static IHikAccessDeviceClient accessDevice() {
        return HikDeviceFactory.getAccessControlClient();
    }

    /**
     * 网络摄像头，完善中
     *
     * @return 设备中心
     */
    public static IHikWebcamClient webcamDevice() {
        return HikDeviceFactory.getWebcamClient();
    }
}
