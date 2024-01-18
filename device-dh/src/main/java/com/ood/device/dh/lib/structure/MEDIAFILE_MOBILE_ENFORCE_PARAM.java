package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.enumeration.EM_MOBILE_ENFORCE_UPLOAD_FLAG;

/**
 * 采集站和手持终端文件信息查询条件 入参
 * 接口 {@link NetSDKLib#CLIENT_FindFileEx}
 * 枚举 {@link NetSDKLib.EM_FILE_QUERY_TYPE#NET_FILE_QUERY_MOBILE_ENFORCE}
 *
 * @author 47040
 * @since Created at 2021/5/31 10:07
 */
public class MEDIAFILE_MOBILE_ENFORCE_PARAM extends NetSDKLib.SdkStructure {
    /**
     * 结构体大小
     */
    public int dwSize;
    /**
     * 通道号从 0 开始, -1 表示查询所有通道
     */
    public int nChannelID;
    /**
     * 开始时间
     */
    public NET_TIME stuBeginTime;
    /**
     * 结束时间
     */
    public NET_TIME stuEndTime;
    /**
     * 文件上传标志 枚举值 {@link EM_MOBILE_ENFORCE_UPLOAD_FLAG}
     */
    public int emUploadFlag;

    /**
     * 预留字节
     */
    public byte[] byReserved = new byte[65828];

    public MEDIAFILE_MOBILE_ENFORCE_PARAM() {
        dwSize = this.size();
    }
}
