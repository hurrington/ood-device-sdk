package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 251823
 * @description 全局参数信息
 * @date 2021/09/28
 */
public class GLOBAL_INFO extends NetSDKLib.SdkStructure{
	 /**
     *  灯组故障等级。参考枚举{ @link EM_LIGHTGROUP_FAULT_LEVEL }
     */
    public int emLightGroupFault;

    /**
     *  倒计时方式。参考枚举{ @link EM_COUNTDOWN_TYPE }
     */
    public int emCountDownType;

    /**
     *  脉冲式倒计时脉冲宽度（单位ms）
     */
    public int nPulseWidth;

    /**
     *  脉冲式倒计时倒计时时间（单位s）
     */
    public int nPulseTime;

    /**
     *  485倒计时波特率 INTEGER(2400 4800 9600 19200 38400)
     */
    public int nBaudRault;

    /**
     *  485倒计时使用协议（0：GAT208-2004 1：GAT208-2014）
     */
    public int nGatType;

    /**
     *  半程485倒计时时间（单位秒）
     */
    public int nHalfTime;

    /**
     *  未配置相位运行模式1：全红 2：关灯 3：黄闪 4：红闪 -1不设置
     */
    public int nDefaultRunModel;

    /**
     *  可变车道通讯方式选择 0：供电式 1：通讯式 -1不设置
     */
    public int nVarLaneCommunicationType;

    /**
     *  自动维护使能：0-不启用，1-启用 -1不设置
     */
    public int nAutoProtectEnable;

    /**
     *  自动维护星期。参考枚举{ @link EM_AUTO_PROTECT_WEEK }
     */
    public int emAutoProtectWeek;

    /**
     *  自动维护小时
     */
    public int nAutoProtectHour;

    /**
     *  自动维护分钟
     */
    public int nAutoProtectMinute;

    /**
     *  通道自动解锁时间（0-65535）
     */
    public int nControlUnlockTime;

    /**
     *  故障是否推送(按位表示，置0表示开启上报，置1表示关闭上报.bit0：灯组故障bit1：检测器故障bit2：绿冲突故障bit3：外围板故障bit4:
     */
    public int nFaultReport;

    /**
     *  额定电压值
     */
    public int nRatedVoltage;

    /**
     *  百分比值个数
     */
    public int nPercentageValNum;

    /**
     *  百分比值;array[0]:百分比1（关机电压比）/ array[1]:百分比2（闪烁启动电压比）/array[2]:百分比3
     */
    public int[] nPercentageVal = new int[4];

    /**
     *  开机启动灯色个数
     */
    public int nStartUpLampNum;

    /**
     *  开机启动灯色 1：红灯 2：黄灯 4: 红闪 5: 黄闪 ; array[n]:阶段n灯色,n <3
     */
    public int[] nStartUpLamp = new int[3];

    /**
     *  开机启动时间个数
     */
    public int nStartUpTimeNum;

    /**
     *  开机启动时间 array[n]:阶段n时间,n <3
     */
    public int[] nStartUpTime = new int[3];

    /**
     *  预留字节
     */
    public byte[] byReserved = new byte[1024];
}
