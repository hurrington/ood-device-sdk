package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib.SdkStructure;

public class NET_BACKUP_STATES_INFO extends SdkStructure{
	public int        nProgress;						// 备份进度
	public int        emState;						// 备份状态值(EM_BACKUP_STATES)
	public byte[]	  szDeviceName = new byte[64];               // 备份的设备名称
	public byte[]	  byReserved= new byte[256];				// 保留字节
}
