package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description CLIENT_SetThingsConfig 接口输出参数
 * @date 2022/04/20 10:16:57
 */
public class NET_OUT_THINGS_SET extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小, 调用者必须初始化该字段
     */
    public int dwSize;

    public NET_OUT_THINGS_SET() {
        this.dwSize = this.size();
    }
}
