package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 是否与其他目标关联
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 19:35
 */
public class HUMAN_ATTRIBUTES_INFO_EX extends NetSDKLib.SdkStructure {

    /**
     *  是否戴帽子
     */
    public int					emHasRainCoat;

    /**
     *  口罩颜色
     */
    public int					emMaskColor;

    /**
     *  人体优选分数
     */
    public int					nQeScore;
    /**
     *  目标完整性，0-完整，非0-不完整，视频流时值有效。
     */
    public int					nIntegrality;
    /**
     *  是否与其他目标关联
     */
    public int					emExtRecordSource;

    /**
     *  预留字节
     */
    public byte					byReserved[] = new byte[1024];

}
