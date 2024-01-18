package com.ood.device.dh.lib.enumeration;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 是否戴帽子
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 20:21
 */
public class EM_HAS_HAT extends NetSDKLib.SdkStructure {

    /**
     *  未知
     */
    public static final int					EM_HAS_HAT_UNKNOWN = 0;
    /**
     *  不戴帽子
     */
    public static final int					EM_HAS_HAT_NO = 1;
    /**
     *  戴帽子
     */
    public static final int					EM_HAS_HAT_YES = 2;
}
