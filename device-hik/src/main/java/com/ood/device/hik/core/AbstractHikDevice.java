package com.ood.device.hik.core;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.ood.core.entity.ResultData;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 海康设备虚拟类
 *
 * @author dbg
 * @date 2024/01/10
 */
public abstract class AbstractHikDevice implements IHikDevice {

    private static final Log log = Log.get(AbstractHikDevice.class);

    /**
     * 事件附件保存路径
     */
    @Setter
    @Getter
    private String eventFilePath;
    /**
     * 事件回调地址（url）
     */
    @Setter
    @Getter
    private String callBackUrl;
    /**
     * 日志路径
     */
    @Setter
    @Getter
    private String logPath;

    static HCNetSDK hCNetSDK;
    // 用户句柄
    static Map<String, Integer> lUserIDMap = new HashMap<>();
    // 设备字符集
    static Map<String, Byte> iCharEncodeTypeMap = new HashMap<>();
    // 设备通道
    static Map<String, Byte> channelMap = new HashMap<>();
    // 报警布防句柄集合
    static Map<String, Integer> lAlarmHandleMap = new HashMap<>();
    // 报警回调函数实现
    public Map<String, HikAccessDeviceClient.FMSGCallBack_V31> fMSFCallBack_V31Map = new HashMap<>();
    // 报警回调函数实现
    public Map<String, HikAccessDeviceClient.FMSGCallBack> fMSFCallBackMap = new HashMap<>();

    public ResultData login(String username, String password, String ip, int port) {
        if (hCNetSDK == null) {
            log.error("未初始化sdk");
            throw new RuntimeException("未初始化sdk");
        }

        String uuid = ip + "_" + port + "_" + username;

        // 判断是否已登陆
        Integer isLogin = lUserIDMap.get(uuid);
        if (isLogin != null && isLogin != -1) {
            log.trace(uuid + "设备已登录");
            return ResultData.success(uuid);
        }

        // 注册
        // 设备登录信息
        HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();

        loginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(ip.getBytes(), 0, loginInfo.sDeviceAddress, 0, ip.length());

        loginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(username.getBytes(), 0, loginInfo.sUserName, 0, username.length());

        loginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(password.getBytes(), 0, loginInfo.sPassword, 0, password.length());

        loginInfo.wPort = (short) port;
        // 是否异步登录：0- 否，1- 是
        loginInfo.bUseAsynLogin = false;
        loginInfo.write();

        // 设备信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        int lUserID = hCNetSDK.NET_DVR_Login_V40(loginInfo, deviceInfo);
        // 放入设备中心
        lUserIDMap.put(uuid, lUserID);

        if (lUserID == -1) {
            int i = hCNetSDK.NET_DVR_GetLastError();
            String msg = "";
            if (i == 1) {
                msg = "用户名密码错误。注册时输入的用户名或者密码错误。";
            } else if (i == 7) {
                msg = "设备无法连接。";
            }
            String errMsg = StrUtil.format("登录失败，错误码为{}。{}", i, msg);
            log.warn(errMsg);
            return ResultData.error(msg);
        } else {
            log.debug(uuid + "登录成功！");
            iCharEncodeTypeMap.put(uuid, deviceInfo.byCharEncodeType);
            channelMap.put(uuid, deviceInfo.struDeviceV30.byStartChan);
            return ResultData.success((Object) uuid);
        }
    }

    /**
     * 设备注销
     */
    public ResultData logout(String uuid) {
        try {
            // 退出程序时调用注销登录、反初始化接口
            hCNetSDK.NET_DVR_Logout(lUserIDMap.get(uuid));
            closeAlarmChan(uuid);
            lUserIDMap.remove(uuid);
            hCNetSDK.NET_DVR_Cleanup();
        } catch (Exception e) {
            log.error("设备注销失败：" + e.getMessage());
            int i = hCNetSDK.NET_DVR_GetLastError();
            String errMsg = StrUtil.format("登录失败，错误码为{}。", i);
            return ResultData.error(errMsg);
        }
        return ResultData.success();
    }

    /**
     * 撤防
     */
    public ResultData closeAlarmChan(String uuid) {

        // 报警撤防
        int lAlarmHandle;
        if (lAlarmHandleMap.get(uuid) == null) { // 没有布防
            return ResultData.success("该设备没有布防");
        } else { // 有布防
            lAlarmHandle = lAlarmHandleMap.get(uuid);
            // 取消布防
            if (!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle)) {
                return ResultData.error("撤防失败");
            } else {
                lAlarmHandleMap.remove(uuid); // 清空布防信息
                fMSFCallBack_V31Map.remove(uuid); // 清空布防回调
                fMSFCallBackMap.remove(uuid); // 清空布防回调
                return ResultData.success("撤防成功");
            }
        }
    }

    /**
     * 如果设置日志路径，生成SDK日志
     *
     * @return 结果
     */
    public ResultData generateLogsRegularly() {
        if (StrUtil.isNotBlank(logPath)) {
            // 打开日志，可选
            File path = new File(StrUtil.format("{}/hikSdkLog/log{}.log",
                    logPath, DateUtil.format(new Date(), "yyyyMMdd")));
            FileUtil.mkParentDirs(path);
            log.debug("生成日志：" + path);
            boolean b = hCNetSDK.NET_DVR_SetLogToFile(3, path.getAbsolutePath(), true);
            if (!b) {
                log.debug("设备开启日志失败！");
            }
            return ResultData.success();
        }
        return ResultData.error("开启日志失败");
    }
}
