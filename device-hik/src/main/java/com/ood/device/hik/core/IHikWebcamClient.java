package com.ood.device.hik.core;

import com.ood.core.entity.ResultData;

import java.util.HashMap;

/**
 * 网络监控
 *
 * @author zsj
 */
public interface IHikWebcamClient extends IHikDevice {

	void setLogPath(String logPath);

	/**
	 * 是否初始化
	 * @return 布尔值
	 */
	boolean isInit();

	/**
	 * 初始化
	 * @param hcNetSDKDllPath DLL文件目录
	 * @param playControlDllPath DLL文件目录
	 * @return 是否成功
	 */
	ResultData init(String hcNetSDKDllPath, String playControlDllPath);

	/**
	 * 注销SDK
	 *
	 * @return 注销SDK
	 */
	ResultData stop();

	/**
	 * 设备登陆
	 * @param username 用户名
	 * @param password 密码
	 * @param ip IP
	 * @param port 端口
	 * @return 登录ID
	 */
	ResultData login(String username, String password, String ip, int port);

	/**
	 * 设备注销
	 * @param uuid 登录ID
	 * @return 是否成功
	 */
	ResultData logout(String uuid);

	boolean realPlay(String uuid,boolean needCallBack);

	boolean stopRealPlay(String uuid);

	/**
	 * 保存实时录像
	 *
	 * @param uuid
	 * @param filePath 文件路径
	 * @param duration
	 * @return 结果
	 */
	HashMap<String, Object> saveRealData(String uuid, String filePath, int duration);
}
