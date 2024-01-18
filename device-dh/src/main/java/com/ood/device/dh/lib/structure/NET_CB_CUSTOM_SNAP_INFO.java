package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;
import com.sun.jna.Pointer;

/**
* @author 291189
* @description 图片文件订阅回调信息
* @date 2022/03/31 14:04:19
*/
public class NET_CB_CUSTOM_SNAP_INFO extends NetSDKLib.SdkStructure {
/**
通道号
*/
public			int					nChannelID;
/**
抓图时间
*/
public NET_TIME stuSnapTime=new NET_TIME();
/**
自定义抓图类型 {@link com.ood.device.dh.lib.enumeration.EM_CUSTOM_SNAP_TYPE}
*/
public			int					emCustomSnapType;
/**
图片包含数据, 根据emCustomSnapType值来确定具体的结构体类型
*/
public Pointer pDetailInfo;
/**
保留字节
*/
public			byte[]					byReserve=new byte[1024];
}
