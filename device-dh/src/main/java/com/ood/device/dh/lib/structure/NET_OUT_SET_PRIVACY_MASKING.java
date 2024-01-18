package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description CLIENT_SetPrivacyMasking 输出参数
 * @date 2022/07/21 17:12:29
 */
public class NET_OUT_SET_PRIVACY_MASKING extends NetSDKLib.SdkStructure {
	/**
	 * 结构体大小
	 */
	public int dwSize;

	public NET_OUT_SET_PRIVACY_MASKING() {
		this.dwSize = this.size();
	}
}
