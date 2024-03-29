package com.ood.device.dh.utils;

import com.ood.device.dh.core.DhAccessDeviceFactory;
import com.ood.device.dh.core.IDhAccessDeviceClient;

/**
 * 大华工具
 *
 * @author 西某川
 * @date 2024/01/18
 */
public class DhDeviceUtil {

    /**
     * 门禁设备
     *
     * @return 门禁设备中心
     */
    public static IDhAccessDeviceClient accessDevice() {
        return DhAccessDeviceFactory.getAccessDeviceClient();
    }
}
