package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description  城市名字符串对应字节数组
 * @date 2021/08/15
 */
public class PassingCityByteArr extends NetSDKLib.SdkStructure{
	/**
	 *  城市名字符串对应字节数组
	 */
	public byte[] passingCityByteArr = new byte[128];

}
