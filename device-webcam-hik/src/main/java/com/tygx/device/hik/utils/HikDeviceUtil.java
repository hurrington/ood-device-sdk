package com.tygx.device.hik.utils;

import com.tygx.device.hik.core.HikDeviceFactory;
import com.tygx.device.hik.core.IHikAccessDeviceClient;
import com.tygx.device.hik.core.IHikWebcamClient;

/**
 * 海康设备
 *
 * @author dbg
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
