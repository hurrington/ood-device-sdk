package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 自适应下载录像 按文件 {@link NetSDKLib#CLIENT_DownloadByFileSelfAdapt} 出参
 * 入参 {@link NET_IN_DOWNLOAD_BYFILE_SELFADAPT}
 *
 * @author 47040
 * @since Created in 2021/6/19 10:42
 */
public class NET_OUT_DOWNLOAD_BYFILE_SELFADAPT extends NetSDKLib.SdkStructure {

    /**
     * 结构体大小
     */
    public int dwSize;

    public NET_OUT_DOWNLOAD_BYFILE_SELFADAPT() {
        dwSize = this.size();
    }

}
