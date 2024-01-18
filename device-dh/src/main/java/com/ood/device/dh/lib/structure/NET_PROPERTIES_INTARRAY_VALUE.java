package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author 260611
 * @description int+Array值
 * @date 2022/04/20 11:31:59
 */
public class NET_PROPERTIES_INTARRAY_VALUE extends NetSDKLib.SdkStructure {
    /**
     * Value：1,2,3
     */
    public int nValue;
    /**
     * 预留字节
     */
    public byte[] szReserved = new byte[32];
}
