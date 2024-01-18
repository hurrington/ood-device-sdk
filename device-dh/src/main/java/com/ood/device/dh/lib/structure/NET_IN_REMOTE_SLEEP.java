package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @version 1.0
 * @description CLIENT_RemoteSleep 输入接口参数
 * @date 2022/3/24 13:59
 */
public class NET_IN_REMOTE_SLEEP extends NetSDKLib.SdkStructure{
    public int dwSize;                    // 结构体大小
    public NET_IN_REMOTE_SLEEP(){
        this.dwSize=this.size();
    }
}
