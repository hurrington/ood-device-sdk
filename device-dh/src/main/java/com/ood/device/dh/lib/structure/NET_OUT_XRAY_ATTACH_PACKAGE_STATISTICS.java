package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 *@author ： 291189
 *@since ： Created in 2021/7/1
 * CLIENT_XRayAttachPackageStatistics 输出结构体
 */
public class NET_OUT_XRAY_ATTACH_PACKAGE_STATISTICS  extends NetSDKLib.SdkStructure {

    public int		dwSize;// 赋值为结构体大小

    public NET_OUT_XRAY_ATTACH_PACKAGE_STATISTICS(){
        this.dwSize=this.size();
    }
}
