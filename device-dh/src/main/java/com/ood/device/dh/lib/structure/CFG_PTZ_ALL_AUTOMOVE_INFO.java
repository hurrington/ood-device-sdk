package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib.SdkStructure;
import com.ood.device.dh.lib.constant.SDKStructureFieldLenth;

/**
 * @创建人 291189
 * @创建时间 2021/5/28
 * @描述
 */
public class CFG_PTZ_ALL_AUTOMOVE_INFO extends SdkStructure{

   /**
    *  配置信息
    */

   public CFG_PTZ_PER_AUTOMOVE_INFO[]  stPTZPerInfo =
           new CFG_PTZ_PER_AUTOMOVE_INFO[SDKStructureFieldLenth.MAX_CONFIG_NUM];
   /**
    * 获取到的配置个数
    */
   public   int	  nCfgNum;

   public CFG_PTZ_ALL_AUTOMOVE_INFO(){
      for(int i=0;i<stPTZPerInfo.length;i++){
         stPTZPerInfo[i]=new CFG_PTZ_PER_AUTOMOVE_INFO();
      }
   }
}
