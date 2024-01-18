package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description   RFID卡片信息
* @date 2022/09/01 20:07:33
*/
public class NET_RFID_CARD_INFO extends NetSDKLib.SdkStructure {
/**
RFID卡片ID
*/
public			byte[]					szCardId=new byte[24];
/**
预留字节
*/
public			byte[]					byReserved=new byte[128];

public NET_RFID_CARD_INFO(){
}
}
