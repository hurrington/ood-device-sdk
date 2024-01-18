 package com.ood.device.dh.lib.structure;


import com.ood.device.dh.lib.NetSDKLib;

/**
 * @author ： 260611
 * @description ： 动物检测的大图信息
 * @since ： Created in 2021/11/05 14:46
 */

public class NET_ANIMAL_SCENE_IMAGE_INFO extends NetSDKLib.SdkStructure {
     /**
      * 在二进制数据块中的偏移
      */
     public			int					nOffSet;
     /**
      * 图片大小,单位字节
      */
     public			int					nLength;
     /**
      * 保留字节
      */
     public			byte[]					bReserved=new byte[256];
}
