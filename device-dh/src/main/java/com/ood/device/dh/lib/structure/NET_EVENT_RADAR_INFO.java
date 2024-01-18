package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  物体在雷达坐标系中的信息
* @date 2022/05/30 14:16:15
*/
public class NET_EVENT_RADAR_INFO extends NetSDKLib.SdkStructure {
/**
X轴坐标(横向距离)，单位：米
*/
public			float					fCoordinateX;
/**
Y轴坐标（纵向距离），单位：米
*/
public			float					fCoordinateY;
/**
预留字节
*/
public			byte[]					bReserved=new byte[24];

public			NET_EVENT_RADAR_INFO(){
}
}
