package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 291189
 * @description 报警事件类型DH_ALARM_AUDIO_ANOMALY(音频异常事件)对应的数据描述信息
 * @date 2022/06/20 17:39:31
 */
public class ALARM_AUDIO_ANOMALY extends NetSDKLib.SdkStructure {
	/**
	 * 结构体大小
	 */
	public int dwStructSize;
	/**
	 * 事件动作, 0:Start, 1:Stop
	 */
	public int dwAction;
	/**
	 * 音频通道号
	 */
	public int dwChannelID;
	/**
	 * 声音强度
	 */
	public int nDecibel;
	/**
	 * 声音频率
	 */
	public int nFrequency;
    /**
     * 事件公共扩展字段结构体
     */
    public NET_EVENT_INFO_EXTEND   stuEventInfoEx = new NET_EVENT_INFO_EXTEND();

	public ALARM_AUDIO_ANOMALY() {
		this.dwStructSize = this.size();
	}
}
