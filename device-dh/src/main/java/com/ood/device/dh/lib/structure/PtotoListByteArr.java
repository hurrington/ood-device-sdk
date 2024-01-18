package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description  雷达控制盒支持的协议字符串对应字节数组
 * @date 2021/09/23
 */
public class PtotoListByteArr extends NetSDKLib.SdkStructure{
	/**
	 *  雷达控制盒支持的协议字符串对应字节数组
	 */
	public byte[] ptotoListByteArr = new byte[32];
}
