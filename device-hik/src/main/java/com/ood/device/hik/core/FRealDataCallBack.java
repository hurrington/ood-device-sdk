package com.ood.device.hik.core;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * 视频播放回调流
 *
 * @author zsj
 * @date 2024/04/08
 */
public class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30{

    private PlayCtrl playControl;
    private IntByReference m_lPort;

    public FRealDataCallBack(PlayCtrl playControl,IntByReference m_lPort) {
        this.playControl = playControl;
        this.m_lPort = m_lPort;
    }

    // 预览回调
    public void invoke(
            int lRealHandle,
            int dwDataType,
            ByteByReference pBuffer,
            int dwBufSize,
            Pointer pUser) {
        // 播放库解码
        switch (dwDataType) {
            case HCNetSDK.NET_DVR_SYSHEAD: // 系统头
                if (!playControl.PlayM4_GetPort(m_lPort)) // 获取播放库未使用的通道号
                {
                    break;
                }
                if (dwBufSize > 0) {
                    if (!playControl.PlayM4_SetStreamOpenMode(
                            m_lPort.getValue(), PlayCtrl.STREAME_REALTIME)) // 设置实时流播放模式
                    {
                        break;
                    }
                    if (!playControl.PlayM4_OpenStream(
                            m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) // 打开流接口
                    {
                        break;
                    }
                    if (!playControl.PlayM4_Play(m_lPort.getValue(), null)) // 播放开始
                    {
                        break;
                    }
                }
            case HCNetSDK.NET_DVR_STREAMDATA: // 码流数据
                if ((dwBufSize > 0) && (m_lPort.getValue() != -1)) {
                    if (!playControl.PlayM4_InputData(
                            m_lPort.getValue(), pBuffer, dwBufSize)) // 输入流数据
                    {
                        break;
                    }
                }
        }
    }

}
