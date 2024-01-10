package com.tygx.device.hik.lib;

import com.sun.jna.NativeLong;
import com.tygx.device.hik.core.HCNetSDK;

/**
 * @author 251823
 * @description
 * @date 2022/06/17 11:14:34
 */
public class RECT extends HCNetSDK.SdkStructure {

	public NativeLong left;

	public NativeLong top;

	public NativeLong right;

	public NativeLong bottom;

	public RECT() {
	}
}
