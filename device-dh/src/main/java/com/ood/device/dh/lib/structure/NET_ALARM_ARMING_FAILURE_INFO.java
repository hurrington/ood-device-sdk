package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @version 1.0
 * @description
 * @date 2022/7/19 10:51
 */
public class NET_ALARM_ARMING_FAILURE_INFO extends NetSDKLib.SdkStructure{
  public    int						nAction;				// 事件动作 0:脉冲
  public 	int                     nChannel;				// 通道号
  public 	NET_TIME_EX				stuUTC=new NET_TIME_EX();// 事件发生的时间,标准的（不带时区偏差的）UTC时间
  /**
   * {@link com.ood.device.dh.lib.enumeration.EM_ARM_TYPE}
   */
  public    int emMode;				    // 布撤防模式
  public 	byte[]					szReserved=new byte[1024];      	// 保留字节

}
