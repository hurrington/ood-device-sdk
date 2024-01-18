package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.NetSDKLib.SdkStructure;

/**
 *
 * @author 119178
 * CLIENT_AttachBackupTaskState接口输入参数
 * {@link NetSDKLib#CLIENT_AttachBackupTaskState}
 */
public class NET_OUT_ATTACH_BACKUP_STATE extends SdkStructure{
	public int                   dwSize;

	public NET_OUT_ATTACH_BACKUP_STATE(){
        this.dwSize = this.size();
    }
}
