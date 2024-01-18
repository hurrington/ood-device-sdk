package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  CLIENT_SetExamRecordingPlans 接口出参
* @date 2022/05/16 20:42:53
*/
public class NET_OUT_SET_EXAM_RECORDING_PLANS extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;

public NET_OUT_SET_EXAM_RECORDING_PLANS(){
		this.dwSize=this.size();
}
}
