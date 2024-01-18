package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description 门禁定制提供客户的"系统设置"配置
 * @date 2022/06/02 10:15:21
 */
public class NET_CFG_CUSTOMER_DEV_SETTING_INFO extends NetSDKLib.SdkStructure {
	/**
	 * 结构体大小
	 */
	public int dwSize;
	/**
	 * 开门方式 {@link com.ood.device.dh.lib.enumeration.EM_CUSTOMER_DEV_SETTING_DOOR_METHOD}
	 */
	public int emDoorMethod;
	/**
	 * 国密加密算法密钥：加密密钥的密文及校验码，字符串
	 */
	public byte[] szEnckeycipher = new byte[1024];

	public NET_CFG_CUSTOMER_DEV_SETTING_INFO() {
		this.dwSize = this.size();
	}
}
