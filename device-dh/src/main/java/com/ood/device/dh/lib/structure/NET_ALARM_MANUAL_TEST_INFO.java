package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @description  手动测试上报事件 (对应 DH_ALARM_MANUAL_TEST)
 * @date 2022/07/25 15:32:25
 */
public class NET_ALARM_MANUAL_TEST_INFO extends NetSDKLib.SdkStructure {
	/**
	 事件动作 0:脉冲
	 */
	public			int					nAction;
	/**
	 通道号
	 */
	public			int					nChannel;
	/**
	 事件发生的时间,标准的（不带时区偏差的）UTC时间
	 */
	public			NET_TIME_EX					stuUTC=new NET_TIME_EX();
	/**
	 配件序列号
	 */
	public			byte[]					szSN=new byte[32];
	/**
	 配件名称
	 */
	public			byte[]					szName=new byte[32];
	/**
	 配件所属区域名称
	 */
	public			byte[]					szAreaName=new byte[128];
	/**
	 保留字节
	 */
	public			byte[]					szReserved=new byte[1024];

	public			NET_ALARM_MANUAL_TEST_INFO(){
	}
}
