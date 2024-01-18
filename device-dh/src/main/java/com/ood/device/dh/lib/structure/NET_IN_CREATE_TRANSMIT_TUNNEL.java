package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.sun.jna.Pointer;

/**
 * CLIENT_CreateTransmitTunnel 接口输入参数
 */
public class NET_IN_CREATE_TRANSMIT_TUNNEL extends NetSDKLib.SdkStructure {
	/**
	 * /< 结构体大小
	 */
	public int dwSize;
	/**
	 * /< 代理类型 {@link com.ood.device.dh.lib.enumeration.EM_TRANSMIT_PROXY_TYPE }
	 */
	public int emProxyType;
	/**
	 * /< 代理模式 {@link com.ood.device.dh.lib.enumeration.EM_TRANSMIT_PROXY_MODE }
	 */
	public int emProxyMode;
	/**
	 * /< 当emProxyType为EM_TRANSMIT_PROXY_TYPE_GENERAL 是有效
	 */
	public int nPort;
	/**
	 * /< emProxyMode为EM_TRANSMIT_PROXY_MODE_CASCADE时有效，与Port一起，组成需级联代理的服务
	 */
	public int nChannel;
	/**
	 * /< 字节对齐
	 */
	public byte[] szReserved = new byte[4];
	/**
	 * /< 隧道超时2分钟断线回调
	 */
	public NetSDKLib.fTransmitDisConnectCallBack cbDisConnectCallBack;

	/**
	 * /< 隧道断线回调
	 */
	public NetSDKLib.fSubBizDisConnectCallBack cbDisConnectCallBackEx;
	/**
	 * /< 用户数据
	 */
	public Pointer dwUserData;

	public NET_IN_CREATE_TRANSMIT_TUNNEL() {
		this.dwSize = this.size();
	}
}
