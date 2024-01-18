package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description CLIENT_SetRtscGlobalParam 接口输入参数
 * @date 2022/05/23
 */
public class NET_IN_SET_IVSEVENT_PARSE_INFO extends NetSDKLib.SdkStructure{
	/**
     *  结构体大小
     */
    public int dwSize;

	/**
     *  智能分析事件类型,参考dhnetsdk.h中定义的智能分析事件宏.
     *  // 当前支持情况如下:
        // EVENT_IVS_FACERECOGNITION      dwStructType为 1 时,对应结构体 DEV_EVENT_FACERECOGNITION_INFO_V1
     *
     */
    public int dwIVSEvent;

	/**
     *  指定解析的类型,具体含义参考 dwIVSEvent 字段的说明
     */
    public int dwStructType;
}
