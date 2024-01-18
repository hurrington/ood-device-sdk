package com.ood.device.dh.lib.enumeration;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 是否虚检
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 19:35
 */
public class EM_IS_ERRORDETECT extends NetSDKLib.SdkStructure {

    /**
     *  未知
     */
    public static final int					EM_IS_ERRORDETECT_UNKNOWN = -1;
    /**
     *  无
     */
    public static final int					EM_IS_ERRORDETECT_NO = 0;
    /**
     *  有
     */
    public static final int					EM_IS_ERRORDETECT_YES = 1;
}
