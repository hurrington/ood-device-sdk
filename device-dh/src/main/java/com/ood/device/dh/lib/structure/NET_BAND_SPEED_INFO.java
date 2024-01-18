package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description 网卡信息
 * @date 2022/05/30 14:45:57
 */
public class NET_BAND_SPEED_INFO extends NetSDKLib.SdkStructure {
	/**
	 * 网卡名称
	 */
	public byte[] szEthName = new byte[16];
	/**
	 * 网卡接收速率，单位 Mb/s
	 */
	public double dbReceivedBytes;
	/**
	 * 网卡发送速率，单位 Mb/s
	 */
	public double dbTransmittedBytes;
	/**
	 * 保留字节
	 */
	public byte[] szResvered = new byte[512];

	public NET_BAND_SPEED_INFO() {
	}
}
