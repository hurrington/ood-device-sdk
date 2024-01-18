package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @version 1.0
 * @description {@link NetSDKLib#CLIENT_SetChannelCfg}的出参
 * @date 2020/11/07
 */
public class NET_OUT_SET_CHANNEL_CFG extends NetSDKLib.SdkStructure {

	// 结构体大小
	public int dwSize;

	public NET_OUT_SET_CHANNEL_CFG() {
		this.dwSize = this.size();
	}

}
