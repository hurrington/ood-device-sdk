package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description CLIENT_GetRtscRunningMode 接口输入参数
 * @date 2021/09/28
 */
public class NET_IN_GET_RUNNING_MODE extends NetSDKLib.SdkStructure{
	/**
     *  结构体大小
     */
    public int dwSize;

    public NET_IN_GET_RUNNING_MODE(){
        this.dwSize = this.size();
    }
}
