package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
CLIENT_SetTransmitTunnelParam 接口输出参数
*/
public class NET_OUT_SET_TRANSMITTUNNEL_PARAM extends NetSDKLib.SdkStructure {
/**
/<  结构体大小
*/
public			int					dwSize;

    public NET_OUT_SET_TRANSMITTUNNEL_PARAM(){
        this.dwSize=this.size();
    }
}
