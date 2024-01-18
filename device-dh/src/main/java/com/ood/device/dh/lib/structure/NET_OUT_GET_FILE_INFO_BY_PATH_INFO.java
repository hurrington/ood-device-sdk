package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  CLIENT_FileManagerGetExFileInfoByPath接口出参
* @date 2022/09/22 19:11:10
*/
public class NET_OUT_GET_FILE_INFO_BY_PATH_INFO extends NetSDKLib.SdkStructure {
/**
此结构体大小,必须赋值
*/
public			int					dwSize;
/**
剩余空间, 单位MB
*/
public NET_TIME_EX stuStartTime=new NET_TIME_EX();
/**
总空间, 单位MB
*/
public NET_TIME_EX stuEndTime=new NET_TIME_EX();
/**
文件上传状态 {@link com.ood.device.dh.lib.enumeration.EM_FILE_UPDATE_STATE}
*/
public			int					emState;
/**
文件上传进度
*/
public			int					nProgress;

public NET_OUT_GET_FILE_INFO_BY_PATH_INFO(){
		this.dwSize=this.size();
}
}
