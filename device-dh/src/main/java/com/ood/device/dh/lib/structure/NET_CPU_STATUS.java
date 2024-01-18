package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

import static com.ood.device.dh.lib.NetSDKLib.NET_MAX_CPU_NUM;

/**
 * className：NET_CPU_STATUS
 * description：
 * author：251589
 * createTime：2021/2/25 12:01
 *
 * @version v1.0
 */

public class NET_CPU_STATUS extends NetSDKLib.SdkStructure {
    /**
     * dwSize;
     */
    public int dwSize;
    /**
     *  查询是否成功
     */
    public int bEnable;

    /**
     *  CPU数量
     */
    public int nCount;

    /**
     *  CPU信息
     */
    public NET_CPU_INFO[] stuCPUs = (NET_CPU_INFO[]) new NET_CPU_INFO().toArray(NET_MAX_CPU_NUM);

    public NET_CPU_STATUS(){
        this.dwSize = this.size();
    }
}
