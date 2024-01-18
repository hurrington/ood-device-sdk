package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description 获取雷达状态出参(对应 EM_RADAR_OPERATE_TYPE_GETSTATUS)
 * @date 2022/08/04 10:13:32
 */
public class NET_OUT_RADAR_GETSTATUS extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;
    /**
     * 雷达状态 {@link com.ood.device.dh.lib.enumeration.EM_RADAR_STATUS}
     */
    public int emRadarStatus;

    public NET_OUT_RADAR_GETSTATUS() {
        this.dwSize = this.size();
    }

}
