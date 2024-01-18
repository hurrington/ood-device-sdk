package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

//DEV_EVENT_FACERECOGNITION_INFO 对象的改版
public class NET_DEV_EVENT_FACERECOGNITION_INFO extends NetSDKLib.SdkStructure {
    public   int                 nChannelID;                                 // 通道号
    public byte[] 					szName = new byte[128];                                 // 事件名称
    public int                 nEventID;                                   // 事件ID
    public NetSDKLib.NET_TIME_EX UTC;                                        // 事件发生的时间
    public NetSDKLib.DH_MSG_OBJECT stuObject;                                  // 检测到的物体
    public int                bGlobalScenePic;                            // 全景图是否存在
    public NetSDKLib.NET_PIC_INFO stuGlobalScenePicInfo;                      // 全景图片信息
    public int					nRetCandidatesExNum;						// 当前人脸匹配到的候选对象数量实际返回值
    public NetSDKLib.CANDIDATE_INFOEX[] stuCandidatesEx=new NetSDKLib.CANDIDATE_INFOEX[3];      					// 当前人脸匹配到的候选对象信息扩展
}
