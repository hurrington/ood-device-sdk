package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description 雷达功率配置能力
 * @date 2022/08/04 10:13:31
 */
public class NET_RADAR_CAPACITY_CAP extends NetSDKLib.SdkStructure {
    /**
     * 是否支持该能力
     */
    public int bSupport;
    /**
     * 探测距离列表的有效数据个数
     */
    public int nListNum;
    /**
     * 配置时可选的探测距离列表
     */
    public int[] nDistanceList = new int[24];
    /**
     * 预留
     */
    public byte[] byReserved = new byte[224];

    public NET_RADAR_CAPACITY_CAP() {
    }

}
