package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  CLIENT_GetCustomInfoCaps 输入参数
* @date 2022/05/11 20:23:41
*/
public class NET_IN_GET_CUSTOMINFO_CAPS extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;

public NET_IN_GET_CUSTOMINFO_CAPS(){
		this.dwSize=this.size();
}
}
