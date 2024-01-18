package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  绝缘子检测结果
* @date 2022/06/28 19:44:56
*/
public class NET_INSULATOR_DETECT extends NetSDKLib.SdkStructure {
/**
绝缘子状态 {@link com.ood.device.dh.lib.enumeration.EM_INSULATOR_STATE}
*/
public			int					emInsulatorState;
/**
包围盒
*/
public NET_RECT stuBoundingBox=new NET_RECT();

public			NET_INSULATOR_DETECT(){
}
}
