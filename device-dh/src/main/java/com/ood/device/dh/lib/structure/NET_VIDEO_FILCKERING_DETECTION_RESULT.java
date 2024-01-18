package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  	视频闪频检测
* @date 2022/08/03 14:49:00
*/
public class NET_VIDEO_FILCKERING_DETECTION_RESULT extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;
/**
检测结果量化值
*/
public			int					nValue;
/**
检测结果状态 {@link NetSDKLib.NET_STATE_TYPE}
*/
public			int					emState;
/**
状态持续时间，单位:秒
*/
public			int					nDuration;

public NET_VIDEO_FILCKERING_DETECTION_RESULT(){
		this.dwSize=this.size();
}
}
