package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
* @author 291189
* @description 消费相关策略,   对应配置项 NET_EM_CFG_CONSUMPTION_STRATEGY
* @date 2022/03/07 10:54:03
*/
public class NET_CFG_CONSUMPTION_STRATEGY_INFO extends NetSDKLib.SdkStructure {
/**
结构体大小
*/
public			int					dwSize;
/**
消费模式 {@link com.ood.device.dh.lib.enumeration.EM_NET_CONSUMPTION_MODE}
*/
public			int					emMode;
/**
离线消费使能, TRUE:支持离线消费 FALSE:不支持离线消费
*/
public			boolean					bOfflineEnable;
/**
同一个人消费间隔, 单位：秒，取值范围[1,600]
*/
public			int					nConsumptionInterval;

public NET_CFG_CONSUMPTION_STRATEGY_INFO(){
    this.dwSize=this.size();
}

    @Override
    public String toString() {
        return "NET_CFG_CONSUMPTION_STRATEGY_INFO{" +
                "dwSize=" + dwSize +
                ", emMode=" + emMode +
                ", bOfflineEnable=" + bOfflineEnable +
                ", nConsumptionInterval=" + nConsumptionInterval +
                '}';
    }
}
