package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.NetSDKLib.SdkStructure;

/**
 *
 * @author 119178
 * CLIENT_StartBackupTaskSession 接口输出参数
 * {@link NetSDKLib#CLIENT_StartBackupTaskSession}
 */
public class NET_OUT_START_BACKUP_TASK_SESSION extends SdkStructure{

	public int               dwSize;

	public NET_OUT_START_BACKUP_TASK_SESSION(){
        this.dwSize = this.size();
    }
}
