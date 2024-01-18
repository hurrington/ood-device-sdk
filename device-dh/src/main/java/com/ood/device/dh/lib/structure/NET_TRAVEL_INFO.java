package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.enumeration.EM_TRAVEL_CODE_COLOR;

/**
 * @author 251823
 * @description  行程码信息
 * @date 2021/08/15
 */
public class NET_TRAVEL_INFO extends NetSDKLib.SdkStructure{
	 /**
     *  行程码状态，{@link EM_TRAVEL_CODE_COLOR}
     */
    public int emTravelCodeColor;

    /**
     *  最近14天经过的城市个数
     */
    public int nCityCount;

    /**
     *  最近14天经过的城市名. 按时间顺序排列, 最早经过的城市放第一个
     */
    public PassingCityByteArr[] szPassingCity = (PassingCityByteArr[])new PassingCityByteArr().toArray(16);

    /**
     *  保留字节
     */
    public byte[] szReserved = new byte[1024];

}
