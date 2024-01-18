package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
CLIENT_SetPersonInfoInputResult 输入参数
*/
public class NET_IN_PERSON_INFO_INPUT_RESULT extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;
/**
通道ID
*/
public			int					nChannelID;
/**
0 : 成功  1 : 人员信息不在人员库中
*/
public			int					nResult;
/**
字节对齐
*/
public			byte[]					byReserved=new byte[4];
/**
证件号码
*/
public			byte[]					szCitizenID=new byte[64];
/**
设备展示颜色（定制） com.ood.device.dh.lib.enumeration.EM_SHOW_COLOR
*/
public			int					emColor;
/**
0：实时采集的数据  1：离线采集的数据
*/
public			int					nDataType;

public NET_IN_PERSON_INFO_INPUT_RESULT(){
    this.dwSize=this.size();
}

}
