package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @version 1.0
 * @description
 * @date 2022/7/19 11:37
 */
public class NET_EVENT_AREAR_INFO extends NetSDKLib.SdkStructure{
   public byte[]					szName=new byte[128];	// 所属区域名称
   public 	int 					nIndex;			// 所属区域编号
   public byte[]					szRsd=new byte[64];     // 保留字节
}
