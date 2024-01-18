package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author ： 260611
 * @description ： CLIENT_AttachRadarRFIDCardInfo接口出参
 * @since ： Created in 2022/02/11 10:22
 */

public class NET_OUT_ATTACH_RADAR_RFIDCARD_INFO extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;

    public NET_OUT_ATTACH_RADAR_RFIDCARD_INFO() {
        this.dwSize = this.size();
    }
}
