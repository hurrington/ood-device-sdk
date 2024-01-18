package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.enumeration.EM_COMPLIANCE_STATE;
import com.ood.device.dh.lib.enumeration.EM_WEARING_STATE;

/**
 * @author ： 260611
 * @description ： 靴子相关属性状态信息
 * @since ： Created in 2022/03/10 11:17
 */

public class NET_BOOT_ATTRIBUTE extends NetSDKLib.SdkStructure {
    /**
     * 是否有穿靴子,{@link EM_WEARING_STATE}
     */
    public int emHasBoot;
    /**
     * 靴子检测结果,{@link EM_COMPLIANCE_STATE}
     */
    public int emHasLegalBoot;
}
