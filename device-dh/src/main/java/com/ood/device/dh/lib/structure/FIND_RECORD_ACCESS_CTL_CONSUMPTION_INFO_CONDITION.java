package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description 消费记录查询条件
* @date 2022/03/08 17:30:31
*/
public class FIND_RECORD_ACCESS_CTL_CONSUMPTION_INFO_CONDITION extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;
/**
启用用户查询
*/
public			int					bUserIDEnable;
/**
用户ID
*/
public			byte[]					szUserID=new byte[256];
/**
启用卡号查询
*/
public			int					bCardNoEnable;
/**
卡号
*/
public			byte[]					szCardNo=new byte[32];
/**
启用时间段查询
*/
public			int					bTimeEnable;
/**
起始时间
*/
public NET_TIME stStartTime=new NET_TIME();
/**
结束时间
*/
public NET_TIME stEndTime=new NET_TIME();

public FIND_RECORD_ACCESS_CTL_CONSUMPTION_INFO_CONDITION(){
this.dwSize=this.size();
}
}
