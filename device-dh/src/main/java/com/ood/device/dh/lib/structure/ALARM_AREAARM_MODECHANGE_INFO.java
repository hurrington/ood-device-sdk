package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description  区域防区模式改变事件(对应事件 DH_ALARM_AREAARM_MODECHANGE)
* @date 2022/07/19 10:09:08
*/
public class ALARM_AREAARM_MODECHANGE_INFO extends NetSDKLib.SdkStructure {
/**
区域编号
*/
public			int					nAreaIndex;
/**
事件ID
*/
public			int					nEventID;
/**
事件发生的时间
*/
public NET_TIME_EX UTC=new NET_TIME_EX();
/**
操作方式 {@link com.ood.device.dh.lib.enumeration.EM_AREAARM_TRIGGERMODE}
*/
public			int					emTriggerMode;
/**
操作用户，仅emTriggerMode = EM_AREAARM_USER_USER时有效 {@link com.ood.device.dh.lib.enumeration.EM_AREAARM_USER}
*/
public			int					emUser;
/**
ID号,emTriggerMode = EM_AREAARM_TRIGGERMODE_KEYPAD
表示键盘地址;emTriggerMode =EM_AREAARM_TRIGGERMODE_REMOTECONTROL时表示遥控器编号
*/
public			int					nID;
/**
 布撤防状态 {@link com.ood.device.dh.lib.enumeration.EM_ARM_STATE}
*/
public			int					emArmState;
/**
保留扩展字节
*/
public			byte[]					byReserved=new byte[1024];

public ALARM_AREAARM_MODECHANGE_INFO(){
}
}
