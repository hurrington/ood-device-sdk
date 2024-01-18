package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description 扩展信息
 * @date 2022/10/31 11:10:11
 */
public class NET_MULTI_APPEND_EXTENDED_INFO extends NetSDKLib.SdkStructure {
	/**
	 * 该次人脸导入的token值
	 */
	public int nToken;
	/**
	 * 人脸导入状态 {@link com.ood.device.dh.lib.enumeration.EM_FACE_APPEND_STATE}
	 */
	public int emState;
	/**
	 * 保留字节
	 */
	public byte[] szResvered = new byte[248];

	public NET_MULTI_APPEND_EXTENDED_INFO() {
	}
}
