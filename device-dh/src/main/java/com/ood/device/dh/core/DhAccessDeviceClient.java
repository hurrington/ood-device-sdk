package com.ood.device.dh.core;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.ood.core.entity.ResultData;
import com.ood.device.dh.entity.CardAddEntity;
import com.ood.device.dh.entity.PersonAddEntitiy;
import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.NetSDKLib.*;
import com.ood.device.dh.lib.ToolKits;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DhAccessDeviceClient implements IDhAccessDeviceClient {
    private static final Log log = Log.get(DhAccessDeviceClient.class);

    private static final Map<String, LLong> loginHandleMap = new HashMap<>(); // 登陆句柄
    private static final Map<String, NET_DEVICEINFO_Ex> deviceinfoMap = new HashMap<>();
    private static final int TIME_OUT = 6 * 1000;
    private static final DhAccessDeviceClient singleton = new DhAccessDeviceClient();
    public static NetSDKLib netsdkApi;
    public static NetSDKLib configapi;
    //断连事件
    private final fDisConnectCB m_DisConnectCB = new fDisConnectCB();
    private final HaveReConnect haveReConnect = new HaveReConnect();
    String snapPicturePath;
    // 事件附件保存路径
    @Setter
    @Getter
    private String eventFilePath;
    //事件回调地址（url）
    @Setter
    @Getter
    private String callBackUrl;
    //日志路径
    @Setter
    @Getter
    private String logPath;

    public DhAccessDeviceClient() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            log.info("大华windows服务启动");
        } else {
            log.info("大华linux服务启动");
        }
    }

    public static DhAccessDeviceClient getInstance() {
        return singleton;
    }


    /**
     * 是否初始化
     *
     * @return 布尔值
     */
    @Override
    public boolean isInit() {
        if (netsdkApi != null) {
            return true;
        }
        return false;
    }

    @Override
    public void setEventFilePath(String eventFilePath) {
        this.eventFilePath = eventFilePath;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    @Override
    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    //反序列化定义该方法，则不需要创建新对象
    private Object readResolve() throws ObjectStreamException {
        return singleton;
    }

    @Override
    public ResultData init(String dhnetsdkPath, String dhconfigsdkPath) {
        if (netsdkApi != null) {
            // 避免重复初始化
            return ResultData.success();
        }
        log.debug("大华设备sdk初始化，dll路径：{}，{}" + dhnetsdkPath, dhconfigsdkPath);
        netsdkApi = Native.load(dhnetsdkPath, NetSDKLib.class);
        configapi = Native.load(dhconfigsdkPath, NetSDKLib.class);

        boolean init = netsdkApi.CLIENT_Init(m_DisConnectCB, null);
        if (init && logPath != null) {
            generateLogsRegularly();
        }
        return ResultData.success(init);
    }

    /**
     * 如果设置日志路径，定时每日生成日志
     */
    public void generateLogsRegularly() {
        if (StrUtil.isNotBlank(logPath)) {
            // 打开日志，可选
            LOG_SET_PRINT_INFO setLog = new LOG_SET_PRINT_INFO();
            File path = new File(logPath + "/dhSdkLog/log" + DateUtil.format(new Date(), "yyyyMMdd") + ".log");
            FileUtil.mkParentDirs(path);
            log.debug("生成日志：" + path);
            setLog.bSetFilePath = 1;
            System.arraycopy(
                    path.getPath().getBytes(),
                    0,
                    setLog.szLogFilePath,
                    0,
                    path.getPath().getBytes().length);
            setLog.bSetPrintStrategy = 1;
            setLog.nPrintStrategy = 0;
            boolean bLogopen = netsdkApi.CLIENT_LogOpen(setLog);
            if (!bLogopen) {
                log.debug("设备开启日志失败！");
            }
        }
    }

    @Override
    public ResultData login(String username, String password, String ip, int port) {
        Assert.notNull(netsdkApi, "未初始化sdk");
        String uuid = ip + "_" + port + "_" + username;

        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
        // 此操作为可选操作，但建议用户进行设置
        netsdkApi.CLIENT_SetAutoReconnect(haveReConnect, null);

        // 设置登录超时时间和尝试次数，可选
        int waitTime = 5000; // 登录请求响应超时时间设置为5S
        int tryTimes = 3; // 登录时尝试建立链接3次
        netsdkApi.CLIENT_SetConnectTime(waitTime, tryTimes);

        // 设置更多网络参数，NET_PARAM的nWaittime，nConnectTryNum成员与CLIENT_SetConnectTime
        // 接口设置的登录设备超时时间和尝试次数意义相同,可选
        NET_PARAM netParam = new NET_PARAM();
        netParam.nConnectTime = 10000; // 登录时尝试建立链接的超时时间

        netsdkApi.CLIENT_SetNetworkParam(netParam);

        // 向设备登入
        deviceinfoMap.put(uuid, new NET_DEVICEINFO_Ex());
        //入参
        NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstInParam.nPort = port;
        pstInParam.szIP = ip.getBytes();
        pstInParam.szPassword = password.getBytes();
        pstInParam.szUserName = username.getBytes();
        //出参
        NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstOutParam.stuDeviceInfo = deviceinfoMap.get(uuid);
        loginHandleMap.put(uuid, netsdkApi.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam));

        if (loginHandleMap.get(uuid).longValue() != 0) {
            log.debug("设备uuid[{}]ip[{}]port[{}]登录成功！", uuid, ip, port);
            return ResultData.success((Object) uuid);
        } else {
            log.debug("设备uuid[{}]ip[{}]port[{}]username[{}]password[{}]登录失败！", uuid, ip, port, username, password);
        }
        return ResultData.error("登录失败");
    }

    @Override
    public ResultData logout(String uuid) {
        if (loginHandleMap.get(uuid) == null) {
            //已登出
            log.debug("设备{}已登出", uuid);
            return ResultData.success();
        }
        try {
            // 退出程序时调用注销登录、反初始化接口
            netsdkApi.CLIENT_Logout(loginHandleMap.get(uuid));
            closeAlarmChan(uuid);
            loginHandleMap.remove(uuid);
            netsdkApi.CLIENT_Cleanup();
            log.debug("设备{}登出", uuid);
        } catch (Exception e) {
            log.error("设备注销失败：" + e.getMessage());
            return ResultData.error(e.getMessage());
        }
        return ResultData.success();
    }

    @Override
    public ResultData manualSnap(String uuid, String filePath) {
        fCaptureReceiveCB fCaptureReceiveCB = new fCaptureReceiveCB();
        Native.setCallbackThreadInitializer(fCaptureReceiveCB,
                new CallbackThreadInitializer(false, false, "snapPicture callback thread"));
        netsdkApi.CLIENT_SetSnapRevCallBack(fCaptureReceiveCB, null);

        // send caputre picture command to device
        SNAP_PARAMS stuSnapParams = new SNAP_PARAMS();
        stuSnapParams.Channel = 0;            // channel
        stuSnapParams.mode = 0;                // capture picture mode
        stuSnapParams.Quality = 3;                // picture quality
        stuSnapParams.InterSnap = 0;    // timer capture picture time interval
        stuSnapParams.CmdSerial = 0;            // request serial
        stuSnapParams.ImageSize = 1;

        IntByReference reserved = new IntByReference(0);
        if (!netsdkApi.CLIENT_SnapPictureEx(loginHandleMap.get(uuid), stuSnapParams, reserved)) {
            log.debug("CLIENT_SnapPictureEx Failed!" + ToolKits.getErrorCodePrint());
            return ResultData.error("抓拍失败");
        } else {
            log.debug("CLIENT_SnapPictureEx success");
            //获取照片
            try {
                snapPicturePath = filePath;
                synchronized (fCaptureReceiveCB.class) {
                    // 默认等待 3s, 防止设备断线时抓拍回调没有被触发，而导致死等
                    fCaptureReceiveCB.class.wait(3000L);
                }
            } catch (InterruptedException e) {
                log.debug("设备[{}]抓拍失败,{}", uuid, e.getMessage());
            }
        }
        log.debug("设备[{}]抓拍成功", uuid);
        return ResultData.success("抓拍成功");
    }

    @Override
    public ResultData controlGateway(String uuid) {
        NET_CTRL_ACCESS_OPEN openInfo = new NET_CTRL_ACCESS_OPEN();
        openInfo.nChannelID = 0;
        openInfo.emOpenDoorType = EM_OPEN_DOOR_TYPE.EM_OPEN_DOOR_TYPE_REMOTE;

        Pointer pointer = new Memory(openInfo.size());
        ToolKits.SetStructDataToPointer(openInfo, pointer, 0);
        boolean ret = netsdkApi.CLIENT_ControlDeviceEx(loginHandleMap.get(uuid),
                CtrlType.CTRLTYPE_CTRL_ACCESS_OPEN, pointer, null, 10000);
        if (ret) {
            log.debug("设备[{}]开门成功", uuid);
            return ResultData.success("开门成功");
        }
        log.debug("设备[{}]开门失败，{}", uuid, ToolKits.getErrorCodePrint());
        return ResultData.error("开门失败。" + ToolKits.getErrorCodePrint());
    }

    /**
     * 添加用户
     *
     * @param employeeNo 人员工号
     * @param name       人员名称
     * @param beginTime  开始时间 eg：2017-08-01T17:30:08
     * @param userType   人员类型 normal普通人员 visitor访客
     */
    @Override
    public ResultData addUser(String uuid, String employeeNo, String name, @NonNull String userType, Date beginTime, Date endTime) {

        /**
         * 入参
         */
        NET_IN_ACCESS_USER_SERVICE_INSERT stIn = new NET_IN_ACCESS_USER_SERVICE_INSERT();
        /**
         * 出参
         */
        NET_OUT_ACCESS_USER_SERVICE_INSERT stOut = new NET_OUT_ACCESS_USER_SERVICE_INSERT();


        try {
            List<PersonAddEntitiy> userInfoList = new LinkedList<>();
            PersonAddEntitiy personAddEntitiy = new PersonAddEntitiy();
            personAddEntitiy.setUserId(employeeNo);
            personAddEntitiy.setUserName(name);
            personAddEntitiy.setPasswd(null);
            personAddEntitiy.setRoomNo(null);
            personAddEntitiy.setStartTime(DateUtil.formatDateTime(beginTime));
            personAddEntitiy.setEndTime(DateUtil.formatDateTime(endTime));
            personAddEntitiy.setMemberType(userType);//
            userInfoList.add(personAddEntitiy);
            PersonAddEntitiy[] userInfos = userInfoList.toArray(new PersonAddEntitiy[0]);

            // 用户操作类型
            // 添加用户
            int emtype = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_INSERT;

            // 添加的用户个数
            int nMaxNum = userInfos.length;

            /**
             * 用户信息数组
             */
            // 先初始化用户信息数组
            NET_ACCESS_USER_INFO[] users = new NET_ACCESS_USER_INFO[nMaxNum];
            // 初始化返回的失败信息数组
            FAIL_CODE[] failCodes = new FAIL_CODE[nMaxNum];

            for (int i = 0; i < nMaxNum; i++) {
                users[i] = new NET_ACCESS_USER_INFO();
                failCodes[i] = new FAIL_CODE();
            }

            /**
             * 用户信息赋值
             */
            for (int i = 0; i < nMaxNum; i++) {
                // 用户ID, 用于后面的添加卡、人脸、指纹
                System.arraycopy(userInfos[i].userId.getBytes(), 0,
                        users[i].szUserID, 0, userInfos[i].userId.getBytes().length);

                // 用户名称
                try {
                    System.arraycopy(userInfos[i].userName.getBytes("GBK"), 0,
                            users[i].szName, 0,
                            userInfos[i].userName.getBytes("GBK").length);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // 用户类型
                if (userInfos[i].memberType.equals("normal")) {
                    users[i].emUserType = NET_ENUM_USER_TYPE.NET_ENUM_USER_TYPE_NORMAL;
                } else if (userInfos[i].memberType.equals("visitor")) {
                    users[i].emUserType = NET_ENUM_USER_TYPE.NET_ENUM_USER_TYPE_GUEST;
                }

                // 密码, UserID+密码开门时密码
                System.arraycopy(
                        (userInfos[i].userId + userInfos[i].passwd).getBytes(),
                        0,
                        users[i].szPsw,
                        0,
                        (userInfos[i].userId + userInfos[i].passwd).getBytes().length);

                // 来宾卡的通行次数
                users[i].nUserTime = 100;

                // 有效门数, 门个数 表示双门控制器
                users[i].nDoorNum = 1;

                // 有权限的门序号, 表示第一个门有权限
                users[i].nDoors[0] = 0;

                // 房间个数
                users[i].nRoom = 0;

                // 房间号
                if (StrUtil.isNotBlank(userInfos[i].roomNo)) {
                    System.arraycopy(userInfos[i].roomNo.getBytes(), 0,
                            users[i].szRoomNos[0].szRoomNo, 0,
                            userInfos[i].roomNo.getBytes().length);
                }

                // 与门数对应
                users[i].nTimeSectionNum = 1;

                // 表示第一个门全天有效
                users[i].nTimeSectionNo[0] = 255;

                // 开始有效期
                if (StrUtil.isNotBlank(userInfos[i].startTime)) {
                    DateTime dateTime = DateUtil.parse(userInfos[i].startTime);
                    users[i].stuValidBeginTime.setTime(dateTime.year(),
                            dateTime.month() + 1,
                            dateTime.dayOfMonth(),
                            dateTime.hour(true),
                            dateTime.minute(),
                            dateTime.second());
                } else {
                    users[i].stuValidBeginTime.setTime(2021, 4, 27, 16, 3, 10);
                }

                // 结束有效期
                if (StrUtil.isNotBlank(userInfos[i].endTime)) {
                    DateTime dateTime = DateUtil.parse(userInfos[i].endTime);
                    users[i].stuValidEndTime.setTime(dateTime.year(),
                            dateTime.month() + 1,
                            dateTime.dayOfMonth(),
                            dateTime.hour(true),
                            dateTime.minute(),
                            dateTime.second());
                } else {
                    users[i].stuValidEndTime.setTime(2031, 4, 27, 16, 3, 10);
                }
            }

            // /////////////////////////// 以下固定写法
            // /////////////////////////////////////
            stIn.nInfoNum = nMaxNum;
            stIn.pUserInfo = new Memory(users[0].size() * nMaxNum); // 申请内存
            stIn.pUserInfo.clear(users[0].size() * nMaxNum);

            // 将用户信息传给指针
            ToolKits.SetStructArrToPointerData(users, stIn.pUserInfo);

            stOut.nMaxRetNum = nMaxNum;
            stOut.pFailCode = new Memory(failCodes[0].size() * nMaxNum); // 申请内存
            stOut.pFailCode.clear(failCodes[0].size() * nMaxNum);

            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            if (netsdkApi.CLIENT_OperateAccessUserService(loginHandleMap.get(uuid), emtype,
                    stIn.getPointer(), stOut.getPointer(), TIME_OUT)) {
                // 将指针转为具体的信息
                ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);

                /**
                 * 具体的打印信息
                 */
                for (int i = 0; i < nMaxNum; i++) {
                    log.debug("[" + i + "]添加用户结果：" + failCodes[i].nFailCode);
                }
                return ResultData.success("添加用户成功");
            } else {
                log.debug("添加用户失败, " + ToolKits.getErrorCodePrint());
                return ResultData.error("添加用户失败, " + ToolKits.getErrorCodePrint());
            }
        } finally {
            stIn.read();
            stOut.read();
        }

    }

    @Override
    public ResultData modifyUser(String uuid, String employeeNo, String name, String userType, Date beginTime) {
        return null;
    }

    @Override
    public ResultData delUser(String uuid, String employeeNo) {
        /**
         * 入参
         */
        NET_IN_ACCESS_USER_SERVICE_REMOVE stIn = new NET_IN_ACCESS_USER_SERVICE_REMOVE();
        /**
         * 出参
         */
        NET_OUT_ACCESS_USER_SERVICE_REMOVE stOut = new NET_OUT_ACCESS_USER_SERVICE_REMOVE();

        try {
            List<String> employeeNoList = new LinkedList<>();
            employeeNoList.add(employeeNo);
            String[] userIDs = employeeNoList.toArray(new String[0]);

            // 删除的用户个数
            int nMaxNum = userIDs.length;

            // /////////////////////////// 以下固定写法
            // /////////////////////////////////////
            // 用户操作类型
            // 删除用户
            int emtype = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_REMOVE;

            // 初始化返回的失败信息数组
            FAIL_CODE[] failCodes = new FAIL_CODE[nMaxNum];
            for (int i = 0; i < nMaxNum; i++) {
                failCodes[i] = new FAIL_CODE();
            }

            // 用户ID个数
            stIn.nUserNum = userIDs.length;

            // 用户ID
            for (int i = 0; i < userIDs.length; i++) {
                System.arraycopy(userIDs[i].getBytes(), 0,
                        stIn.szUserIDs[i].szUserID, 0, userIDs[i].getBytes().length);
            }

            stOut.nMaxRetNum = nMaxNum;

            stOut.pFailCode = new Memory(failCodes[0].size() * nMaxNum); // 申请内存
            stOut.pFailCode.clear(failCodes[0].size() * nMaxNum);

            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            if (netsdkApi.CLIENT_OperateAccessUserService(loginHandleMap.get(uuid), emtype,
                    stIn.getPointer(), stOut.getPointer(), TIME_OUT)) {
                // 将指针转为具体的信息
                ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);

                /**
                 * 打印具体的信息
                 */
                for (int i = 0; i < nMaxNum; i++) {
                    log.debug("[" + i + "]删除用户结果：" + failCodes[i].nFailCode);
                }

                return ResultData.success("删除用户成功");
            } else {
                log.debug("删除用户失败, " + ToolKits.getErrorCodePrint());
                return ResultData.error("删除用户失败, " + ToolKits.getErrorCodePrint());
            }
        } finally {
            stIn.read();
            stOut.read();
        }
    }

    @Override
    public ResultData delUserFace(String uuid, String employeeNo) {
        int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_REMOVE;

        /** 入参 */
        NET_IN_REMOVE_FACE_INFO inRemove = new NET_IN_REMOVE_FACE_INFO();

        // 用户ID
        System.arraycopy(
                employeeNo.getBytes(), 0, inRemove.szUserID, 0, employeeNo.getBytes().length);

        /** 出参 */
        NET_OUT_REMOVE_FACE_INFO outRemove = new NET_OUT_REMOVE_FACE_INFO();

        inRemove.write();
        outRemove.write();
        boolean bRet =
                netsdkApi.CLIENT_FaceInfoOpreate(
                        loginHandleMap.get(uuid),
                        emType,
                        inRemove.getPointer(),
                        outRemove.getPointer(),
                        5000);
        inRemove.read();
        outRemove.read();
        if (bRet) {
            return ResultData.success("删除人脸成功");
        } else {
            log.error("删除人脸失败!" + ToolKits.getErrorCodePrint());
            return ResultData.error("删除人脸失败!" + ToolKits.getErrorCodePrint());
        }
    }

    /**
     * 添加人脸，人脸图片最大200k
     *
     * @param uuid
     * @param employeeNo 人员编号
     * @param filePath   文件地址
     * @return
     */
    @Override
    public ResultData addUserFace(String uuid, String employeeNo, String filePath) {

        int emType = EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_ADD; // 添加

        /** 入参 */
        NET_IN_ADD_FACE_INFO stIn = new NET_IN_ADD_FACE_INFO();

        // 用户ID
        System.arraycopy(employeeNo.getBytes(), 0, stIn.szUserID, 0, employeeNo.getBytes().length);

        // 人脸照片个数
        stIn.stuFaceInfo.nFacePhoto = 1;

        // 每张图片的大小
        Memory memory = null;
        try {
            memory = ToolKits.readPictureFile(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (memory != null) {
            stIn.stuFaceInfo.nFacePhotoLen[0] = (int) memory.size();
        }

        // 人脸照片数据,大小不超过100K, 图片格式为jpg
        stIn.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto = memory;

        /** 出参 */
        NET_OUT_ADD_FACE_INFO stOut = new NET_OUT_ADD_FACE_INFO();

        stIn.write();
        stOut.write();
        boolean bRet =
                netsdkApi.CLIENT_FaceInfoOpreate(
                        loginHandleMap.get(uuid),
                        emType,
                        stIn.getPointer(),
                        stOut.getPointer(),
                        5000);
        stIn.read();
        stOut.read();
        if (bRet) {
            return ResultData.success("添加人脸成功");
        } else {
            return ResultData.error("添加人脸失败, " + ToolKits.getErrorCodePrint());
        }
    }

    @Override
    public ResultData searchUser(String uuid, String[] employeeNos, Integer pageSize, Integer pageNum) {

        // 获取的用户个数
        int nMaxNum = employeeNos.length;

        // /////////////////////////// 以下固定写法
        // /////////////////////////////////////
        // 用户操作类型
        // 获取用户
        int emtype = NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_GET;

        /**
         * 用户信息数组
         */
        // 先初始化用户信息数组
        NET_ACCESS_USER_INFO[] users = new NET_ACCESS_USER_INFO[nMaxNum];
        // 初始化返回的失败信息数组
        FAIL_CODE[] failCodes = new FAIL_CODE[nMaxNum];

        for (int i = 0; i < nMaxNum; i++) {
            users[i] = new NET_ACCESS_USER_INFO();
            failCodes[i] = new FAIL_CODE();
        }

        /**
         * 入参
         */
        NET_IN_ACCESS_USER_SERVICE_GET stIn = new NET_IN_ACCESS_USER_SERVICE_GET();
        // 用户ID个数
        stIn.nUserNum = employeeNos.length;

        // 用户ID
        for (int i = 0; i < employeeNos.length; i++) {
            System.arraycopy(employeeNos[i].getBytes(), 0,
                    stIn.szUserIDs[i].szUserID, 0, employeeNos[i].getBytes().length);
        }

        /**
         * 出参
         */
        NET_OUT_ACCESS_USER_SERVICE_GET stOut = new NET_OUT_ACCESS_USER_SERVICE_GET();
        stOut.nMaxRetNum = nMaxNum;

        stOut.pUserInfo = new Memory(users[0].size() * nMaxNum); // 申请内存
        stOut.pUserInfo.clear(users[0].size() * nMaxNum);

        stOut.pFailCode = new Memory(failCodes[0].size() * nMaxNum); // 申请内存
        stOut.pFailCode.clear(failCodes[0].size() * nMaxNum);

        ToolKits.SetStructArrToPointerData(users, stOut.pUserInfo);
        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

        stIn.write();
        stOut.write();
        try {
            if (netsdkApi.CLIENT_OperateAccessUserService(
                    loginHandleMap.get(uuid),
                    emtype,
                    stIn.getPointer(),
                    stOut.getPointer(),
                    TIME_OUT)) {
                // 将指针转为具体的信息
                ToolKits.GetPointerDataToStructArr(stOut.pUserInfo, users);
                ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);

                /** 打印具体的信息 */
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < nMaxNum; i++) {
                    try {
                        log.debug("[" + i + "]用户名：" + new String(users[i].szName, "GBK").trim());
                        log.debug("[" + i + "]密码：" + new String(users[i].szPsw).trim());
                        log.debug("[" + i + "]查询用户结果：" + failCodes[i].nFailCode);
                        list.add(
                                JSONUtil.createObj()
                                        .set("name", new String(users[i].szName, "GBK").trim())
                                        .set("employeeNo", new String(users[i].szUserID)));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return ResultData.success("查询用户成功", list);
            } else {
                log.debug("查询用户失败, " + ToolKits.getErrorCodePrint());
            }
        } finally {
            stIn.read();
            stOut.read();
        }
        return ResultData.error("查询用户失败");
    }

    @Override
    public ResultData getEventRecord(String uuid, String dateStr, String dateType) {
        return null;
    }

    @Override
    public ResultData searchFaceInfo(String uuid, String employeeNo, String filePath) {
        String[] userIDs = {employeeNo};

        // 获取人脸的用户最大个数
        int nMaxCount = userIDs.length;

        // ////////////////////// 每个用户的人脸信息初始化 ////////////////////////
        NET_ACCESS_FACE_INFO[] faces = new NET_ACCESS_FACE_INFO[nMaxCount];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = new NET_ACCESS_FACE_INFO();

            // 根据每个用户的人脸图片的实际个数申请内存，最多5张照片

            faces[i].nFacePhoto = 1; // 每个用户图片个数

            // 对每张照片申请内存
            faces[i].nInFacePhotoLen[0] = 200 * 1024;
            faces[i].pFacePhotos[0].pFacePhoto = new Memory(200 * 1024); // 人脸照片数据,大小不超过200K
            faces[i].pFacePhotos[0].pFacePhoto.clear(200 * 1024);
        }

        // 初始化
        FAIL_CODE[] failCodes = new FAIL_CODE[nMaxCount];
        for (int i = 0; i < failCodes.length; i++) {
            failCodes[i] = new FAIL_CODE();
        }

        // 人脸操作类型
        // 获取人脸信息
        int emtype = NET_EM_ACCESS_CTL_FACE_SERVICE.NET_EM_ACCESS_CTL_FACE_SERVICE_GET;

        /**
         * 入参
         */
        NET_IN_ACCESS_FACE_SERVICE_GET stIn = new NET_IN_ACCESS_FACE_SERVICE_GET();
        stIn.nUserNum = nMaxCount;
        for (int i = 0; i < nMaxCount; i++) {
            System.arraycopy(userIDs[i].getBytes(), 0,
                    stIn.szUserIDs[i].szUserID, 0, userIDs[i].getBytes().length);
        }

        /**
         * 出参
         */
        NET_OUT_ACCESS_FACE_SERVICE_GET stOut = new NET_OUT_ACCESS_FACE_SERVICE_GET();
        stOut.nMaxRetNum = nMaxCount;

        stOut.pFaceInfo = new Memory(faces[0].size() * nMaxCount);
        stOut.pFaceInfo.clear(faces[0].size() * nMaxCount);

        stOut.pFailCode = new Memory(failCodes[0].size() * nMaxCount);
        stOut.pFailCode.clear(failCodes[0].size() * nMaxCount);

        ToolKits.SetStructArrToPointerData(faces, stOut.pFaceInfo);
        ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

        stIn.write();
        stOut.write();

        try {
            if (netsdkApi.CLIENT_OperateAccessFaceService(loginHandleMap.get(uuid), emtype,
                    stIn.getPointer(), stOut.getPointer(), TIME_OUT)) {
                // 将获取到的结果信息转成具体的结构体
                ToolKits.GetPointerDataToStructArr(stOut.pFaceInfo, faces);
                ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);


                // 打印具体信息
                // nMaxCount 几个用户
                for (int i = 0; i < nMaxCount; i++) {
                    log.debug("[" + i + "]用户ID : "
                            + new String(faces[i].szUserID).trim());

                    // 保存查询到的图片
                    for (int j = 0; j < faces[i].nFacePhoto; j++) {

                        if (faces[i].nFacePhoto == 0
                                || faces[i].pFacePhotos[j].pFacePhoto == null) {
                            return ResultData.error("获取人员人脸失败");
                        }

                        log.debug("路径：" + filePath);
                        // 人脸图片数据
                        byte[] buffer = faces[i].pFacePhotos[j].pFacePhoto
                                .getByteArray(0, faces[i].nOutFacePhotoLen[j]);

                        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(buffer);

                        FileUtil.mkParentDirs(filePath);
                        BufferedImage bufferedImage = ImageIO
                                .read(byteInputStream);
                        if (bufferedImage == null) {
                            return ResultData.error("获取人员人脸失败");
                        }
                        ImageIO.write(bufferedImage, "jpg", new File(filePath));
                    }

                    log.debug("[" + i + "]获取人脸结果 : " + failCodes[i].nFailCode);
                }
            } else {
                log.debug("获取人脸失败, " + ToolKits.getErrorCodePrint());
            }
        } catch (IOException e) {
            log.debug("获取人员人脸失败", e);
        } finally {
            stIn.read();
            stOut.read();
        }
        return ResultData.error("查询用户人脸失败");
    }

    @Override
    public ResultData updateCard(String uuid, String employeeNo, String cardNo, boolean isDeleteCard, String cardType) {
        /**
         * 入参
         */
        NET_IN_ACCESS_CARD_SERVICE_UPDATE stIn = new NET_IN_ACCESS_CARD_SERVICE_UPDATE();
        /**
         * 出参
         */
        NET_OUT_ACCESS_CARD_SERVICE_UPDATE stOut = new NET_OUT_ACCESS_CARD_SERVICE_UPDATE();

        try {
            CardAddEntity cardAddEntity = new CardAddEntity();
            cardAddEntity.setUserId(employeeNo);
            cardAddEntity.setCardNo(cardNo);
            cardAddEntity.setEmType(Integer.parseInt(cardType));
            CardAddEntity[] cardInfos = new CardAddEntity[]{cardAddEntity};
//            CardAddEntity[] cardInfos = cardAddEntities.toArray(new CardAddEntity[0]);

            // 修改的卡的最大个数
            int nMaxCount = cardInfos.length;

            // 卡片信息
            NET_ACCESS_CARD_INFO[] cards = new NET_ACCESS_CARD_INFO[nMaxCount];
            for (int i = 0; i < nMaxCount; i++) {
                cards[i] = new NET_ACCESS_CARD_INFO();
            }

            //
            FAIL_CODE[] failCodes = new FAIL_CODE[nMaxCount];
            for (int i = 0; i < nMaxCount; i++) {
                failCodes[i] = new FAIL_CODE();
            }

            /**
             * 卡信息赋值
             */
            for (int i = 0; i < nMaxCount; i++) {
                // 卡类型
                cards[i].emType = cardInfos[i].emType; // NET_ACCESSCTLCARD_TYPE;

                // 用户ID
                System.arraycopy(cardInfos[i].userId.getBytes(), 0,
                        cards[i].szUserID, 0, cardInfos[i].userId.getBytes().length);

                // 卡号
                System.arraycopy(cardInfos[i].cardNo.getBytes(), 0,
                        cards[i].szCardNo, 0, cardInfos[i].cardNo.getBytes().length);
            }

            // 卡操作类型
            // 修改卡
            int emtype = NET_EM_ACCESS_CTL_CARD_SERVICE.NET_EM_ACCESS_CTL_CARD_SERVICE_UPDATE;


            stIn.nInfoNum = nMaxCount;
            stIn.pCardInfo = new Memory((long) cards[0].size() * nMaxCount);
            stIn.pCardInfo.clear((long) cards[0].size() * nMaxCount);

            ToolKits.SetStructArrToPointerData(cards, stIn.pCardInfo);

            stOut.nMaxRetNum = nMaxCount;
            stOut.pFailCode = new Memory((long) failCodes[0].size() * nMaxCount);
            stOut.pFailCode.clear((long) failCodes[0].size() * nMaxCount);

            ToolKits.SetStructArrToPointerData(failCodes, stOut.pFailCode);

            stIn.write();
            stOut.write();
            if (netsdkApi.CLIENT_OperateAccessCardService(loginHandleMap.get(uuid), emtype,
                    stIn.getPointer(), stOut.getPointer(), TIME_OUT)) {
                // 将获取到的结果信息转成 failCodes
                ToolKits.GetPointerDataToStructArr(stOut.pFailCode, failCodes);

                // 打印具体信息
                for (int i = 0; i < nMaxCount; i++) {
                    log.debug("[" + i + "]修改卡结果 : " + failCodes[i].nFailCode);
                }
                return ResultData.success("修改卡成功");
            } else {
                log.debug("修改卡失败, " + ToolKits.getErrorCodePrint());
                return ResultData.error("修改卡失败, " + ToolKits.getErrorCodePrint());
            }
        } finally {
            stIn.read();
            stOut.read();
        }
    }

    @Override
    public ResultData carManualSnap(String uuid, String filePath) {
        return null;
    }

    @Override
    public ResultData closeAlarmChan(String uuid) {
        // 停止订阅报警
        boolean bRet = netsdkApi.CLIENT_StopListen(loginHandleMap.get(uuid));
        if (bRet) {
            log.debug("设备[{}]取消订阅报警信息.", uuid);
            return ResultData.success("取消订阅报警成功");
        }
        return ResultData.error("取消订阅报警失败");
    }

    @Override
    public ResultData setupAlarmChan(String uuid) {
        // 设置报警回调函数
        netsdkApi.CLIENT_SetDVRMessCallBack(fAlarmAccessDataCB.getInstance(), null);

        // 订阅报警
        boolean bRet = netsdkApi.CLIENT_StartListenEx(loginHandleMap.get(uuid));
        if (!bRet) {
            log.debug("设备[{}]订阅报警失败! LastError = 0x%x\n" + netsdkApi.CLIENT_GetLastError(), uuid);
        } else {
            log.debug("设备[{}]订阅报警成功.", uuid);
            return ResultData.success(StrUtil.format("订阅报警成功", uuid));
        }
        return ResultData.error(StrUtil.format("订阅报警失败", uuid));
    }

    /**
     * 清空人员
     *
     * @param uuid 登录信息
     * @return 结果
     */
    @Override
    public ResultData clearUser(String uuid) {
        /** 记录集操作 */
        NetSDKLib.NET_CTRL_RECORDSET_PARAM msg = new NetSDKLib.NET_CTRL_RECORDSET_PARAM();
        msg.emType = EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD; // 门禁卡记录集信息类型

        msg.write();
        boolean bRet =
                netsdkApi.CLIENT_ControlDevice(
                        loginHandleMap.get(uuid),
                        CtrlType.CTRLTYPE_CTRL_RECORDSET_CLEAR,
                        msg.getPointer(),
                        5000);
        msg.read();
        if (!bRet) {
            return ResultData.error("清空卡信息失败." + ToolKits.getErrorCodePrint());

        } else {
            return ResultData.success("清空卡信息成功");
        }
    }

    /**
     * 远程重启设备
     *
     * @param uuid 登录信息
     * @return 结果
     */
    @Override
    public ResultData reboot(String uuid) {
        if (!netsdkApi.CLIENT_ControlDevice(
                loginHandleMap.get(uuid), CtrlType.CTRLTYPE_CTRL_REBOOT, null, 3000)) {
            System.err.println("CLIENT_ControlDevice Failed!" + ToolKits.getErrorCodePrint());
            return ResultData.error("远程重启失败");
        }
        return ResultData.success("远程重启成功");
    }

    public byte[] GetFacePhotoData(String file) {
        int fileLen = GetFileSize(file);
        if (fileLen <= 0) {
            return null;
        }

        try {
            File infile = new File(file);
            if (infile.canRead()) {
                FileInputStream in = new FileInputStream(infile);
                byte[] buffer = new byte[fileLen];
                long currFileLen = 0;
                int readLen = 0;
                while (currFileLen < fileLen) {
                    readLen = in.read(buffer);
                    currFileLen += readLen;
                }

                in.close();
                return buffer;
            } else {
                log.error("Failed to open file %s for read!!!\n");
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to open file %s for read!!!\n");
            e.printStackTrace();
        }
        return null;
    }

    // 获取图片大小
    public int GetFileSize(String filePath) {
        File f = new File(filePath);
        if (f.exists() && f.isFile()) {
            return (int) f.length();
        } else {
            return 0;
        }
    }

    // 获取当前时间
    public String getDate() {
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDate.format(new Date()).replace(" ", "_").replace(":", "-");
    }

    /*
     * 报警事件回调 -----门禁事件(对应结构体 ALARM_ACCESS_CTL_EVENT_INFO)
     */
    private static class fAlarmAccessDataCB implements fMessCallBack {
        private fAlarmAccessDataCB() {
        }

        public static fAlarmAccessDataCB getInstance() {
            return fAlarmDataCBHolder.instance;
        }

        public boolean invoke(int lCommand, LLong lLoginID, Pointer pStuEvent,
                              int dwBufLen, String strDeviceIP, NativeLong nDevicePort,
                              Pointer dwUser) {
            System.out.printf("command = %x\n", lCommand);
            switch (lCommand) {
                case NetSDKLib.NET_ALARM_ACCESS_CTL_EVENT: // 设备请求对方发起对讲事件
                {
                    ALARM_ACCESS_CTL_EVENT_INFO msg = new ALARM_ACCESS_CTL_EVENT_INFO();
                    ToolKits.GetPointerData(pStuEvent, msg);
                    log.debug(msg.toString());
                    break;
                }
            }
            IDhEventCallBackHandle bean = SpringUtil.getBean(IDhEventCallBackHandle.class);
            if (bean != null) {
                ALARM_ACCESS_CTL_EVENT_INFO msg = new ALARM_ACCESS_CTL_EVENT_INFO();
                ToolKits.GetPointerData(pStuEvent, msg);
                bean.handle(
                        JSONUtil.createObj()
                                .set("employeeNo", new String(msg.szUserID))
                                .set("cardNo", new String(msg.szCardNo))
                                .set("eventType", msg.emEventType)
                                .set("status", msg.bStatus).set("date", msg.stuTime.toStringTimeEx())
                                .toString());
            }

            return true;
        }

        private static class fAlarmDataCBHolder {
            private static DhAccessDeviceClient.fAlarmAccessDataCB instance = new DhAccessDeviceClient.fAlarmAccessDataCB();
        }
    }

    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    public class fDisConnectCB implements fDisConnect {
        public void invoke(LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] Disconnect!\n", pchDVRIP, nDVRPort);
        }
    }

    // 网络连接恢复，设备重连成功回调
    // 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
    public class HaveReConnect implements fHaveReConnect {
        public void invoke(LLong loginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
        }
    }

    public class fCaptureReceiveCB implements fSnapRev {
        BufferedImage bufferedImage = null;

        public void invoke(LLong lLoginID, Pointer pBuf, int RevLen, int EncodeType, int CmdSerial, Pointer dwUser) {
            if (pBuf != null && RevLen > 0) {

                log.debug("strFileName = " + snapPicturePath);

                byte[] buf = pBuf.getByteArray(0, RevLen);
                ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buf);
                try {
                    bufferedImage = ImageIO.read(byteArrInput);
                    if (bufferedImage == null) {
                        return;
                    }
                    ImageIO.write(bufferedImage, "jpg", new File(snapPicturePath));
                } catch (IOException e) {
                    log.debug("抓拍失败", e);
                }

            }
        }
    }
}
