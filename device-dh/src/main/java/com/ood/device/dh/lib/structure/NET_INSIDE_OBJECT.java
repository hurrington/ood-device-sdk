package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 物品类型
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 09:19
 */
public class NET_INSIDE_OBJECT extends NetSDKLib.SdkStructure {

    /**
     *  物品危险等级
     */
    public int					emDangerGrade;

    /**
     *  物品类型
     */
    public int					emObjType;

    /**
     *  相似度
     */
    public int					nSimilarity;

    /**
     *  包围盒
     */
    public NetSDKLib.NET_RECT stuBoundingBox = new NetSDKLib.NET_RECT();

    /**
     *  保留字节,留待扩展
     */
    public byte					byReserved[] = new byte[108];

    @Override
    public String toString() {
        return "NET_INSIDE_OBJECT{" +
                "emDangerGrade=" + emDangerGrade +
                ", emObjType=" + emObjType +
                ", nSimilarity=" + nSimilarity +
                ", stuBoundingBox=" + stuBoundingBox.toString() +
                '}';
    }
}
