package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description CLIENT_SmokeRemoteReboot 出参
 * @date 2022/07/26 10:52:50
 */
public class NET_OUT_SMOKE_REMOTE_REBOOT_INFO extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;

    public NET_OUT_SMOKE_REMOTE_REBOOT_INFO() {
        this.dwSize = this.size();
    }
}
