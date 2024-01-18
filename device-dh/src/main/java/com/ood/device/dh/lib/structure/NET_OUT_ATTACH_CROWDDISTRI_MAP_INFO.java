package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @version 1.0
 * @description CLIENT_AttachCrowdDistriMap 接口输出参数
 * @date 2022/01/07
 */
public class NET_OUT_ATTACH_CROWDDISTRI_MAP_INFO extends NetSDKLib.SdkStructure{
    /**
     *  结构体大小
     */
    public int dwSize;

    public NET_OUT_ATTACH_CROWDDISTRI_MAP_INFO(){
        this.dwSize = this.size();
    }
}
