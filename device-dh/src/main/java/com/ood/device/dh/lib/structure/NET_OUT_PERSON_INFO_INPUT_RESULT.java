package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
CLIENT_SetPersonInfoInputResult 输出参数
*/
public class NET_OUT_PERSON_INFO_INPUT_RESULT extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;

public NET_OUT_PERSON_INFO_INPUT_RESULT(){
    this.dwSize=this.size();
}
}
