package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @version 1.0
 * @description CLIENT_DeleteGroupFromVehicleRegisterDB 接口输出参数
 * @date 2021/8/17 14:16
 */
public class NET_OUT_DELETE_GROUP_FROM_VEHICLE_REG_DB  extends NetSDKLib.SdkStructure {
    public int                           		dwSize;							// 结构体大小
    public NET_OUT_DELETE_GROUP_FROM_VEHICLE_REG_DB(){
        this.dwSize=this.size();
    }
}
