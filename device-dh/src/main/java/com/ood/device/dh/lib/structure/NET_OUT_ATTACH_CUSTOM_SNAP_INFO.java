package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description CLIENT_AttachCustomSnapInfo 接口输出参数
* @date 2022/03/31 11:35:21
*/
public class NET_OUT_ATTACH_CUSTOM_SNAP_INFO extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;

public NET_OUT_ATTACH_CUSTOM_SNAP_INFO(){
this.dwSize=this.size();
}
}
