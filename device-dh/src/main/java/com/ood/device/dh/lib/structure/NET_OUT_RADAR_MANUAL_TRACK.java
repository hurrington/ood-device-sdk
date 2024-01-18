package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @version 1.0
 * @description CLIENT_RadarManualTrack接口出参
 * @date 2021/10/22 9:03
 */
public class NET_OUT_RADAR_MANUAL_TRACK extends NetSDKLib.SdkStructure {
	/** 结构体大小 */
	public int dwSize;

	/** 返回被跟踪轨迹对应的球机ip */
	public byte[] szSDIP = new byte[32];

	/**返回被跟踪目标的一个全局唯一id */
	public int nEventID;

	public NET_OUT_RADAR_MANUAL_TRACK() {
		this.dwSize = this.size();
	}

}
