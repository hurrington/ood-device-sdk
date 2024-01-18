package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description CLIENT_StartThingsHistoryData 输出参数
 * @date 2022/04/20 10:16:56
 */
public class NET_OUT_THINGS_START_HISTORYDATA extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小, 调用者必须初始化该字段
     */
    public int dwSize;
    /**
     * 符合查询条件的总数
     */
    public int dwCount;

    public NET_OUT_THINGS_START_HISTORYDATA() {
        this.dwSize = this.size();
    }
}
