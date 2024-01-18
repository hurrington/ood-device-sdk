package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;
import com.sun.jna.NativeLong;

/**
* @author 251823
* @description 区域；各边距按整长8192的比例
* @date 2022/07/21 15:15:27
*/
public class DH_RECT_REGION extends NetSDKLib.SdkStructure {
    public NativeLong  	left;
    public NativeLong  	top;
    public NativeLong  	right;
    public NativeLong  	bottom;
}
