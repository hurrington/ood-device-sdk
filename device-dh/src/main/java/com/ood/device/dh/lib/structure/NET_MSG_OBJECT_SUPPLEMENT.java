package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 检测到的物体补充字段
 * @author ： 251823
 * @since ： Created in 2021/11/07 15:22
 */
public class NET_MSG_OBJECT_SUPPLEMENT extends NetSDKLib.SdkStructure{
	/**
	 * 智能物体全局唯一物体标识
	 */
	public byte[] szObjectUUID = new byte[48];

	/**
	 * 预留字节
	 */
	public byte[] szReserved = new byte[256];
}
