package com.ood.device.hik.core;

import cn.hutool.log.Log;
import com.ood.core.entity.ResultData;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络监控
 *
 * @author zsj
 */
public class HikWebcamClient extends AbstractHikDevice implements IHikWebcamClient {

    private static final Log log = Log.get(HikWebcamClient.class);

    private static final HikWebcamClient singleton = new HikWebcamClient();
    static PlayCtrl playControl;
    static Map<String, Integer> lPlayMap = new HashMap<>();
    static IntByReference m_lPort = new IntByReference(-1);
    public Map<String, FRealDataCallBack> fRealDataCallBackMap = new HashMap<>(); // 报警回调函数实现
    /**
     * 日志路径
     */
    private String logPath;

    public HikWebcamClient() {
    }

    public static HikWebcamClient getInstance() {
        return singleton;
    }

    @Override
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * 是否初始化
     *
     * @return 布尔值
     */
    @Override
    public boolean isInit() {
        if (hCNetSDK != null) {
            return true;
        }
        return false;
    }

    /**
     * 初始化
     *
     * @param hcNetSDKDllPath    DLL文件目录
     * @param playControlDllPath DLL文件目录
     * @return 是否成功
     */
    @Override
    public ResultData init(String hcNetSDKDllPath, String playControlDllPath) {
        if (hCNetSDK != null) {
            // 避免重复初始化
            return ResultData.success();
        }
        log.info("海康设备hcNetSDKDll初始化，dll路径：" + hcNetSDKDllPath);
        log.info("海康设备playControlDll初始化，dll路径：" + playControlDllPath);
        hCNetSDK = Native.load(hcNetSDKDllPath, HCNetSDK.class);
        playControl = Native.load(playControlDllPath, PlayCtrl.class);
        boolean init = hCNetSDK.NET_DVR_Init();
        if (init && logPath != null) {
            generateLogsRegularly();
        }
        return init ? ResultData.success() : ResultData.error();
    }

    /**
     * 注销SDK
     *
     * @return 注销SDK
     */
    @Override
    public ResultData stop() {
        try {
            if (hCNetSDK != null) {
                hCNetSDK.NET_DVR_Cleanup();
            }
            if (playControl != null) {
                playControl.PlayM4_Stop(m_lPort.getValue());
            }
        } catch (Exception e) {
            log.error("注销SDK失败", e);
            return ResultData.error("注销SDK失败");
        }
        return ResultData.success();
    }

    /**
     * 开启预览
     *
     * @param uuid 登录ID
     * @return 结果
     */
    @Override
    public boolean realPlay(String uuid, boolean needCallBack) {

        // 开启预览
        int lPlay;
        if (needCallBack) {
            HCNetSDK.NET_DVR_PREVIEWINFO strClientInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
            strClientInfo.read();
            //		strClientInfo.hPlayWnd = null;  //窗口句柄，从回调取流不显示一般设置为空
            strClientInfo.lChannel = channelMap.get(uuid); // 通道号
            strClientInfo.dwStreamType = 1; // 0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
            strClientInfo.dwLinkMode =
                    0; // 连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输）
            // ，7- RTSP/HTTPS，8- NPQ
            strClientInfo.bBlocked = 1;
            strClientInfo.write();
            fRealDataCallBackMap.put(uuid, new FRealDataCallBack());
            lPlay =
                    hCNetSDK.NET_DVR_RealPlay_V40(
                            lUserIDMap.get(uuid),
                            strClientInfo,
                            fRealDataCallBackMap.get(uuid),
                            null);
        } else {
            HCNetSDK.NET_DVR_CLIENTINFO strClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();
            strClientInfo.lChannel = channelMap.get(uuid);
//            strClientInfo.lLinkMode ;
            strClientInfo.hPlayWnd = null;
//			strClientInfo.sMultiCastIP
            lPlay = hCNetSDK.NET_DVR_RealPlay(lUserIDMap.get(uuid), strClientInfo);
        }
        if (lPlay == -1) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.debug("取流失败" + iErr);
            return false;
        } else {
            lPlayMap.put(uuid, lPlay);
        }
        log.debug("取流成功");
        return true;
    }

    /**
     * 关闭预览
     *
     * @param uuid 登录ID
     * @return 结果
     */
    @Override
    public boolean stopRealPlay(String uuid) {
        return hCNetSDK.NET_DVR_StopRealPlay(lPlayMap.get(uuid));
    }

    /**
     * 保存实时录像
     *
     * @param uuid     登录ID
     * @param filePath 文件路径
     * @param duration 持续时间（秒）
     * @return 结果
     */
    @Override
    public ResultData saveRealData(String uuid, String filePath, int duration) {
        try {
            if (lPlayMap.get(uuid) == null) {
                realPlay(uuid, false);
            }
            hCNetSDK.NET_DVR_SaveRealData(lPlayMap.get(uuid), filePath);
            log.debug("保存录像，录像地址{}，时长{}", filePath, (Object) duration);
            Thread.sleep(1000L * duration);
        } catch (Exception e) {
            log.error("保存录像异常");
            return ResultData.error();
        } finally {
            hCNetSDK.NET_DVR_StopSaveRealData(lPlayMap.get(uuid));
        }
        return ResultData.success();
    }

    static class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {
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
}
