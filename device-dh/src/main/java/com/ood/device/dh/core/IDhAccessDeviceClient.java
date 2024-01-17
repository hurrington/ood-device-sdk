package com.ood.device.dh.core;


import com.ood.core.entity.ResultData;

import java.io.Serializable;
import java.util.Date;

/**
 * 功能接口
 */
public interface IDhAccessDeviceClient extends Serializable {

    /**
     * 是否初始化
     *
     * @return 布尔值
     */
    boolean isInit();

    void setEventFilePath(String eventFilePath);

    void setCallBackUrl(String callBackUrl);

    void setLogPath(String logPath);

    /**
     * 初始化
     * @param dhnetsdkPath dll文件
     * @param dhconfigsdkPath dll文件
     * @return 初始化结果
     */
    ResultData init(String dhnetsdkPath, String dhconfigsdkPath);

    /**
     * 设备登陆
     */
    ResultData login(String username, String password, String ip, int port);

    /**
     * 设备注销
     */
    ResultData logout(String uuid);

    /**
     * 手动抓拍
     *
     * @param filePath 输出抓拍地址
     * @return 结果
     */
    ResultData manualSnap(String uuid, String filePath);

    /**
     * 门禁监控
     *
     * @return 结果
     */
    ResultData controlGateway(String uuid);

    /**
     * 以人为中心 添加人
     *
     * @param employeeNo 人员工号
     * @param name       人员名称
     * @param beginTime  开始时间 eg：2017-08-01T17:30:08
     * @param userType   人员类型
     */
    ResultData addUser(String uuid, String employeeNo, String name, String userType, Date beginTime, Date endTime);

    /**
     * 修改人员信息
     *
     * @param employeeNo 人员编号
     * @param name       人员名称
     * @param userType   人员类型
     * @param beginTime  开始时间（门禁权限）
     */
    ResultData modifyUser(String uuid, String employeeNo, String name, String userType, Date beginTime);

    /**
     * 以人为中心 删除人 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     * @return 结果
     */
    ResultData delUser(String uuid, String employeeNo);

    /**
     * 以人为中心 删除人脸 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     */
    ResultData delUserFace(String uuid, String employeeNo);

    /**
     * 以人为中心 添加人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   文件地址
     */
    ResultData addUserFace(String uuid, String employeeNo, String filePath);

    /**
     * 以人为中心 查人员信息
     *
     * @param employeeNos 人员编号（可以为空）
     * @return 结果
     */
    ResultData searchUser(String uuid, String[] employeeNos, Integer pageSize, Integer pageNum);

    /**
     * 获取事件 （人脸打卡）
     *
     * @param dateStr  日期 2020-01-01
     * @param dateType 日期类型 month day
     * @return 结果
     */
    ResultData getEventRecord(String uuid, String dateStr, String dateType);

    /**
     * 查询人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   保存路径路径
     * @return 结果
     */
    ResultData searchFaceInfo(String uuid, String employeeNo, String filePath);

    /**
     * 以人为中心 添加卡
     *
     * @param employeeNo   人员 ID
     * @param cardNo       卡号
     * @param isDeleteCard 是否删除该卡
     * @param cardType     卡类型 默认 normalCard-普通卡
     */
    ResultData updateCard(String uuid, String employeeNo, String cardNo, boolean isDeleteCard, String cardType);

    ResultData carManualSnap(String uuid, String filePath);

    /**
     * 撤防
     */
    ResultData closeAlarmChan(String uuid);

    /**
     * 设防
     *
     * @param uuid 登陆id
     */
    ResultData setupAlarmChan(String uuid);

    /**
     * 清空人员
     *
     * @param uuid 登录信息
     * @return 结果
     */
    ResultData clearUser(String uuid);

    /**
     * 远程重启设备
     *
     * @param uuid 登录信息
     * @return 结果
     */
    ResultData reboot(String uuid);

}
