package com.tygx.device.hik.core;


import com.ood.core.entity.ResultData;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

/**
 * 功能接口
 */
public interface IHikAccessDeviceClient extends IHikDevice {

	/**
	 * 是否初始化
	 * @return 布尔值
	 */
	boolean isInit();

    public void setEventFilePath(String eventFilePath);

    public void setCallBackUrl(String callBackUrl);

    public void setLogPath(String logPath);

    public ResultData init(String dllPath);

    /**
     * 设备登陆
     */
    public ResultData login(String username, String password, String ip, int port);

    /**
     * 设备注销
     */
    public ResultData logout(String uuid);

    /**
     * 查询卡号
     *
     * @param cardNo 卡号
     * @return 结果
     */
    public ResultData getOneCard(String uuid, String cardNo);

    /**
     * 获取所有人员卡信息
     *
     * @return 返回list
     */
    public ResultData getAllCard(String uuid, Integer pageSize, Integer pageNum);

    /**
     * 下发
     *
     * @param cardNo     卡号
     * @param cardName   人员名称
     * @param employeeNo 人员编号
     * @return 返回状态码
     */
    public ResultData setOneCard(String uuid, String cardNo, String cardName, int employeeNo, String userType, String beginTime, String endTime);

    /**
     * 下发人脸
     *
     * @param carNo 卡号
     * @param file  图片
     * @return 返回状态码
     */
    public ResultData setOneCardFace(String uuid, String carNo, File file);

    /**
     * 删除人脸
     *
     * @param cardNo 卡号
     * @return 结果
     */
    public ResultData delOneCardFace(String uuid, String cardNo);

    /**
     * 删除卡号
     *
     * @param cardNo 卡号
     * @return 结果
     */
    public ResultData delOneCard(String uuid, String cardNo);

    /**
     * 手动抓拍
     *
     * @param filePath 输出抓拍地址
     * @return 结果
     */
    public ResultData manualSnap(String uuid, String filePath);

	ResultData controlGateway(String uuid);

	/**
     * 门禁监控
     *
     * @param gatewayIndex 门禁序号（楼层编号、锁ID），从1开始，-1表示对所有门（或者梯控的所有楼层）进行操作
     * @param operateType  命令值：0- 关闭（对于梯控，表示受控），1- 打开（对于梯控，表示开门），2- 常开（对于梯控，表示自由、通道状态），3- 常关（对于梯控，表示禁用），4-
     *                     恢复（梯控，普通状态），5- 访客呼梯（梯控），6- 住户呼梯（梯控）
     * @return 结果
     */
    public ResultData controlGateway(String uuid, int gatewayIndex, int operateType);

    /**
     * 以人为中心 添加人
     *
     * @param employeeNo 人员工号
     * @param name       人员名称
     * @param beginTime  开始时间 eg：2017-08-01T17:30:08
     * @param userType   人员类型 员工normal（默认） 访客visitor
     */
    public ResultData addUser(String uuid, String employeeNo, String name, String userType, Date beginTime, Date endTime);

    /**
     * 修改人员信息
     *
     * @param employeeNo 人员编号
     * @param name       人员名称
     * @param userType   人员类型
     * @param beginTime  开始时间（门禁权限）
     */
    public ResultData modifyUser(String uuid, String employeeNo, String name, String userType, Date beginTime);

    /**
     * 以人为中心 删除人 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     * @return 结果
     */
    public ResultData delUser(String uuid, String employeeNo);

    /**
     * 以人为中心 删除人脸 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     */
    public ResultData delUserFace(String uuid, String employeeNo);

    /**
     * 以人为中心 添加人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   文件地址
     */
    public ResultData addUserFace(String uuid, String employeeNo, String filePath);

	ResultData addUserFace(String uuid, String employeeNo, FileInputStream file);

	/**
     * 以人为中心 查人员信息
     *
     * @param employeeNos 人员编号（可以为空）
     * @return 结果
     */
    public ResultData searchUser(String uuid, String[] employeeNos, Integer pageSize, Integer pageNum);

    /**
     * 获取事件 （人脸打卡）
     *
     * @param dateStr  日期 2020-01-01
     * @param dateType 日期类型 month day
     * @return 结果
     */
    public ResultData getEventRecord(String uuid, String dateStr, String dateType);

    /**
     * 查询人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   保存路径路径
     * @return 结果
     */
    public ResultData searchFaceInfo(String uuid, String employeeNo, String filePath);

    /**
     * 以人为中心 添加卡
     *
     * @param employeeNo 人员 ID
     * @param cardNo     卡号
     * @param isDeleteCard 是否删除该卡
     * @param cardType   卡类型 默认 normalCard-普通卡
     */
    public ResultData updateCard(String uuid, String employeeNo, String cardNo, boolean isDeleteCard, String cardType);

    public ResultData carManualSnap(String uuid, String filePath);

    /**
     * 撤防
     */
    public ResultData closeAlarmChan(String uuid);

    /**
     * 设防
     *
     * @param uuid 登陆id
     */
    public ResultData setupAlarmChan(String uuid);

	/**
	 * 清空人员
	 * @param uuid 登录信息
	 * @return 结果
	 */
	ResultData clearUser(String uuid);

	/**
	 * 远程重启设备
	 * @param uuid 登录信息
	 * @return 结果
	 */
	ResultData reboot(String uuid);


    ResultData generateLogsRegularly();
}
