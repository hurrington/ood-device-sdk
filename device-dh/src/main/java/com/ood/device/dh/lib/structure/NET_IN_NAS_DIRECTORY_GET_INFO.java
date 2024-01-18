package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  CLIENT_NASDirectoryGetInfo接口入参
* @date 2022/09/22 17:26:59
*/
public class NET_IN_NAS_DIRECTORY_GET_INFO extends NetSDKLib.SdkStructure {
/**
此结构体大小,必须赋值
*/
public			int					dwSize;

public NET_IN_NAS_DIRECTORY_GET_INFO(){
		this.dwSize=this.size();
}
}
