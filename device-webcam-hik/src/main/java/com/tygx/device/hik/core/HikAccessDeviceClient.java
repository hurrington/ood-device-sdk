package com.tygx.device.hik.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import com.ood.core.entity.ResultData;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.tygx.device.hik.constant.DeviceHikConstants;
import com.tygx.device.hik.entity.MonitorEventEntity;
import com.tygx.device.hik.entity.PersonCardEntity;
import com.tygx.device.hik.entity.PersonInformation;
import com.tygx.device.hik.utils.TransIsapi;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 海康设备客户端
 *
 * @author zsj
 */
public class HikAccessDeviceClient extends AbstractHikDevice implements IHikAccessDeviceClient {

    private static final Log log = Log.get(HikAccessDeviceClient.class);

    public static final int ISAPI_DATA_LEN = 1024 * 1024;
    public static final int ISAPI_STATUS_LEN = 4 * 4096;
    public static final int BYTE_ARRAY_LEN = 1024;
    private static final HikAccessDeviceClient singleton = new HikAccessDeviceClient();

    // 下发卡长连接句柄
    static Map<String, Integer> setCardCfgHandleMap = new HashMap<>();
    // 下发人脸长连接句柄
    static Map<String, Integer> setFaceCfgHandleMap = new HashMap<>();
    // 下发卡数据状态
    static Map<String, Integer> dwStateMap = new HashMap<>();
    // 下发人脸数据状态
    static Map<String, Integer> dwFaceStateMap = new HashMap<>();


    public HikAccessDeviceClient() {
    }

    public static HikAccessDeviceClient getInstance() {
        return singleton;
    }

    /**
     * 获取pinter数据
     *
     * @param pJavaStu Structure
     * @param pointer  Pointer
     */
    private static void getPointerData(Structure pJavaStu, Pointer pointer) {
        pJavaStu.write();
        Pointer pJavaMem = pJavaStu.getPointer();
        pJavaMem.write(0, pointer.getByteArray(0, pJavaStu.size()), 0, pJavaStu.size());
        pJavaStu.read();
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

    // 反序列化定义该方法，则不需要创建新对象
    private Object readResolve() throws ObjectStreamException {
        return singleton;
    }

    /**
     * 初始化dll
     *
     * @param dllPath dll文件路径 /home/sdk/hik/6.1.6.3/libhcnetsdk.so
     */
    @Override
    public ResultData init(String dllPath) {

        if (hCNetSDK != null) {
            // 避免重复初始化
            return ResultData.success();
        }
        log.info("海康设备sdk初始化，dll路径：" + dllPath);
        hCNetSDK = Native.load(dllPath, HCNetSDK.class);
        boolean init = hCNetSDK.NET_DVR_Init();
        if (init && getLogPath() != null) {
            generateLogsRegularly();
        }
        return init ? ResultData.success() : ResultData.error("初始化失败");
    }

    /**
     * 查询卡号
     *
     * @param cardNo 卡号
     * @return 结果
     */
    @Override
    public ResultData getOneCard(String uuid, String cardNo) {
        HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
        cardCond.read();
        cardCond.dwSize = cardCond.size();
        // 查询一个卡参数
        cardCond.dwCardNum = 1;
        cardCond.write();
        Pointer ptrCond = cardCond.getPointer();

        int lUserID = lUserIDMap.get(uuid);
        setCardCfgHandleMap.put(
                uuid,
                hCNetSDK.NET_DVR_StartRemoteConfig(
                        lUserID, HCNetSDK.NET_DVR_GET_CARD, ptrCond, cardCond.size(), null, null));
        if (setCardCfgHandleMap.get(uuid) == -1) {
            log.warn("建立查询卡参数长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return (ResultData.error("建立查询卡参数长连接失败"));
        } else {
            log.debug("建立查询卡参数长连接成功！");
        }

        // 查找指定卡号的参数，需要下发查找的卡号条件
        HCNetSDK.NET_DVR_CARD_SEND_DATA struCardNo = new HCNetSDK.NET_DVR_CARD_SEND_DATA();
        struCardNo.read();
        struCardNo.dwSize = struCardNo.size();

        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struCardNo.byCardNo[i] = 0;
        }
        System.arraycopy(cardNo.getBytes(), 0, struCardNo.byCardNo, 0, cardNo.length());
        struCardNo.write();

        HCNetSDK.NET_DVR_CARD_RECORD cardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
        cardRecord.read();

        IntByReference pInt = new IntByReference(0);

        while (true) {

            dwStateMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                            setCardCfgHandleMap.get(uuid),
                            struCardNo.getPointer(),
                            struCardNo.size(),
                            cardRecord.getPointer(),
                            cardRecord.size(),
                            pInt));
            cardRecord.read();
            if (dwStateMap.get(uuid) == -1) {
                log.warn(
                        "NET_DVR_SendWithRecvRemoteConfig查询卡参数调用失败，错误码："
                                + hCNetSDK.NET_DVR_GetLastError());
                break;
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                log.debug("配置等待");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error("海康设备配置等待", e);
                }
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                log.warn("获取卡参数失败, 卡号: " + cardNo);
                return ResultData.error("获取卡参数失败");
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                log.warn("获取卡参数异常, 卡号: " + cardNo);
                return (ResultData.error("获取卡参数异常"));
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                log.info(
                        "获取卡参数成功, 卡号: "
                                + new String(cardRecord.byCardNo).trim()
                                + ", 卡类型："
                                + cardRecord.byCardType
                                + ", 姓名："
                                + new String(cardRecord.byName, StandardCharsets.UTF_8).trim()
                                + "人员编号："
                                + cardRecord.dwEmployeeNo);
                PersonCardEntity personEntity = new PersonCardEntity();
                personEntity.setStrCardNo(new String(cardRecord.byCardNo).trim());
                personEntity.setName(new String(cardRecord.byName, StandardCharsets.UTF_8).trim());
                personEntity.setEmployeeNo(String.valueOf(cardRecord.dwEmployeeNo));
                return (new ResultData(0, "获取成功", personEntity));
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                log.debug("获取卡参数完成");
                break;
            }
        }

        if (!hCNetSDK.NET_DVR_StopRemoteConfig(setCardCfgHandleMap.get(uuid))) {
            log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.debug("NET_DVR_StopRemoteConfig接口成功");
        }
        return (ResultData.error("查询失败"));
    }

    /**
     * 获取所有人员卡信息
     *
     * @return 返回list
     */
    @Override
    public ResultData getAllCard(String uuid, Integer pageSize, Integer pageNum) {
        HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
        cardCond.read();
        if (ObjectUtil.hasEmpty(pageNum, pageSize)) {
            cardCond.dwSize = cardCond.size();
            cardCond.dwCardNum = 0xffffffff;
        } else {
            cardCond.dwSize = pageSize;
            cardCond.dwCardNum = pageNum;
        }
        // 查询所有
        cardCond.write();
        Pointer ptrCond = cardCond.getPointer();

        int lUserID = lUserIDMap.get(uuid);
        setCardCfgHandleMap.put(
                uuid,
                hCNetSDK.NET_DVR_StartRemoteConfig(
                        lUserID, HCNetSDK.NET_DVR_GET_CARD, ptrCond, cardCond.size(), null, null));
        if (setCardCfgHandleMap.get(uuid) == -1) {
            log.warn("建立下发卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return null;
        } else {
            log.debug("建立下发卡长连接成功！");
        }

        HCNetSDK.NET_DVR_CARD_RECORD struCardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
        struCardRecord.read();
        struCardRecord.dwSize = struCardRecord.size();
        struCardRecord.write();

        IntByReference pInt = new IntByReference(0);
        List<PersonCardEntity> list = CollUtil.newLinkedList();
        while (true) {
            dwStateMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_GetNextRemoteConfig(
                            setCardCfgHandleMap.get(uuid),
                            struCardRecord.getPointer(),
                            struCardRecord.size()));
            struCardRecord.read();
            if (dwStateMap.get(uuid) == -1) {
                log.warn(
                        "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                + hCNetSDK.NET_DVR_GetLastError());
                break;
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                log.debug("配置等待");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error("配置等待异常", e);
                }
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                log.warn("获取卡参数失败");
                break;
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                log.warn("获取卡参数异常");
                break;
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                log.info(
                        "获取卡参数成功, 卡号: "
                                + new String(struCardRecord.byCardNo).trim()
                                + ", 卡类型："
                                + struCardRecord.byCardType
                                + ", 姓名："
                                + new String(struCardRecord.byName, StandardCharsets.UTF_8).trim());
                PersonCardEntity personEntity = new PersonCardEntity();
                personEntity.setStrCardNo(new String(struCardRecord.byCardNo).trim());
                personEntity.setName(
                        new String(struCardRecord.byName, StandardCharsets.UTF_8).trim());
                personEntity.setEmployeeNo(String.valueOf(struCardRecord.dwEmployeeNo));
                list.add(personEntity);
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                log.debug("获取卡参数完成");
                break;
            }
        }

        if (!hCNetSDK.NET_DVR_StopRemoteConfig(setCardCfgHandleMap.get(uuid))) {
            log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.debug("NET_DVR_StopRemoteConfig接口成功");
            return ResultData.success(list);
        }
        return null;
    }

    /**
     * 下发
     *
     * @param cardNo     卡号
     * @param cardName   人员名称
     * @param employeeNo 人员编号
     * @return 返回状态码
     */
    @Override
    public ResultData setOneCard(
            String uuid,
            String cardNo,
            String cardName,
            int employeeNo,
            String userType,
            String beginTime,
            String endTime) {
        try {
            HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
            cardCond.read();
            cardCond.dwSize = cardCond.size();
            // 下发一张
            cardCond.dwCardNum = 1;
            cardCond.write();
            Pointer ptrStruCond = cardCond.getPointer();

            int lUserID = lUserIDMap.get(uuid);
            setCardCfgHandleMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            HCNetSDK.NET_DVR_SET_CARD,
                            ptrStruCond,
                            cardCond.size(),
                            null,
                            null));
            if (setCardCfgHandleMap.get(uuid) == -1) {
                log.warn("建立下发卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("建立下发卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("建立下发卡长连接成功！");
            }

            HCNetSDK.NET_DVR_CARD_RECORD struCardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
            struCardRecord.read();
            struCardRecord.dwSize = struCardRecord.size();

            for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
                struCardRecord.byCardNo[i] = 0;
            }
            System.arraycopy(cardNo.getBytes(), 0, struCardRecord.byCardNo, 0, cardNo.length());

            // 普通卡
            struCardRecord.byCardType = 1;
            // 是否为首卡，0-否，1-是
            struCardRecord.byLeaderCard = 0;
            struCardRecord.byUserType = 0;
            // 门1有权限
            struCardRecord.byDoorRight[0] = 1;

            if (ObjectUtil.isNotNull(beginTime) && "visitor".equals(userType)) {
                DateTime beginTimeDate = DateUtil.parse(beginTime);
                DateTime endTimeDate;
                if (StrUtil.isBlank(endTime)) {
                    // 没有结束时间的默认为30分钟
                    endTimeDate = DateUtil.offsetMinute(beginTimeDate, 30);
                } else {
                    endTimeDate = DateUtil.parse(endTime);
                }

                int i = beginTimeDate.month() + 1;
                byte i1 = (byte) i;

                // 卡有效期使能，下面是卡有效期从2000-1-1 11:11:11到2030-1-1 11:11:11
                struCardRecord.struValid.byEnable = 1;
                setCardTime(struCardRecord, beginTimeDate, endTimeDate);
            } else if ("normal".equals(userType)) {
                if (!StrUtil.hasBlank(beginTime, endTime)) {
                    DateTime beginTimeDate = DateUtil.parse(beginTime);
                    DateTime endTimeDate = DateUtil.parse(endTime);

                    struCardRecord.struValid.byEnable = 1;
                    setCardTime(struCardRecord, beginTimeDate, endTimeDate);

                } else {
                    // 普通成员，不设置有效期
                    // 卡有效期使能，下面是卡有效期从2000-1-1 11:11:11到2030-1-1 11:11:11
                    struCardRecord.struValid.byEnable = 0;
                    DateTime beginTimeDate = DateUtil.parse("2000-1-1 11:11:11");
                    DateTime endTimeDate = DateUtil.parse("2030-1-1 11:11:11");
                    setCardTime(struCardRecord, beginTimeDate, endTimeDate);
                }

            } else {
                log.error(
                        "下发人员信息失败，参数异常："
                                + cardNo
                                + "\t"
                                + cardName
                                + "\t"
                                + userType
                                + "\t"
                                + beginTime);
                return ResultData.error("下发人员信息失败");
            }

            // 卡计划模板1有效
            struCardRecord.wCardRightPlan[0] = 1;
            // 工号
            struCardRecord.dwEmployeeNo = employeeNo;

            // 姓名
            byte[] strCardName = cardName.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < HCNetSDK.NAME_LEN; i++) {
                struCardRecord.byName[i] = 0;
            }
            System.arraycopy(strCardName, 0, struCardRecord.byName, 0, strCardName.length);
            struCardRecord.write();

            HCNetSDK.NET_DVR_CARD_STATUS struCardStatus = new HCNetSDK.NET_DVR_CARD_STATUS();
            struCardStatus.read();
            struCardStatus.dwSize = struCardStatus.size();
            struCardStatus.write();

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                setCardCfgHandleMap.get(uuid),
                                struCardRecord.getPointer(),
                                struCardRecord.size(),
                                struCardStatus.getPointer(),
                                struCardStatus.size(),
                                pInt));
                struCardStatus.read();
                if (dwStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                    log.debug("配置等待");
                    Thread.sleep(10);
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn(
                            "下发卡失败, 卡号: "
                                    + new String(struCardStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + struCardStatus.dwErrorCode);
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn(
                            "下发卡异常, 卡号: "
                                    + new String(struCardStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + struCardStatus.dwErrorCode);
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    if (struCardStatus.dwErrorCode != 0) {
                        // 卡号或者编号不是唯一
                        log.warn(
                                "下发卡成功,但是错误码"
                                        + struCardStatus.dwErrorCode
                                        + ", 卡号："
                                        + new String(struCardStatus.byCardNo).trim());
                        log.warn("卡号或者编号不是唯一");
                    } else {
                        log.info(
                                "下发卡成功, 卡号: "
                                        + new String(struCardStatus.byCardNo).trim()
                                        + ", 状态："
                                        + struCardStatus.byStatus);
                        return ResultData.success("下发成功");
                    }
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    log.debug("下发卡完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(setCardCfgHandleMap.get(uuid))) {
                log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("NET_DVR_StopRemoteConfig接口成功");
            }

        } catch (InterruptedException e) {
            log.error("配置等待异常", e);
        }
        return ResultData.error("下发失败。错误编码：" + hCNetSDK.NET_DVR_GetLastError());
    }

    /**
     * 下发人脸
     *
     * @param carNo 卡号
     * @param file  图片
     * @return 返回状态码
     */
    @Override
    public ResultData setOneCardFace(String uuid, String carNo, File file) {
        try {
            HCNetSDK.NET_DVR_FACE_COND faceCond = new HCNetSDK.NET_DVR_FACE_COND();
            faceCond.read();
            faceCond.dwSize = faceCond.size();
            // 人脸关联的卡号（设置时该参数可不设置）
            faceCond.byCardNo = carNo.getBytes();
            // 下发一张
            faceCond.dwFaceNum = 1;
            // 人脸读卡器编号
            faceCond.dwEnableReaderNo = 1;
            faceCond.write();
            Pointer ptrFaceCond = faceCond.getPointer();

            int lUserID = lUserIDMap.get(uuid);
            setFaceCfgHandleMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            HCNetSDK.NET_DVR_SET_FACE,
                            ptrFaceCond,
                            faceCond.size(),
                            null,
                            null));
            if (setFaceCfgHandleMap.get(uuid) == -1) {
                log.warn("建立下发人脸长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("建立下发人脸长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("建立下发人脸长连接成功！");
            }

            HCNetSDK.NET_DVR_FACE_RECORD struFaceRecord = new HCNetSDK.NET_DVR_FACE_RECORD();
            struFaceRecord.read();
            struFaceRecord.dwSize = struFaceRecord.size();

            for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
                struFaceRecord.byCardNo[i] = 0;
            }
            System.arraycopy(carNo.getBytes(), 0, struFaceRecord.byCardNo, 0, carNo.length());

            /*****************************************
             * 从本地文件里面读取JPEG图片二进制数据
             *****************************************/
            FileInputStream picfile = null;
            int picdataLength = 0;

            picfile = new FileInputStream(file);

            picdataLength = picfile.available();

            if (picdataLength < 0) {
                log.warn("input file dataSize < 0");
                return ResultData.error("找不到文件");
            }

            HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(picdataLength);
            try {
                picfile.read(ptrpicByte.byValue);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            ptrpicByte.write();
            struFaceRecord.dwFaceLen = picdataLength;
            struFaceRecord.pFaceBuffer = ptrpicByte.getPointer();

            struFaceRecord.write();

            HCNetSDK.NET_DVR_FACE_STATUS struFaceStatus = new HCNetSDK.NET_DVR_FACE_STATUS();
            struFaceStatus.read();
            struFaceStatus.dwSize = struFaceStatus.size();
            struFaceStatus.write();

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwFaceStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                setFaceCfgHandleMap.get(uuid),
                                struFaceRecord.getPointer(),
                                struFaceRecord.size(),
                                struFaceStatus.getPointer(),
                                struFaceStatus.size(),
                                pInt));
                struFaceStatus.read();
                if (dwFaceStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwFaceStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                    log.debug("配置等待");
                    Thread.sleep(10);
                } else if (dwFaceStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn(
                            "下发人脸失败, 卡号: "
                                    + new String(struFaceStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwFaceStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn(
                            "下发卡异常, 卡号: "
                                    + new String(struFaceStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwFaceStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    if (struFaceStatus.byRecvStatus != 1) {
                        log.warn(
                                "下发卡失败，人脸读卡器状态"
                                        + struFaceStatus.byRecvStatus
                                        + ", 卡号："
                                        + new String(struFaceStatus.byCardNo).trim());
                        break;
                    } else {
                        log.info(
                                "下发卡成功, 卡号: "
                                        + new String(struFaceStatus.byCardNo).trim()
                                        + ", 状态："
                                        + struFaceStatus.byRecvStatus);
                        return ResultData.success(
                                "下发卡成功, 卡号: "
                                        + new String(struFaceStatus.byCardNo).trim()
                                        + ", 状态："
                                        + struFaceStatus.byRecvStatus);
                    }
                } else if (dwFaceStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    log.debug("下发人脸完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(setFaceCfgHandleMap.get(uuid))) {
                log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("NET_DVR_StopRemoteConfig接口成功");
            }
        } catch (IOException e) {
            log.error("图片读取异常", e);
        } catch (InterruptedException e) {
            log.error("配置等待异常", e);
        }
        return ResultData.error("下发人脸失败");
    }

    // 2020-10-27 10:04:06 zsj 手动抓拍 返回jpg文件

    /**
     * 删除人脸
     *
     * @param cardNo 卡号
     * @return 结果
     */
    @Override
    public ResultData delOneCardFace(String uuid, String cardNo) {
        HCNetSDK.NET_DVR_FACE_PARAM_CTRL faceDelCond = new HCNetSDK.NET_DVR_FACE_PARAM_CTRL();
        faceDelCond.dwSize = faceDelCond.size();
        // 删除方式：0- 按卡号方式删除，1- 按读卡器删除
        faceDelCond.byMode = 0;

        faceDelCond.struProcessMode.setType(HCNetSDK.NET_DVR_FACE_PARAM_BYCARD.class);

        // 需要删除人脸关联的卡号
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            faceDelCond.struProcessMode.struByCard.byCardNo[i] = 0;
        }
        System.arraycopy(
                cardNo.getBytes(),
                0,
                faceDelCond.struProcessMode.struByCard.byCardNo,
                0,
                cardNo.length());

        // 读卡器
        faceDelCond.struProcessMode.struByCard.byEnableCardReader[0] = 1;
        // 人脸ID
        faceDelCond.struProcessMode.struByCard.byFaceID[0] = 1;
        faceDelCond.write();

        Pointer ptrFaceDelCond = faceDelCond.getPointer();

        int lUserID = lUserIDMap.get(uuid);
        boolean bRet =
                hCNetSDK.NET_DVR_RemoteControl(
                        lUserID,
                        HCNetSDK.NET_DVR_DEL_FACE_PARAM_CFG,
                        ptrFaceDelCond,
                        faceDelCond.size());
        if (!bRet) {
            log.warn("删除人脸失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.error("删除人脸失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("删除人脸成功！cardNo：" + cardNo);
            return ResultData.success("删除人脸成功！cardNo：" + cardNo);
        }
    }

    /**
     * 删除卡号
     *
     * @param cardNo 卡号
     * @return 结果
     */
    @Override
    public ResultData delOneCard(String uuid, String cardNo) {
        try {
            HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
            cardCond.read();
            cardCond.dwSize = cardCond.size();
            // 下发一张
            cardCond.dwCardNum = 1;
            cardCond.write();
            Pointer ptrcond = cardCond.getPointer();

            int lUserID = lUserIDMap.get(uuid);
            setCardCfgHandleMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            HCNetSDK.NET_DVR_DEL_CARD,
                            ptrcond,
                            cardCond.size(),
                            null,
                            null));
            if (setCardCfgHandleMap.get(uuid) == -1) {
                log.warn("建立删除卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("建立删除卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("建立删除卡长连接成功！");
            }

            HCNetSDK.NET_DVR_CARD_SEND_DATA cardData = new HCNetSDK.NET_DVR_CARD_SEND_DATA();
            cardData.read();
            cardData.dwSize = cardData.size();

            for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
                cardData.byCardNo[i] = 0;
            }
            for (int i = 0; i < cardNo.length(); i++) {
                cardData.byCardNo[i] = cardNo.getBytes()[i];
            }
            cardData.write();

            HCNetSDK.NET_DVR_CARD_STATUS struCardStatus = new HCNetSDK.NET_DVR_CARD_STATUS();
            struCardStatus.read();
            struCardStatus.dwSize = struCardStatus.size();
            struCardStatus.write();

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                setCardCfgHandleMap.get(uuid),
                                cardData.getPointer(),
                                cardData.size(),
                                struCardStatus.getPointer(),
                                struCardStatus.size(),
                                pInt));
                struCardStatus.read();
                if (dwStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                    log.debug("配置等待");
                    Thread.sleep(10);
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn(
                            "删除卡失败, 卡号: "
                                    + new String(struCardStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + struCardStatus.dwErrorCode);
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn(
                            "删除卡异常, 卡号: "
                                    + new String(struCardStatus.byCardNo).trim()
                                    + ", 错误码："
                                    + struCardStatus.dwErrorCode);
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    if (struCardStatus.dwErrorCode != 0) {
                        log.warn(
                                "删除卡成功,但是错误码"
                                        + struCardStatus.dwErrorCode
                                        + ", 卡号："
                                        + new String(struCardStatus.byCardNo).trim());
                    } else {
                        log.info(
                                "删除卡成功, 卡号: "
                                        + new String(struCardStatus.byCardNo).trim()
                                        + ", 状态："
                                        + struCardStatus.byStatus);
                        return ResultData.success(
                                "删除卡成功, 卡号: "
                                        + new String(struCardStatus.byCardNo).trim()
                                        + ", 状态："
                                        + struCardStatus.byStatus);
                    }
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    log.debug("删除卡完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(setCardCfgHandleMap.get(uuid))) {
                log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("NET_DVR_StopRemoteConfig接口成功");
            }
        } catch (InterruptedException e) {
            log.error("配置等待异常", e);
        }
        return ResultData.error("删除卡号失败");
    }

    // 2020-11-23 10:24:02 zsj 海康6163新接口

    /**
     * 手动抓拍
     *
     * @param filePath 输出抓拍地址
     * @return 结果
     */
    @Override
    public ResultData manualSnap(String uuid, String filePath) {
        // 登陆
        int lUserID = lUserIDMap.get(uuid);
        Byte channel = channelMap.get(uuid);
        if (lUserID == -1) {
            return ResultData.error("手动抓拍失败");
        }
        HCNetSDK.NET_DVR_WORKSTATE_V30 workState = new HCNetSDK.NET_DVR_WORKSTATE_V30();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState_V30(lUserID, workState)) {
            // 返回Boolean值，判断是否获取设备能力
            log.info("hksdk(抓图)-返回设备状态失败");
        }
        log.info("准备拍照，userId:[{}],startChan:[{}],时间：[{}]", (Object) lUserID, channel, DateUtil.now());
        // 图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        // 设置图片分辨率
        jpeg.wPicSize = 0xff;
        // 设置图片质量
        jpeg.wPicQuality = 1;
        boolean res =
                hCNetSDK.NET_DVR_CaptureJPEGPicture(
                        lUserID, channel, jpeg, filePath.getBytes(StandardCharsets.UTF_8));
        // 单帧数据捕获图片
        if (!res) {
            log.warn("抓拍失败!" + " err: " + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.success("抓拍失败: " + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("抓拍成功。路径：" + filePath);
            return ResultData.success("抓拍成功。路径：" + filePath, filePath);
        }
    }

    /**
     * 远程开门
     *
     * @param uuid 登录信息
     * @return 结果
     */
    @Override
    public ResultData controlGateway(String uuid) {
        return controlGateway(uuid, 1, 1);
    }

    /**
     * 门禁监控
     *
     * @param gatewayIndex 门禁序号（楼层编号、锁ID），从1开始，-1表示对所有门（或者梯控的所有楼层）进行操作
     * @param operateType  命令值：0- 关闭（对于梯控，表示受控），1- 打开（对于梯控，表示开门），2- 常开（对于梯控，表示自由、通道状态），3-
     *                     常关（对于梯控，表示禁用），4- 恢复（梯控，普通状态），5- 访客呼梯（梯控），6- 住户呼梯（梯控）
     * @return 结果
     */
    @Override
    public ResultData controlGateway(String uuid, int gatewayIndex, int operateType) {
        // 登陆
        int lUserID = lUserIDMap.get(uuid);
        Byte channel = channelMap.get(uuid);
        if (lUserID == -1) {
            return ResultData.error("控制门禁失败，未登陆设备");
        }
        HCNetSDK.NET_DVR_WORKSTATE_V30 workState = new HCNetSDK.NET_DVR_WORKSTATE_V30();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState_V30(lUserID, workState)) {
            // 返回Boolean值，判断是否获取设备能力
            log.info("hksdk-返回设备状态失败");
        }
        log.info("准备控制门禁，userId:[{}],startChan:[{}],时间：[{}]", (Object) lUserID, channel, DateUtil.now());
        boolean b = hCNetSDK.NET_DVR_ControlGateway(lUserID, gatewayIndex, operateType);
        if (!b) {
            log.warn("控制门禁失败!" + " err: " + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.error("控制门禁失败!" + " err: " + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("控制门禁成功");
            return ResultData.success("控制门禁成功");
        }
    }

    /**
     * 以人为中心 添加人
     *
     * @param employeeNo 人员工号
     * @param name       人员名称
     * @param beginTime  开始时间 eg：2017-08-01T17:30:08
     * @param userType   人员类型
     */
    @Override
    public ResultData addUser(
            String uuid,
            String employeeNo,
            String name,
            String userType,
            Date beginTime,
            Date endTime) {

        /*
        海康数据结构：
        {
            "UserInfo" : {
                "employeeNo": 1,    //必填，integer，工号（人员ID）
                "name": "",    //可选，string，姓名
                "userType ": "normal",    //必填，string，人员类型，normal-普通人（主人），visitor-来宾（访客），blackList-黑名单人，tenant-租户
                "openDelayEnabled": true,    //可选，boolean，是否开门延迟，true-是，false-否
                "Valid" : {    //必填，有效期参数（byEnable不使能代表长期有效）
                    "enable": true,    //必填，boolean，使能有效期，false-不使能，true-使能
                    "beginTime": "2017-08-01T17:30:08",    //必填，有效期起始时间
                    "endTime": "2017-08-31T17:30:08"    //必填，有效期结束时间
                },
                "belongGroup ": "1,3,5",    //可选，string，所属群组
                "password": "123456",    //可选，string，密码
                "doorRight": "1,3",    //可选，string，门权限（代表对门1、门3有权限）（锁权限，此处为锁ID，可填写多个，代表对锁1、锁3有权限）
                "RightPlan" : [    //可选，门权限计划（锁权限计划）
                    {
                        "doorNo": 1,    //可选，integer，门编号（锁ID）
                        "planTemplateNo": "1,3,5"    //可选，string，计划模板编号，同个门不同计划模板采用权限或的方式处理
                    }
                ],
                "maxOpenDoorTime": 0,    //可选，integer，最大开门次数，0为无次数限制
                "roomNumber": 123,    //可选，integer，房间号
                "floorNumber": 1,    //可选，integer，层号
                "doubleLockRight": true    //可选，boolean，反锁开门权限，true-有权限，false-无权限
            }
        }
         */

        ResultData resultData = ResultData.error("添加人员失败");
        try {
            // 数组
            HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);
            String strInBuffer = "POST /ISAPI/AccessControl/UserInfo/Record?format=json";
            // 字符串拷贝到数组中
            System.arraycopy(
                    strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());
            ptrByteArray.write();

            int lUserID = lUserIDMap.get(uuid);
            int lHandler =
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            DeviceHikConstants.NET_DVR_JSON_CONFIG,
                            ptrByteArray.getPointer(),
                            strInBuffer.length(),
                            null,
                            null);
            if (lHandler < 0) {
                log.error(
                        "AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为"
                                + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("添加失败");
            } else {
                log.debug("AddUserInfo NET_DVR_StartRemoteConfig 成功!");

                // 根据iCharEncodeType判断，如果iCharEncodeType返回6，则是UTF-8编码。
                // 如果是0或者1或者2，则是GBK编码
                byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

                // 将中文字符编码之后用数组拷贝的方式，避免因为编码导致的长度问题

                String strInBuffer1;
                String strInBuffer2;

                // ps：如果是访客有时间限制
                if (ObjectUtil.isNotNull(beginTime)
                        && DeviceHikConstants.MEMBER_TYPE_VISITOR.equals(userType)) {
                    // 访客
                    String beginTimeStr = DateUtil.format(beginTime, "yyyy-MM-dd'T'HH:mm:ss");
                    if (endTime == null) {
                        endTime = DateUtil.offsetMinute(beginTime, 30);
                    }
                    String endTimeStr = DateUtil.format(endTime, "yyyy-MM-dd'T'HH:mm:ss");
                    strInBuffer1 =
                            "{\"UserInfo\":{\"Valid\":{\"beginTime\":\""
                                    + beginTimeStr
                                    + "\",\"enable\":true,\"endTime\":\""
                                    + endTimeStr
                                    + "\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\""
                                    + employeeNo
                                    + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
                    strInBuffer2 =
                            "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"visitor\"}}";
                } else if (DeviceHikConstants.MEMBER_TYPE_NORMAL.equals(userType)) {
                    // 普通成员，不设置有效期
                    String beginTimeStr =
                            beginTime != null
                                    ? DateUtil.format(beginTime, "yyyy-MM-dd'T'HH:mm:ss")
                                    : "2017-08-01T17:30:08";
                    String endTimeStr =
                            endTime != null
                                    ? DateUtil.format(endTime, "yyyy-MM-dd'T'HH:mm:ss")
                                    : "2030-08-01T17:30:08";
                    strInBuffer1 =
                            "{\"UserInfo\":{\"Valid\":{\"beginTime\":\""
                                    + beginTimeStr
                                    + "\",\"enable\":false,\"endTime\":\""
                                    + endTimeStr
                                    + "\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\""
                                    + employeeNo
                                    + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
                    strInBuffer2 =
                            "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"normal\"}}";
                } else {
                    log.error(
                            "下发人员信息失败，参数异常："
                                    + employeeNo
                                    + "\t"
                                    + name
                                    + "\t"
                                    + userType
                                    + "\t"
                                    + beginTime);
                    return ResultData.error(
                            "下发人员信息失败，参数异常："
                                    + employeeNo
                                    + "\t"
                                    + name
                                    + "\t"
                                    + userType
                                    + "\t"
                                    + beginTime);
                }

                int iStringSize = nameBytes.length + strInBuffer1.length() + strInBuffer2.length();

                HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(iStringSize);
                System.arraycopy(
                        strInBuffer1.getBytes(), 0, ptrByte.byValue, 0, strInBuffer1.length());
                System.arraycopy(
                        nameBytes, 0, ptrByte.byValue, strInBuffer1.length(), nameBytes.length);
                System.arraycopy(
                        strInBuffer2.getBytes(),
                        0,
                        ptrByte.byValue,
                        strInBuffer1.length() + nameBytes.length,
                        strInBuffer2.length());
                ptrByte.write();

                log.debug(new String(ptrByte.byValue));

                HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);

                IntByReference pInt = new IntByReference(0);
                while (true) {
                    dwStateMap.put(
                            uuid,
                            hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                    lHandler,
                                    ptrByte.getPointer(),
                                    iStringSize,
                                    ptrOutuff.getPointer(),
                                    1024,
                                    pInt));
                    // 读取返回的json并解析
                    ptrOutuff.read();
                    String strResult = new String(ptrOutuff.byValue).trim();
                    log.debug(
                            "dwStateMap.get(uuid):"
                                    + dwStateMap.get(uuid)
                                    + ",strResult:"
                                    + strResult);

                    JSONObject jsonResult = new JSONObject(strResult);
                    int statusCode = jsonResult.getInt("statusCode");
                    String statusString = jsonResult.getStr("statusString");

                    if (dwStateMap.get(uuid) == -1) {
                        log.warn(
                                "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                        + hCNetSDK.NET_DVR_GetLastError());
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                        log.debug("配置等待");
                        Thread.sleep(10);
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                        log.warn("下发人员失败, json retun:" + jsonResult);

                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                        log.warn("下发人员异常, json retun:" + jsonResult);
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                        // 返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如有些设备可能因为人员已存在等原因下发失败，所以需要解析Json报文
                        if (statusCode != 1) {
                            log.warn("下发人员成功,但是有异常情况:" + jsonResult);
                        } else {
                            log.info("下发人员成功: json retun:" + jsonResult);
                            resultData = ResultData.success("添加成功");
                        }
                        resultData.setData(strResult);
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                        // 下发人员时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                        log.debug("下发人员完成");
                        break;
                    }
                }
                if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                    log.warn(
                            "NET_DVR_StopRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                } else {
                    log.debug("NET_DVR_StopRemoteConfig接口成功");
                }
            }
        } catch (InterruptedException e) {
            log.error("配置等待失败", e);
        }
        return resultData;
    }

    /**
     * 修改人员信息
     *
     * @param employeeNo 人员编号
     * @param name       人员名称
     * @param userType   人员类型
     * @param beginTime  开始时间（门禁权限）
     */
    @Override
    public ResultData modifyUser(
            String uuid, String employeeNo, String name, String userType, Date beginTime) {
        ResultData resultData = ResultData.error("修改失败");
        try {
            // 数组
            HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);
            String strInBuffer = "PUT /ISAPI/AccessControl/UserInfo/Modify?format=json";
            // 字符串拷贝到数组中
            System.arraycopy(
                    strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());
            ptrByteArray.write();

            int lUserID = lUserIDMap.get(uuid);
            int lHandler =
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            DeviceHikConstants.NET_DVR_JSON_CONFIG,
                            ptrByteArray.getPointer(),
                            strInBuffer.length(),
                            null,
                            null);
            if (lHandler < 0) {
                log.warn(
                        "AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为"
                                + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error(
                        "AddUserInfo NET_DVR_StartRemoteConfig 失败,错误码为"
                                + hCNetSDK.NET_DVR_GetLastError());
            } else {

                log.debug("AddUserInfo NET_DVR_StartRemoteConfig 成功!");
                // 根据iCharEncodeType判断，如果iCharEncodeType返回6，则是UTF-8编码。
                // 如果是0或者1或者2，则是GBK编码
                byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

                // 将中文字符编码之后用数组拷贝的方式，避免因为编码导致的长度问题
                String strInBuffer1;
                String strInBuffer2;
                if (ObjectUtil.isNotNull(beginTime)
                        && DeviceHikConstants.MEMBER_TYPE_VISITOR.equals(userType)) {
                    // 访客
                    String beginTimeStr = DateUtil.format(beginTime, "yyyy-MM-dd'T'HH:mm:ss");
                    DateTime endTime = DateUtil.offsetMinute(beginTime, 30);
                    String endTimeStr = DateUtil.format(endTime, "yyyy-MM-dd'T'HH:mm:ss");
                    strInBuffer1 =
                            "{\"UserInfo\":{\"Valid\":{\"beginTime\":\""
                                    + beginTimeStr
                                    + "\",\"enable\":true,\"endTime\":\""
                                    + endTimeStr
                                    + "\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\""
                                    + employeeNo
                                    + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
                    strInBuffer2 =
                            "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"visitor\"}}";
                } else if (DeviceHikConstants.MEMBER_TYPE_NORMAL.equals(userType)) {
                    // 普通成员，不设置有效期
                    strInBuffer1 =
                            "{\"UserInfo\":{\"Valid\":{\"beginTime\":\"2017-08-01T17:30:08\",\"enable\":false,\"endTime\":\"2030-08-01T17:30:08\"},\"checkUser\":false,\"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\": 1,\"planTemplateNo\": \"1,3,5\"}],\"employeeNo\":\""
                                    + employeeNo
                                    + "\",\"floorNumber\":1,\"maxOpenDoorTime\":0,\"name\":\"";
                    strInBuffer2 =
                            "\",\"openDelayEnabled\":false,\"password\":\"123456\",\"roomNumber\":1,\"userType\":\"normal\"}}";
                } else {
                    log.error(
                            "下发人员信息失败，参数异常："
                                    + employeeNo
                                    + "\t"
                                    + name
                                    + "\t"
                                    + userType
                                    + "\t"
                                    + beginTime);
                    return ResultData.error(
                            "下发人员信息失败，参数异常："
                                    + employeeNo
                                    + "\t"
                                    + name
                                    + "\t"
                                    + userType
                                    + "\t"
                                    + beginTime);
                }

                int iStringSize = nameBytes.length + strInBuffer1.length() + strInBuffer2.length();

                HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(iStringSize);
                System.arraycopy(
                        strInBuffer1.getBytes(), 0, ptrByte.byValue, 0, strInBuffer1.length());
                System.arraycopy(
                        nameBytes, 0, ptrByte.byValue, strInBuffer1.length(), nameBytes.length);
                System.arraycopy(
                        strInBuffer2.getBytes(),
                        0,
                        ptrByte.byValue,
                        strInBuffer1.length() + nameBytes.length,
                        strInBuffer2.length());
                ptrByte.write();

                log.debug("修改人员JSON数据：" + new String(ptrByte.byValue));

                HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);

                IntByReference pInt = new IntByReference(0);
                while (true) {
                    dwStateMap.put(
                            uuid,
                            hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                    lHandler,
                                    ptrByte.getPointer(),
                                    iStringSize,
                                    ptrOutuff.getPointer(),
                                    1024,
                                    pInt));
                    // 读取返回的json并解析
                    ptrOutuff.read();
                    String strResult = new String(ptrOutuff.byValue).trim();
                    log.debug(
                            "dwStateMap.get(uuid):"
                                    + dwStateMap.get(uuid)
                                    + ",strResult:"
                                    + strResult);

                    JSONObject jsonResult = new JSONObject(strResult);
                    int statusCode = jsonResult.getInt("statusCode");
                    String statusString = jsonResult.getStr("statusString");

                    if (dwStateMap.get(uuid) == -1) {
                        log.warn(
                                "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                        + hCNetSDK.NET_DVR_GetLastError());
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                        log.debug("配置等待");
                        Thread.sleep(10);
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                        log.warn("修改人员失败, json retun:" + jsonResult);
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                        log.warn("修改人员异常, json retun:" + jsonResult);
                        break;
                    } else if (dwStateMap.get(uuid)
                            == HCNetSDK
                            .NET_SDK_CONFIG_STATUS_SUCCESS) { // 返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如有些设备可能因为人员已存在等原因下发失败，所以需要解析Json报文
                        if (statusCode != 1) {
                            log.warn("修改人员成功,但是有异常情况:" + jsonResult);
                        } else {
                            log.info("修改人员成功: json retun:" + jsonResult);
                            resultData = ResultData.success("修改成功");
                        }
                        resultData.setData(strResult);
                        break;
                    } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                        // 下发人员时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                        log.debug("修改人员完成");
                        break;
                    }
                }
                if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                    log.warn(
                            "NET_DVR_StopRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                } else {
                    log.debug("NET_DVR_StopRemoteConfig接口成功");
                }
            }
        } catch (InterruptedException e) {
            log.error("配置等待失败", e);
        }
        return resultData;
    }

    /**
     * 以人为中心 删除人 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     * @return 结果
     */
    @Override
    public ResultData delUser(String uuid, String employeeNo) {
        ResultData resultData = ResultData.error("删除失败");

        String strURL = "PUT /ISAPI/AccessControl/UserInfo/Delete?format=json";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        // 输入删除条件
        HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();
        String inbuffer =
                "{\"UserInfoDelCond\":{\"EmployeeNoList\":[{\"employeeNo\":\""
                        + employeeNo
                        + "\"}]}}";
        ptrInBuffer.byValue = inbuffer.getBytes();
        ptrInBuffer.write();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT xmlInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        xmlInput.read();
        xmlInput.dwSize = xmlInput.size();
        xmlInput.lpRequestUrl = ptrUrl.getPointer();
        xmlInput.dwRequestUrlLen = ptrUrl.byValue.length;
        xmlInput.lpInBuffer = ptrInBuffer.getPointer();
        xmlInput.dwInBufferSize = ptrInBuffer.byValue.length;
        xmlInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT xmlOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        xmlOutput.read();
        xmlOutput.dwSize = xmlOutput.size();
        xmlOutput.lpOutBuffer = ptrOutByte.getPointer();
        xmlOutput.dwOutBufferSize = ptrOutByte.size();
        xmlOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        xmlOutput.dwStatusSize = ptrStatusByte.size();
        xmlOutput.write();

        int lUserID = lUserIDMap.get(uuid);
        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, xmlInput, xmlOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.warn("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return ResultData.error("NET_DVR_STDXMLConfig失败，错误号：" + iErr);

        } else {
            xmlOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            log.info("删除人员输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            log.info("删除人员返回状态：" + strStatus);
            JSONObject jsonObject = new JSONObject(strOutXML);
            if (jsonObject.getInt("statusCode") == 1) {
                resultData = ResultData.success("删除成功");
            }
            resultData.setData(strOutXML);
        }
        return resultData;
    }

    /**
     * 清空所有人员
     *
     * @param uuid 登录信息
     * @return 结果
     */
    @Override
    public ResultData clearUser(String uuid) {
        ResultData resultData = ResultData.error("删除失败");
        int userID = lUserIDMap.get(uuid);
        // 删除所有人员
        String deleteUserjson =
                "{\n"
                        + "\t\"UserInfoDetail\": {\t\n"
                        + "\t\t\"mode\":  \"all\",\t\n"
                        + "\t\t\"EmployeeNoList\": [\t\n"
                        + "\t\t]\n"
                        + "\n"
                        + "\t}\n"
                        + "}";
        String deleteUserUrl = "PUT /ISAPI/AccessControl/UserInfoDetail/Delete?format=json";
        String result = TransIsapi.put_isapi(userID, deleteUserUrl, deleteUserjson, hCNetSDK);
        System.out.println(result);
        // 获取删除进度
        while (true) {
            String getDeleteProcessUrl =
                    "GET /ISAPI/AccessControl/UserInfoDetail/DeleteProcess?format=json";
            String deleteResult = TransIsapi.get_isapi(userID, getDeleteProcessUrl, hCNetSDK);
            JSONObject jsonObject = new JSONObject(deleteResult);
            JSONObject jsonObject1 = jsonObject.getJSONObject("UserInfoDetailDeleteProcess");
            String process = jsonObject1.getStr("status");
            System.out.println("process =" + process);
            if (process.equals("processing")) {
                System.out.println("正在删除");
                continue;
            } else if (process.equals("success")) {
                resultData = ResultData.success("删除成功");
                break;
            } else if (process.equals("failed")) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                resultData = ResultData.error("删除失败，错误代码：" + iErr);
                break;
            }
        }
        return resultData;
    }

    /**
     * 远程重启设备
     *
     * @param uuid 登录信息
     * @return 结果
     */
    @Override
    public ResultData reboot(String uuid) {
        int lUserID = lUserIDMap.get(uuid);
        boolean result = hCNetSDK.NET_DVR_RebootDVR(lUserID);
        if (result) {
            return ResultData.success();
        }
        return ResultData.error();
    }

    /**
     * 以人为中心 删除人脸 <br>
     * 设备内没有此{employeeNo}也会返回成功
     *
     * @param employeeNo 人员编号
     */
    @Override
    public ResultData delUserFace(String uuid, String employeeNo) {
        ResultData resultData = ResultData.error("删除人脸失败");

        String strURL =
                "PUT /ISAPI/Intelligent/FDLib/FDSearch/Delete?format=json&FDID=1&faceLibType=blackFD";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        // 输入删除条件
        HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();
        String strInbuffer = "{\"FPID\":[{\"value\":\"" + employeeNo + "\"}]}";
        ptrInBuffer.byValue = strInbuffer.getBytes();
        ptrInBuffer.write();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT XMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        XMLInput.read();
        XMLInput.dwSize = XMLInput.size();
        XMLInput.lpRequestUrl = ptrUrl.getPointer();
        XMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
        XMLInput.lpInBuffer = ptrInBuffer.getPointer();
        XMLInput.dwInBufferSize = ptrInBuffer.byValue.length;
        XMLInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT XMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        XMLOutput.read();
        XMLOutput.dwSize = XMLOutput.size();
        XMLOutput.lpOutBuffer = ptrOutByte.getPointer();
        XMLOutput.dwOutBufferSize = ptrOutByte.size();
        XMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        XMLOutput.dwStatusSize = ptrStatusByte.size();
        XMLOutput.write();

        int lUserID = lUserIDMap.get(uuid);
        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, XMLInput, XMLOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.warn("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return ResultData.error("NET_DVR_STDXMLConfig失败，错误号：" + iErr);

        } else {
            XMLOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            log.info("删除人脸输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            log.info("删除人脸返回状态：" + strStatus);
            JSONObject jsonObject = new JSONObject(strOutXML);
            if (jsonObject.getInt("statusCode") == 1) {
                resultData = ResultData.success("删除成功");
            }
            resultData.setData(strOutXML);
        }
        return resultData;
    }

    /**
     * 以人为中心 添加人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   文件地址
     */
    @Override
    public ResultData addUserFace(String uuid, String employeeNo, String filePath) {
        if (!FileUtil.exist(filePath)) {
            return ResultData.error("找不到人员照片");
        }
        try {
            return addUserFace(uuid, employeeNo, new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            return ResultData.error("下发人脸失败");
        }
    }

    /**
     * 以人为中心 添加人脸
     *
     * @param employeeNo 人员编号
     * @param picfile    文件地址
     */
    @Override
    public ResultData addUserFace(String uuid, String employeeNo, FileInputStream picfile) {
        ResultData resultData = ResultData.error("添加人脸失败");

        if (picfile == null) {
            return ResultData.error("找不到人员照片");
        }

        try {
            // 数组
            HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);
            String strInBuffer = "POST /ISAPI/Intelligent/FDLib/FaceDataRecord?format=json ";
            // 字符串拷贝到数组中
            System.arraycopy(
                    strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());
            ptrByteArray.write();

            int lUserID = lUserIDMap.get(uuid);
            int lHandler =
                    hCNetSDK.NET_DVR_StartRemoteConfig(
                            lUserID,
                            DeviceHikConstants.NET_DVR_FACE_DATA_RECORD,
                            ptrByteArray.getPointer(),
                            strInBuffer.length(),
                            null,
                            null);
            if (lHandler < 0) {
                log.warn(
                        "Addface NET_DVR_StartRemoteConfig 失败,错误码为"
                                + hCNetSDK.NET_DVR_GetLastError());
                return resultData;
            } else {
                log.info("Addface NET_DVR_StartRemoteConfig 成功!");

                // 批量下发多个人脸（不同工号）
                int iNum = 2;
                HCNetSDK.NET_DVR_JSON_DATA_CFG[] struAddFaceDataCfg =
                        (HCNetSDK.NET_DVR_JSON_DATA_CFG[])
                                new HCNetSDK.NET_DVR_JSON_DATA_CFG().toArray(iNum);

                // 下发的人脸图片
                struAddFaceDataCfg[0].read();

                JSONObject jsonObject = new JSONObject();
                jsonObject.set("faceLibType", "blackFD");
                jsonObject.set("FDID", "1");
                // 人脸下发关联的工号
                jsonObject.set("FPID", employeeNo);

                String strJsonData = jsonObject.toString();
                log.debug("下发人脸的json报文:" + strJsonData);

                // 字符串拷贝到数组中
                System.arraycopy(
                        strJsonData.getBytes(), 0, ptrByteArray.byValue, 0, strJsonData.length());
                ptrByteArray.write();

                struAddFaceDataCfg[0].dwSize = struAddFaceDataCfg[0].size();
                struAddFaceDataCfg[0].lpJsonData = ptrByteArray.getPointer();
                struAddFaceDataCfg[0].dwJsonDataSize = strJsonData.length();

                /*****************************************
                 * 从本地文件里面读取JPEG图片二进制数据
                 *****************************************/

                int picdataLength = 0;

                picdataLength = picfile.available();

                if (picdataLength < 0) {
                    log.warn("input file dataSize < 0");
                    return resultData;
                }

                HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(picdataLength);
                try {
                    picfile.read(ptrpicByte.byValue);
                    picfile.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }

                ptrpicByte.write();
                struAddFaceDataCfg[0].dwPicDataSize = picdataLength;
                struAddFaceDataCfg[0].lpPicData = ptrpicByte.getPointer();
                struAddFaceDataCfg[0].write();

                HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(1024);
                IntByReference pInt = new IntByReference(0);

                dwStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                lHandler,
                                struAddFaceDataCfg[0].getPointer(),
                                struAddFaceDataCfg[0].dwSize,
                                ptrOutuff.getPointer(),
                                ptrOutuff.size(),
                                pInt));
                // 读取返回的json并解析
                ptrOutuff.read();
                String strResult = new String(ptrOutuff.byValue).trim();
                log.debug(
                        "dwStateMap.get(uuid):" + dwStateMap.get(uuid) + ",strResult:" + strResult);

                JSONObject jsonResult =
                        StrUtil.isNotBlank(strResult)
                                ? new JSONObject(strResult)
                                : new JSONObject();
                // String statusString = jsonResult.getString("statusString");

                if (dwStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn("下发人脸失败, json retun:" + jsonResult);
                    // 可以继续下发下一个
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn("下发人脸异常, json retun:" + jsonResult);
                    // 异常是长连接异常，不能继续下发后面的数据，需要重新建立长连接
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    // 返回NET_SDK_CONFIG_STATUS_SUCCESS代表流程走通了，但并不代表下发成功，比如人脸图片不符合设备规范等原因，所以需要解析Json报文
                    int statusCode = jsonResult.getInt("statusCode");
                    if (statusCode != 1) {
                        log.warn("下发人脸成功,但是有异常情况:" + jsonResult);
                    } else {
                        log.debug("下发人脸成功,  json retun:" + jsonResult);
                        resultData = ResultData.success("添加人脸成功");
                    }
                    resultData.setData(strResult);
                    // 可以继续下发下一个
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    // 下发人脸时：dwState其实不会走到这里，因为设备不知道我们会下发多少个人，所以长连接需要我们主动关闭
                    log.debug("下发人脸完成");
                } else {
                    log.debug("下发人脸识别，其他状态：" + dwStateMap.get(uuid));
                }

                if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                    log.warn(
                            "NET_DVR_StopRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                } else {
                    log.debug("NET_DVR_StopRemoteConfig接口成功");
                }
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
        }
        return resultData;
    }

    /**
     * 以人为中心 查人员信息
     *
     * @param employeeNos 人员编号（可以为空）
     * @return 结果
     */
    @Override
    public ResultData searchUser(
            String uuid, String[] employeeNos, Integer pageSize, Integer pageNum) {
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024); // 数组
        String strInBuffer = "POST /ISAPI/AccessControl/UserInfo/Search?format=json";
        System.arraycopy(
                strInBuffer.getBytes(),
                0,
                ptrByteArray.byValue,
                0,
                strInBuffer.length()); // 字符串拷贝到数组中
        ptrByteArray.write();

        int lUserID = lUserIDMap.get(uuid);
        int lHandler =
                hCNetSDK.NET_DVR_StartRemoteConfig(
                        lUserID,
                        DeviceHikConstants.NET_DVR_JSON_CONFIG,
                        ptrByteArray.getPointer(),
                        strInBuffer.length(),
                        null,
                        null);
        if (lHandler < 0) {
            log.warn(
                    "SearchUserInfo NET_DVR_StartRemoteConfig 失败,错误码为"
                            + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.error("查询失败");
        } else {
            // 组装查询的JSON报文，这边查询的是所有的卡
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonSearchCond = new JSONObject();

            // 如果需要查询指定的工号人员信息，把下面注释的内容去除掉即可
            if (employeeNos != null) {
                JSONArray employeeNoList = new JSONArray();
                for (String employeeNo : employeeNos) {
                    JSONObject employeeNoObj = new JSONObject();
                    employeeNoObj.set("employeeNo", employeeNo);
                    employeeNoList.put(employeeNoObj);
                }
                jsonSearchCond.set("EmployeeNoList", employeeNoList);
            }

            jsonSearchCond.set("searchID", IdUtil.fastUUID());
            if (ObjectUtil.hasEmpty(pageNum, pageSize)) {
                jsonSearchCond.set("searchResultPosition", 0);
                jsonSearchCond.set("maxResults", 10);
            } else {
                jsonSearchCond.set("searchResultPosition", (pageNum - 1) * pageSize);
                jsonSearchCond.set("maxResults", pageSize);
            }
            jsonObject.set("UserInfoSearchCond", jsonSearchCond);

            String strInbuff = jsonObject.toString();
            log.debug("查询人员的json报文:" + strInbuff);

            // 把string传递到Byte数组中，后续用.getPointer()方法传入指针地址中。
            HCNetSDK.BYTE_ARRAY ptrInbuff = new HCNetSDK.BYTE_ARRAY(strInbuff.length());
            System.arraycopy(strInbuff.getBytes(), 0, ptrInbuff.byValue, 0, strInbuff.length());
            ptrInbuff.write();

            // 定义接收结果的结构体
            HCNetSDK.BYTE_ARRAY ptrOutuff = new HCNetSDK.BYTE_ARRAY(10 * 1024);

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                lHandler,
                                ptrInbuff.getPointer(),
                                strInbuff.length(),
                                ptrOutuff.getPointer(),
                                10 * 1024,
                                pInt));
                if (dwStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                    log.debug("配置等待");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error("配置等待异常", e);
                    }
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn("查询人员失败");
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn("查询人员异常");
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    ptrOutuff.read();
                    String resultList = new String(ptrOutuff.byValue).trim();
                    log.debug("查询人员成功, json:" + resultList);
                    return ResultData.success(JSONUtil.parse(resultList));
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    log.debug("获取人员完成");
                    break;
                }
            }

            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                log.debug("NET_DVR_StopRemoteConfig接口成功");
            }
        }

        return ResultData.error("添加失败");
    }

    /**
     * 获取事件 （人脸打卡）
     *
     * @param dateStr  日期 2020-01-01
     * @param dateType 日期类型 month day
     * @return 结果
     */
    @Override
    public ResultData getEventRecord(String uuid, String dateStr, String dateType) {
        List<PersonInformation> stringList = new ArrayList<>();
        HCNetSDK.NET_DVR_ACS_EVENT_COND struEventCond = new HCNetSDK.NET_DVR_ACS_EVENT_COND();
        struEventCond.read();
        struEventCond.dwSize = struEventCond.size();
        HCNetSDK.NET_DVR_TIME timesStart = new HCNetSDK.NET_DVR_TIME();
        HCNetSDK.NET_DVR_TIME timesEnd = new HCNetSDK.NET_DVR_TIME();
        DateTime date = DateUtil.parse(dateStr);
        int yearCheck = date.year();
        int monthCheck = date.month() + 1;
        int dayCheck = date.dayOfMonth();
        // 时间设置
        if (DeviceHikConstants.DATE_TYPE_MONTH.equals(dateType)) {
            timesStart.dwYear = yearCheck;
            timesStart.dwMonth = monthCheck;
            timesStart.dwDay = 1;
            timesStart.dwHour = 0;
            timesStart.dwMinute = 0;
            timesStart.dwSecond = 0;
            struEventCond.struStartTime = timesStart;
            timesEnd.dwYear = yearCheck;
            timesEnd.dwMonth = monthCheck;
            timesEnd.dwDay = DateUtil.getEndValue(date.toCalendar(), DateField.DAY_OF_MONTH);
            timesEnd.dwHour = 23;
            timesEnd.dwMinute = 59;
            timesEnd.dwSecond = 59;
            struEventCond.struEndTime = timesEnd;
        } else if (DeviceHikConstants.DATE_TYPE_DAY.equals(dateType)) {
            timesStart.dwYear = yearCheck;
            timesStart.dwMonth = monthCheck;
            timesStart.dwDay = dayCheck;
            timesStart.dwHour = 0;
            timesStart.dwMinute = 0;
            timesStart.dwSecond = 0;
            struEventCond.struStartTime = timesStart;
            timesEnd.dwYear = yearCheck;
            timesEnd.dwMonth = monthCheck;
            timesEnd.dwDay = dayCheck;
            timesEnd.dwHour = 23;
            timesEnd.dwMinute = 59;
            timesEnd.dwSecond = 59;
            struEventCond.struEndTime = timesEnd;
        }

        // 人脸打卡信息 zsj 2021-3-9 17:30:45
        struEventCond.dwMajor = DeviceHikConstants.DEVICE_EVENT_FACE_CHECK_DWMAJOR; // 门禁事件
        //        struEventCond.dwMinor =
        // DeviceHikConstants.DEVICE_EVENT_FACE_CHECK_DWMINOR;//开启这个只接收人脸
        struEventCond.write();
        Pointer ptrStruCond = struEventCond.getPointer();
        int lUserID = lUserIDMap.get(uuid);
        int event =
                hCNetSDK.NET_DVR_StartRemoteConfig(
                        lUserID,
                        HCNetSDK.NET_DVR_GET_ACS_EVENT,
                        ptrStruCond,
                        struEventCond.size(),
                        null,
                        null);
        if (event == -1) {
            log.warn("连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.success(stringList);
        } else {
            log.info("连接成功！");
        }

        HCNetSDK.NET_DVR_ACS_EVENT_CFG struEventRecord = new HCNetSDK.NET_DVR_ACS_EVENT_CFG();
        struEventRecord.read();
        struEventRecord.dwSize = struEventRecord.size();
        struEventRecord.write();

        //        IntByReference pInt = new IntByReference(0);
        dwStateMap.put(uuid, -1);
        while (true) {
            dwStateMap.put(
                    uuid,
                    hCNetSDK.NET_DVR_GetNextRemoteConfig(
                            event, struEventRecord.getPointer(), struEventRecord.size()));
            struEventRecord.read();
            if (dwStateMap.get(uuid) == -1) {
                log.warn("接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_GET_NEXT_STATUS_NEED_WAIT) {
                log.debug("配置等待");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error("配置等待异常", e);
                }
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_GET_NEXT_STATUS_SUCCESS) {
                log.debug("获取成功");
                PersonInformation personInformation = new PersonInformation();
                String employeeNo =
                        new String(struEventRecord.struAcsEventInfo.byEmployeeNo).trim();
                // 处理日期格式
                DateTime datetime =
                        DateUtil.date()
                                .setField(DateField.YEAR, struEventRecord.struTime.dwYear)
                                .setField(DateField.MONTH, struEventRecord.struTime.dwMonth - 1)
                                .setField(DateField.DAY_OF_MONTH, struEventRecord.struTime.dwDay)
                                .setField(DateField.HOUR_OF_DAY, struEventRecord.struTime.dwHour)
                                .setField(DateField.MINUTE, struEventRecord.struTime.dwMinute)
                                .setField(DateField.SECOND, struEventRecord.struTime.dwSecond);
                personInformation.setEmployeeNo(employeeNo);
                personInformation.setDateTime(datetime.toString());
                stringList.add(personInformation);

                System.out.print(
                        "获取卡参数成功, 时间: "
                                + struEventRecord.struTime.dwYear
                                + "-"
                                + struEventRecord.struTime.dwMonth
                                + "-"
                                + struEventRecord.struTime.dwDay
                                + " "
                                + struEventRecord.struTime.dwHour
                                + ":"
                                + struEventRecord.struTime.dwMinute
                                + ":"
                                + struEventRecord.struTime.dwSecond
                                + ",卡号 ："
                                + new String(struEventRecord.struAcsEventInfo.byCardNo).trim()
                                + ",员工号："
                                + new String(struEventRecord.struAcsEventInfo.byEmployeeNo).trim()
                                + ", 用户："
                                + new String(struEventRecord.sNetUser).trim()
                                + ",详细参数："
                                + struEventRecord.struAcsEventInfo);
            } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_GET_NEXT_STATUS_FINISH) {
                log.debug("获取卡参数完成");
                break;
            }
        }

        if (!hCNetSDK.NET_DVR_StopRemoteConfig(event)) {
            log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("NET_DVR_StopRemoteConfig接口成功");
        }

        return ResultData.success(stringList);
    }

    /**
     * 查询人脸
     *
     * @param employeeNo 人员编号
     * @param filePath   保存路径路径
     * @return 结果
     */
    @Override
    public ResultData searchFaceInfo(String uuid, String employeeNo, String filePath) {
        Map<String, Object> map = new HashMap<>();

        // 数组
        HCNetSDK.BYTE_ARRAY ptrByteArray = new HCNetSDK.BYTE_ARRAY(1024);
        String strInBuffer = "POST /ISAPI/Intelligent/FDLib/FDSearch?format=json";
        // 字符串拷贝到数组中
        System.arraycopy(strInBuffer.getBytes(), 0, ptrByteArray.byValue, 0, strInBuffer.length());
        ptrByteArray.write();

        int lUserID = lUserIDMap.get(uuid);
        int lHandler =
                hCNetSDK.NET_DVR_StartRemoteConfig(
                        lUserID,
                        DeviceHikConstants.NET_DVR_FACE_DATA_SEARCH,
                        ptrByteArray.getPointer(),
                        strInBuffer.length(),
                        null,
                        null);
        if (lHandler < 0) {
            log.warn(
                    "SearchFaceInfo NET_DVR_StartRemoteConfig 失败,错误码为"
                            + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.error("下载人脸失败");
        } else {
            log.debug("SearchFaceInfo NET_DVR_StartRemoteConfig成功!");

            JSONObject jsonObject = new JSONObject();
            jsonObject.set("searchResultPosition", 0);
            jsonObject.set("maxResults", 1);
            jsonObject.set("faceLibType", "blackFD");
            jsonObject.set("FDID", "1");
            // 人脸关联的工号，同下发人员时的employeeNo字段
            jsonObject.set("FPID", employeeNo);

            String strInbuff = jsonObject.toString();
            log.debug("查询人脸的json报文:" + strInbuff);

            // 把string传递到Byte数组中，后续用.getPointer()方法传入指针地址中。
            HCNetSDK.BYTE_ARRAY ptrInbuff = new HCNetSDK.BYTE_ARRAY(strInbuff.length());
            System.arraycopy(strInbuff.getBytes(), 0, ptrInbuff.byValue, 0, strInbuff.length());
            ptrInbuff.write();

            HCNetSDK.NET_DVR_JSON_DATA_CFG jsonDataCfg = new HCNetSDK.NET_DVR_JSON_DATA_CFG();
            jsonDataCfg.write();

            IntByReference pInt = new IntByReference(0);

            while (true) {
                dwStateMap.put(
                        uuid,
                        hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(
                                lHandler,
                                ptrInbuff.getPointer(),
                                strInbuff.length(),
                                jsonDataCfg.getPointer(),
                                jsonDataCfg.size(),
                                pInt));
                jsonDataCfg.read();
                if (dwStateMap.get(uuid) == -1) {
                    log.warn(
                            "NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码："
                                    + hCNetSDK.NET_DVR_GetLastError());
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_NEED_WAIT) {
                    log.debug("配置等待");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        log.error("门禁设备保存图片失败", e);
                    }
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                    log.warn("查询人脸失败");
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                    log.warn("查询人脸异常");
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                    log.info("查询人脸成功");

                    // 解析JSON字符串
                    HCNetSDK.BYTE_ARRAY pJsonData =
                            new HCNetSDK.BYTE_ARRAY(jsonDataCfg.dwJsonDataSize);
                    getPointerData(pJsonData, jsonDataCfg.lpJsonData);
                    String strResult = new String(pJsonData.byValue).trim();
                    log.info("strResult:" + strResult);
                    JSONObject jsonResult = new JSONObject(strResult);

                    int numOfMatches = jsonResult.getInt("numOfMatches");
                    // 确认有人脸
                    if (numOfMatches != 0) {
                        JSONArray matchList = jsonResult.getJSONArray("MatchList");
                        JSONObject MatchList_1 = matchList.getJSONObject(0);
                        // 获取json中人脸关联的工号
                        String FPID = MatchList_1.getStr("FPID");

                        FileOutputStream fout;
                        try {
                            String fileName =
                                    "\\accessDeviceImage\\FPID_[" + FPID + "]_FacePic.jpg";
                            String file = filePath + fileName;
                            FileUtil.mkParentDirs(file);
                            log.debug("查询人脸图片路径" + file);

                            map.put("path", file);
                            map.put("employeeNo", FPID);

                            fout = new FileOutputStream(file);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    jsonDataCfg.lpPicData.getByteBuffer(
                                            offset, jsonDataCfg.dwPicDataSize);
                            byte[] bytes = new byte[jsonDataCfg.dwPicDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            log.error("门禁设备保存图片失败", e);
                        }
                    }
                    break;
                } else if (dwStateMap.get(uuid) == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                    log.debug("获取人脸完成");
                    break;
                }
            }
            if (!hCNetSDK.NET_DVR_StopRemoteConfig(lHandler)) {
                log.warn("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                return ResultData.error("下载人脸失败");
            } else {
                log.debug("NET_DVR_StopRemoteConfig接口成功");
            }
        }
        return ResultData.success("下载人脸成功", map);
    }

    /**
     * 以人为中心 添加卡
     *
     * @param employeeNo   人员 ID
     * @param cardNo       卡号
     * @param isDeleteCard 是否删除该卡
     * @param cardType     卡类型 默认 normalCard-普通卡
     */
    @Override
    public ResultData updateCard(
            String uuid, String employeeNo, String cardNo, boolean isDeleteCard, String cardType) {

        if (StrUtil.isBlank(cardType)) {
            cardType = "normalCard";
        }

        /*
        {
         "CardInfo" : {
           "employeeNo": "", //必填，string，人员 ID
           "cardNo": "1234567890", //必填，string，卡号
           "deleteCard": true, //可选，boolean，是否删除该卡，true-是（只有删除该卡时，才填写该字段；新增或修改卡时，不填写该字段）
           "cardType ": "normalCard", //必填，string，卡类型，normalCard-普通卡，patrolCard-巡更卡，hijackCard-胁迫卡，superCard-超级卡，dismissingCard-解除卡，emergencyCard-应急管理卡（用于授权临时卡权限，本身不能开门）
           }
         }
         */
        ResultData resultData = ResultData.error("下发卡失败");

        String strURL = "PUT /ISAPI/AccessControl/CardInfo/SetUp?format=json";
        HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(BYTE_ARRAY_LEN);
        System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
        ptrUrl.write();

        // 输入删除条件
        HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrInBuffer.read();

        JSONObject jsonData = new JSONObject();
        jsonData.set("employeeNo", employeeNo);
        jsonData.set("cardNo", cardNo);
        jsonData.set("deleteCard", isDeleteCard);
        jsonData.set("cardType", cardType);

        String strInbuffer = JSONUtil.createObj().set("CardInfo", jsonData).toString();
        ptrInBuffer.byValue = strInbuffer.getBytes();
        ptrInBuffer.write();

        HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        struXMLInput.read();
        struXMLInput.dwSize = struXMLInput.size();
        struXMLInput.lpRequestUrl = ptrUrl.getPointer();
        struXMLInput.dwRequestUrlLen = ptrUrl.byValue.length;
        struXMLInput.lpInBuffer = ptrInBuffer.getPointer();
        struXMLInput.dwInBufferSize = ptrInBuffer.byValue.length;
        struXMLInput.write();

        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(ISAPI_STATUS_LEN);
        ptrStatusByte.read();

        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(ISAPI_DATA_LEN);
        ptrOutByte.read();

        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        struXMLOutput.read();
        struXMLOutput.dwSize = struXMLOutput.size();
        struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
        struXMLOutput.dwOutBufferSize = ptrOutByte.size();
        struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        struXMLOutput.dwStatusSize = ptrStatusByte.size();
        struXMLOutput.write();

        int lUserID = lUserIDMap.get(uuid);
        if (!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput)) {
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            log.warn("NET_DVR_STDXMLConfig失败，错误号：" + iErr);
            return ResultData.error("NET_DVR_STDXMLConfig失败，错误号：" + iErr);

        } else {
            struXMLOutput.read();
            ptrOutByte.read();
            ptrStatusByte.read();
            String strOutXML = new String(ptrOutByte.byValue).trim();
            log.info("下发卡号输出结果:" + strOutXML);
            String strStatus = new String(ptrStatusByte.byValue).trim();
            log.info("下发卡号返回状态：" + strStatus);
            JSONObject jsonObject = new JSONObject(strOutXML);
            if (jsonObject.getInt("statusCode") == 1) {
                resultData = ResultData.success("下发卡号成功");
            }
            resultData.setData(strOutXML);
        }
        return resultData;
    }

    // 报防设防 zsj 2021-8-4 16:37:22

    /**
     * 车辆摄像枪手动抓拍
     *
     * @param uuid     登陆信息
     * @param filePath 文件路径
     */
    @Override
    public ResultData carManualSnap(String uuid, String filePath) {

        Map<String, Object> urlMap = new HashMap<>();
        // 登陆
        int lUserID = lUserIDMap.get(uuid);
        Byte channel = channelMap.get(uuid);
        if (lUserID == -1) {
            return null;
        }
        HCNetSDK.NET_DVR_WORKSTATE_V30 devwork = new HCNetSDK.NET_DVR_WORKSTATE_V30();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState_V30(lUserID, devwork)) {
            // 返回Boolean值，判断是否获取设备能力
            log.info("hksdk(抓图)-返回设备状态失败");
        }
        log.info("准备抓拍，userId:[{}],startChan:[{}],时间：[{}]", lUserID, channel, DateUtil.now());

        // 文件路径
        FileUtil.mkParentDirs(filePath);

        HCNetSDK.NET_DVR_MANUALSNAP struManualParam = new HCNetSDK.NET_DVR_MANUALSNAP();
        struManualParam.read();
        struManualParam.byOSDEnable = 0; // 抓拍图片上是否关闭OSD信息叠加：0- 不关闭(默认)，1- 关闭
        struManualParam.byChannel = 1;
        struManualParam.byLaneNo = 1; // 车道号，取值范围：1~6，默认为1
        struManualParam.write();

        HCNetSDK.NET_DVR_PLATE_RESULT struPlateResult = new HCNetSDK.NET_DVR_PLATE_RESULT();
        struPlateResult.read();
        struPlateResult.pBuffer1 = new Memory(2 * 1024 * 1024);
        struPlateResult.pBuffer2 = new Memory(2 * 1024 * 1024);
        struPlateResult.write();

        boolean res = hCNetSDK.NET_DVR_ManualSnap(lUserID, struManualParam, struPlateResult);
        struPlateResult.pBuffer1 = struPlateResult.getPointer();

        System.out.println(
                "图片长度：" + struPlateResult.dwPicLen + "\t车牌图片长度：" + struPlateResult.dwPicPlateLen);
        if (!res) { // 单帧数据捕获图片
            System.out.println("抓拍失败!" + " err: " + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("抓拍成功");
            struPlateResult.read();

            // 抓拍全图
            if (struPlateResult.byResultType == 1
                    && struPlateResult.dwPicLen > 0
                    && struPlateResult.pBuffer1 != null) {
                FileOutputStream fout;
                try {
                    fout = new FileOutputStream(filePath);
                    // 将字节写入文件
                    long offset = 0;
                    ByteBuffer buffers =
                            struPlateResult.pBuffer1.getByteBuffer(
                                    offset, struPlateResult.dwPicLen);
                    byte[] bytes = new byte[struPlateResult.dwPicLen];
                    buffers.rewind();
                    buffers.get(bytes);
                    fout.write(bytes);
                    fout.close();

                    urlMap.put("photoUrl", filePath);
                } catch (IOException e) {
                    log.error("抓拍失败" + e.getMessage());
                }
            }
        }

        // 车牌
        if (struPlateResult.byResultType == 1
                && struPlateResult.dwPicPlateLen > 0
                && struPlateResult.pBuffer2 != null) {
            FileOutputStream fout;
            try {
                filePath = filePath.replaceAll("\\\\", "/");
                filePath =
                        filePath.substring(0, filePath.lastIndexOf("/") + 1)
                                + "plate_"
                                + filePath.substring(filePath.lastIndexOf("/") + 1);
                fout = new FileOutputStream(filePath);
                // 将字节写入文件
                long offset = 0;
                ByteBuffer buffers =
                        struPlateResult.pBuffer2.getByteBuffer(
                                offset, struPlateResult.dwPicPlateLen);
                byte[] bytes = new byte[struPlateResult.dwPicPlateLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
                urlMap.put("platePhotoUrl", filePath);
            } catch (IOException e) {
                log.error("抓拍车牌失败" + e.getMessage());
            }
        }
        return ResultData.success(urlMap);
    }

    private void setCardTime(
            HCNetSDK.NET_DVR_CARD_RECORD struCardRecord,
            DateTime beginTimeDate,
            DateTime endTimeDate) {
        struCardRecord.struValid.struBeginTime.wYear = Convert.toShort(beginTimeDate.year());
        struCardRecord.struValid.struBeginTime.byMonth = (byte) (beginTimeDate.month() + 1);
        struCardRecord.struValid.struBeginTime.byDay = (byte) beginTimeDate.dayOfMonth();
        struCardRecord.struValid.struBeginTime.byHour = (byte) beginTimeDate.hour(true);
        struCardRecord.struValid.struBeginTime.byMinute = (byte) beginTimeDate.minute();
        struCardRecord.struValid.struBeginTime.bySecond = (byte) beginTimeDate.second();
        struCardRecord.struValid.struEndTime.wYear = Convert.toShort(endTimeDate.year());
        struCardRecord.struValid.struEndTime.byMonth = (byte) (endTimeDate.month() + 1);
        struCardRecord.struValid.struEndTime.byDay = (byte) endTimeDate.dayOfMonth();
        struCardRecord.struValid.struEndTime.byHour = (byte) endTimeDate.hour(true);
        struCardRecord.struValid.struEndTime.byMinute = (byte) endTimeDate.minute();
        struCardRecord.struValid.struEndTime.bySecond = (byte) endTimeDate.second();
    }

    /**
     * 设防
     *
     * @param uuid 登陆id
     */
    @Override
    public ResultData setupAlarmChan(String uuid) {
        Integer lUserID = lUserIDMap.get(uuid);
        Integer lAlarmHandle = lAlarmHandleMap.get(uuid);
        if (lUserID == null || lUserID == -1) { // 没有登录过
            return ResultData.error("请先登陆");
        } else { // 有登录信息
            if (lAlarmHandle != null && lAlarmHandle != -1) { // 有布防
                return ResultData.success("设备已布放");
            }
        }
        fMSFCallBack_V31Map.put(uuid, new FMSGCallBack_V31());
        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31Map.get(uuid), pUser)) {
            log.warn("设置回调函数失败!");
        }
        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        m_strAlarmInfo.byLevel = 1; // 智能交通布防优先级：0- 一等级（高），1- 二等级（中），2- 三等级（低）
        m_strAlarmInfo.byAlarmInfoType =
                1; // 智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
        m_strAlarmInfo.byDeployType = 1; // 布防类型(仅针对门禁主机、人证设备)：0-客户端布防(会断网续传)，1-实时布防(只上传实时数据)
        m_strAlarmInfo.write();
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
        if (lAlarmHandle == -1) {
            log.warn(uuid + "布防失败，错误号:" + hCNetSDK.NET_DVR_GetLastError());
            return ResultData.error("布防失败，错误号:" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            lAlarmHandleMap.put(uuid, lAlarmHandle); // 布防记录
            log.debug(uuid + "布防成功");
            return ResultData.success("布防成功");
        }
    }

    public void AlarmDataHandle(
            int lCommand,
            HCNetSDK.NET_DVR_ALARMER pAlarmer,
            Pointer pAlarmInfo,
            int dwBufLen,
            Pointer pUser) {
        try {
            String sAlarmType = "";
            String[] newRow = new String[3];
            // 报警时间
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] sIP;

            sAlarmType = "lCommand=0x" + Integer.toHexString(lCommand);
            // lCommand是传的报警类型
            switch (lCommand) {
                case HCNetSDK.COMM_ALARM_V40:
                    HCNetSDK.NET_DVR_ALARMINFO_V40 struAlarmInfoV40 =
                            new HCNetSDK.NET_DVR_ALARMINFO_V40();
                    getPointerData(struAlarmInfoV40, pAlarmInfo);

                    switch (struAlarmInfoV40.struAlarmFixedHeader.dwAlarmType) {
                        case 0:
                            struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(
                                    HCNetSDK.struIOAlarm.class);
                            struAlarmInfoV40.read();
                            sAlarmType =
                                    sAlarmType
                                            + "：信号量报警"
                                            + "，"
                                            + "报警输入口："
                                            + struAlarmInfoV40
                                            .struAlarmFixedHeader
                                            .ustruAlarm
                                            .struioAlarm
                                            .dwAlarmInputNo;
                            break;
                        case 1:
                            sAlarmType = sAlarmType + "：硬盘满";
                            break;
                        case 2:
                            sAlarmType = sAlarmType + "：信号丢失";
                            break;
                        case 3:
                            struAlarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(
                                    HCNetSDK.struAlarmChannel.class);
                            struAlarmInfoV40.read();
                            int iChanNum =
                                    struAlarmInfoV40
                                            .struAlarmFixedHeader
                                            .ustruAlarm
                                            .strualarmChannel
                                            .dwAlarmChanNum;
                            sAlarmType =
                                    sAlarmType
                                            + "：移动侦测"
                                            + "，"
                                            + "报警通道个数："
                                            + iChanNum
                                            + "，"
                                            + "报警通道号：";

                            for (int i = 0; i < iChanNum; i++) {
                                byte[] byChannel =
                                        struAlarmInfoV40.pAlarmData.getByteArray(i * 4L, 4);

                                int iChanneNo = 0;
                                for (int j = 0; j < 4; j++) {
                                    int ioffset = j * 8;
                                    int iByte = byChannel[j] & 0xff;
                                    iChanneNo = iChanneNo + (iByte << ioffset);
                                }

                                sAlarmType = sAlarmType + "+ch[" + iChanneNo + "]";
                            }

                            break;
                        case 4:
                            sAlarmType = sAlarmType + "：硬盘未格式化";
                            break;
                        case 5:
                            sAlarmType = sAlarmType + "：读写硬盘出错";
                            break;
                        case 6:
                            sAlarmType = sAlarmType + "：遮挡报警";
                            break;
                        case 7:
                            sAlarmType = sAlarmType + "：制式不匹配";
                            break;
                        case 8:
                            sAlarmType = sAlarmType + "：非法访问";
                            break;
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_ALARM_V30:
                    HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 =
                            new HCNetSDK.NET_DVR_ALARMINFO_V30();
                    getPointerData(strAlarmInfoV30, pAlarmInfo);
                    switch (strAlarmInfoV30.dwAlarmType) {
                        case 0:
                            sAlarmType =
                                    sAlarmType
                                            + "：信号量报警"
                                            + "，"
                                            + "报警输入口："
                                            + (strAlarmInfoV30.dwAlarmInputNumber + 1);
                            break;
                        case 1:
                            sAlarmType = sAlarmType + "：硬盘满";
                            break;
                        case 2:
                            sAlarmType = sAlarmType + "：信号丢失";
                            break;
                        case 3:
                            sAlarmType = sAlarmType + "：移动侦测" + "，" + "报警通道：";
                            for (int i = 0; i < 64; i++) {
                                if (strAlarmInfoV30.byChannel[i] == 1) {
                                    sAlarmType = sAlarmType + "ch" + (i + 1) + " ";
                                }
                            }
                            break;
                        case 4:
                            sAlarmType = sAlarmType + "：硬盘未格式化";
                            break;
                        case 5:
                            sAlarmType = sAlarmType + "：读写硬盘出错";
                            break;
                        case 6:
                            sAlarmType = sAlarmType + "：遮挡报警";
                            break;
                        case 7:
                            sAlarmType = sAlarmType + "：制式不匹配";
                            break;
                        case 8:
                            sAlarmType = sAlarmType + "：非法访问";
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_ALARM_RULE:
                    HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                    getPointerData(strVcaAlarm, pAlarmInfo);

                    switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
                        case 1:
                            sAlarmType =
                                    sAlarmType
                                            + "：穿越警戒面"
                                            + "，"
                                            + "_wPort:"
                                            + strVcaAlarm.struDevInfo.wPort
                                            + "_byChannel:"
                                            + strVcaAlarm.struDevInfo.byChannel
                                            + "_byIvmsChannel:"
                                            + strVcaAlarm.struDevInfo.byIvmsChannel
                                            + "_Dev IP："
                                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        case 2:
                            sAlarmType =
                                    sAlarmType
                                            + "：目标进入区域"
                                            + "，"
                                            + "_wPort:"
                                            + strVcaAlarm.struDevInfo.wPort
                                            + "_byChannel:"
                                            + strVcaAlarm.struDevInfo.byChannel
                                            + "_byIvmsChannel:"
                                            + strVcaAlarm.struDevInfo.byIvmsChannel
                                            + "_Dev IP："
                                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        case 3:
                            sAlarmType =
                                    sAlarmType
                                            + "：目标离开区域"
                                            + "，"
                                            + "_wPort:"
                                            + strVcaAlarm.struDevInfo.wPort
                                            + "_byChannel:"
                                            + strVcaAlarm.struDevInfo.byChannel
                                            + "_byIvmsChannel:"
                                            + strVcaAlarm.struDevInfo.byIvmsChannel
                                            + "_Dev IP："
                                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        default:
                            sAlarmType =
                                    sAlarmType
                                            + "：其他行为分析报警，事件类型："
                                            + strVcaAlarm.struRuleInfo.wEventTypeEx
                                            + "_wPort:"
                                            + strVcaAlarm.struDevInfo.wPort
                                            + "_byChannel:"
                                            + strVcaAlarm.struDevInfo.byChannel
                                            + "_byIvmsChannel:"
                                            + strVcaAlarm.struDevInfo.byIvmsChannel
                                            + "_Dev IP："
                                            + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    if (strVcaAlarm.dwPicDataLen > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            fout =
                                    new FileOutputStream(
                                            ".\\pic\\"
                                                    + new String(pAlarmer.sDeviceIP).trim()
                                                    + "wEventTypeEx["
                                                    + strVcaAlarm.struRuleInfo.wEventTypeEx
                                                    + "]_"
                                                    + newName
                                                    + "_vca.jpg");
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strVcaAlarm.pImage.getByteBuffer(
                                            offset, strVcaAlarm.dwPicDataLen);
                            byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
                    HCNetSDK.NET_DVR_PLATE_RESULT strPlateResult =
                            new HCNetSDK.NET_DVR_PLATE_RESULT();
                    getPointerData(strPlateResult, pAlarmInfo);
                    try {
                        String srt3 = new String(strPlateResult.struPlateInfo.sLicense, "GBK");
                        sAlarmType = sAlarmType + "：交通抓拍上传，车牌：" + srt3;
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    if (strPlateResult.dwPicLen > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            fout =
                                    new FileOutputStream(
                                            ".\\pic\\"
                                                    + new String(pAlarmer.sDeviceIP).trim()
                                                    + "_"
                                                    + newName
                                                    + "_plateResult.jpg");
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strPlateResult.pBuffer1.getByteBuffer(
                                            offset, strPlateResult.dwPicLen);
                            byte[] bytes = new byte[strPlateResult.dwPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_ITS_PLATE_RESULT:
                    HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult =
                            new HCNetSDK.NET_ITS_PLATE_RESULT();
                    getPointerData(strItsPlateResult, pAlarmInfo);
                    try {
                        String srt3 = new String(strItsPlateResult.struPlateInfo.sLicense, "GBK");
                        sAlarmType =
                                sAlarmType
                                        + ",车辆类型："
                                        + strItsPlateResult.byVehicleType
                                        + ",交通抓拍上传，车牌："
                                        + srt3;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    for (int i = 0; i < strItsPlateResult.dwPicNum; i++) {
                        if (strItsPlateResult.struPicInfo[i].dwDataLen > 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename =
                                        ".\\pic\\"
                                                + new String(pAlarmer.sDeviceIP).trim()
                                                + "_"
                                                + newName
                                                + "_type["
                                                + strItsPlateResult.struPicInfo[i].byType
                                                + "]_ItsPlate.jpg";
                                fout = new FileOutputStream(filename);
                                // 将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers =
                                        strItsPlateResult.struPicInfo[i].pBuffer.getByteBuffer(
                                                offset, strItsPlateResult.struPicInfo[i].dwDataLen);
                                byte[] bytes = new byte[strItsPlateResult.struPicInfo[i].dwDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case HCNetSDK.COMM_ALARM_PDC:
                    HCNetSDK.NET_DVR_PDC_ALRAM_INFO strPDCResult =
                            new HCNetSDK.NET_DVR_PDC_ALRAM_INFO();
                    getPointerData(strPDCResult, pAlarmInfo);

                    if (strPDCResult.byMode == 0) {
                        strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATFRAME.class);
                        sAlarmType =
                                sAlarmType
                                        + "：客流量统计，进入人数："
                                        + strPDCResult.dwEnterNum
                                        + "，离开人数："
                                        + strPDCResult.dwLeaveNum
                                        + ", byMode:"
                                        + strPDCResult.byMode
                                        + ", dwRelativeTime:"
                                        + strPDCResult.uStatModeParam.struStatFrame.dwRelativeTime
                                        + ", dwAbsTime:"
                                        + strPDCResult.uStatModeParam.struStatFrame.dwAbsTime;
                    }
                    if (strPDCResult.byMode == 1) {
                        strPDCResult.uStatModeParam.setType(HCNetSDK.NET_DVR_STATTIME.class);
                        String strtmStart =
                                String.format(
                                        "%04d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwYear)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwMonth)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwDay)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwHour)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwMinute)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmStart
                                                .dwSecond);
                        String strtmEnd =
                                String.format(
                                        "%04d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwYear)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwMonth)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwDay)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwHour)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwMinute)
                                        + String.format(
                                        "%02d",
                                        strPDCResult
                                                .uStatModeParam
                                                .struStatTime
                                                .tmEnd
                                                .dwSecond);
                        sAlarmType =
                                sAlarmType
                                        + "：客流量统计，进入人数："
                                        + strPDCResult.dwEnterNum
                                        + "，离开人数："
                                        + strPDCResult.dwLeaveNum
                                        + ", byMode:"
                                        + strPDCResult.byMode
                                        + ", tmStart:"
                                        + strtmStart
                                        + ",tmEnd :"
                                        + strtmEnd;
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(strPDCResult.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;

                case HCNetSDK.COMM_ITS_PARK_VEHICLE:
                    HCNetSDK.NET_ITS_PARK_VEHICLE strItsParkVehicle =
                            new HCNetSDK.NET_ITS_PARK_VEHICLE();
                    getPointerData(strItsParkVehicle, pAlarmInfo);
                    try {
                        String srtParkingNo =
                                new String(strItsParkVehicle.byParkingNo).trim(); // 车位编号
                        String srtPlate =
                                new String(strItsParkVehicle.struPlateInfo.sLicense, "GBK")
                                        .trim(); // 车牌号码
                        sAlarmType =
                                sAlarmType
                                        + ",停产场数据,车位编号："
                                        + srtParkingNo
                                        + ",车位状态："
                                        + strItsParkVehicle.byLocationStatus
                                        + ",车牌："
                                        + srtPlate;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    for (int i = 0; i < strItsParkVehicle.dwPicNum; i++) {
                        if (strItsParkVehicle.struPicInfo[i].dwDataLen > 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                String filename =
                                        ".\\pic\\"
                                                + new String(pAlarmer.sDeviceIP).trim()
                                                + "_"
                                                + newName
                                                + "_type["
                                                + strItsParkVehicle.struPicInfo[i].byType
                                                + "]_ParkVehicle.jpg";
                                fout = new FileOutputStream(filename);
                                // 将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers =
                                        strItsParkVehicle.struPicInfo[i].pBuffer.getByteBuffer(
                                                offset, strItsParkVehicle.struPicInfo[i].dwDataLen);
                                byte[] bytes = new byte[strItsParkVehicle.struPicInfo[i].dwDataLen];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case HCNetSDK.COMM_ALARM_TFS:
                    HCNetSDK.NET_DVR_TFS_ALARM strTFSAlarmInfo = new HCNetSDK.NET_DVR_TFS_ALARM();
                    getPointerData(strTFSAlarmInfo, pAlarmInfo);

                    try {
                        String srtPlate =
                                new String(strTFSAlarmInfo.struPlateInfo.sLicense, "GBK")
                                        .trim(); // 车牌号码
                        sAlarmType =
                                sAlarmType
                                        + "：交通取证报警信息，违章类型："
                                        + strTFSAlarmInfo.dwIllegalType
                                        + "，车牌号码："
                                        + srtPlate
                                        + "，车辆出入状态："
                                        + strTFSAlarmInfo.struAIDInfo.byVehicleEnterState;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(strTFSAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_ALARM_AID_V41:
                    HCNetSDK.NET_DVR_AID_ALARM_V41 struAIDAlarmInfo =
                            new HCNetSDK.NET_DVR_AID_ALARM_V41();
                    getPointerData(struAIDAlarmInfo, pAlarmInfo);
                    sAlarmType =
                            sAlarmType
                                    + "：交通事件报警信息，交通事件类型："
                                    + struAIDAlarmInfo.struAIDInfo.dwAIDType
                                    + "，规则ID："
                                    + struAIDAlarmInfo.struAIDInfo.byRuleID
                                    + "，车辆出入状态："
                                    + struAIDAlarmInfo.struAIDInfo.byVehicleEnterState;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(struAIDAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_ALARM_TPS_V41:
                    HCNetSDK.NET_DVR_TPS_ALARM_V41 struTPSAlarmInfo =
                            new HCNetSDK.NET_DVR_TPS_ALARM_V41();
                    getPointerData(struTPSAlarmInfo, pAlarmInfo);

                    sAlarmType =
                            sAlarmType
                                    + "：交通统计报警信息，绝对时标："
                                    + struTPSAlarmInfo.dwAbsTime
                                    + "，能见度:"
                                    + struTPSAlarmInfo.struDevInfo.byIvmsChannel
                                    + "，车道1交通状态:"
                                    + struTPSAlarmInfo.struTPSInfo.struLaneParam[0].byTrafficState
                                    + "，监测点编号："
                                    + new String(struTPSAlarmInfo.byMonitoringSiteID).trim()
                                    + "，设备编号："
                                    + new String(struTPSAlarmInfo.byDeviceID).trim()
                                    + "，开始统计时间："
                                    + struTPSAlarmInfo.dwStartTime
                                    + "，结束统计时间："
                                    + struTPSAlarmInfo.dwStopTime;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(struTPSAlarmInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT:
                    // 实时人脸抓拍上传
                    HCNetSDK.NET_VCA_FACESNAP_RESULT strFaceSnapInfo =
                            new HCNetSDK.NET_VCA_FACESNAP_RESULT();
                    getPointerData(strFaceSnapInfo, pAlarmInfo);
                    sAlarmType =
                            sAlarmType
                                    + "：人脸抓拍上传，人脸评分："
                                    + strFaceSnapInfo.dwFaceScore
                                    + "，年龄段："
                                    + strFaceSnapInfo.struFeature.byAgeGroup
                                    + "，性别："
                                    + strFaceSnapInfo.struFeature.bySex;
                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(strFaceSnapInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss"); // 设置日期格式
                    String time = df.format(new Date()); // new Date()为获取当前系统时间
                    // 人脸图片写文件
                    try {
                        FileOutputStream small =
                                new FileOutputStream(
                                        System.getProperty("user.dir")
                                                + "\\pic\\"
                                                + time
                                                + "small.jpg");
                        FileOutputStream big =
                                new FileOutputStream(
                                        System.getProperty("user.dir")
                                                + "\\pic\\"
                                                + time
                                                + "big.jpg");

                        if (strFaceSnapInfo.dwFacePicLen > 0) {
                            try {
                                small.write(
                                        strFaceSnapInfo.pBuffer1.getByteArray(
                                                0, strFaceSnapInfo.dwFacePicLen),
                                        0,
                                        strFaceSnapInfo.dwFacePicLen);
                                small.close();
                            } catch (IOException ex) {
                                log.error("实时人脸抓拍上传失败", ex);
                            }
                        }
                        if (strFaceSnapInfo.dwFacePicLen > 0) {
                            try {
                                big.write(
                                        strFaceSnapInfo.pBuffer2.getByteArray(
                                                0, strFaceSnapInfo.dwBackgroundPicLen),
                                        0,
                                        strFaceSnapInfo.dwBackgroundPicLen);
                                big.close();
                            } catch (IOException ex) {
                                log.error("实时人脸抓拍上传失败", ex);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        log.error("实时人脸抓拍上传失败", ex);
                    }
                    break;
                case HCNetSDK.COMM_SNAP_MATCH_ALARM:
                    // 人脸黑名单比对报警
                    HCNetSDK.NET_VCA_FACESNAP_MATCH_ALARM strFaceSnapMatch =
                            new HCNetSDK.NET_VCA_FACESNAP_MATCH_ALARM();
                    getPointerData(strFaceSnapMatch, pAlarmInfo);

                    if ((strFaceSnapMatch.dwSnapPicLen > 0)
                            && (strFaceSnapMatch.byPicTransType == 0)) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    System.getProperty("user.dir")
                                            + "\\pic\\"
                                            + newName
                                            + "_pSnapPicBuffer"
                                            + ".jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strFaceSnapMatch.pSnapPicBuffer.getByteBuffer(
                                            offset, strFaceSnapMatch.dwSnapPicLen);
                            byte[] bytes = new byte[strFaceSnapMatch.dwSnapPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if ((strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen > 0)
                            && (strFaceSnapMatch.byPicTransType == 0)) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    System.getProperty("user.dir")
                                            + "\\pic\\"
                                            + newName
                                            + "_struSnapInfo_pBuffer1"
                                            + ".jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strFaceSnapMatch.struSnapInfo.pBuffer1.getByteBuffer(
                                            offset, strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen);
                            byte[] bytes = new byte[strFaceSnapMatch.struSnapInfo.dwSnapFacePicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if ((strFaceSnapMatch.struBlockListInfo.dwBlockListPicLen > 0)
                            && (strFaceSnapMatch.byPicTransType == 0)) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    System.getProperty("user.dir")
                                            + "\\pic\\"
                                            + newName
                                            + "_fSimilarity_"
                                            + strFaceSnapMatch.fSimilarity
                                            + "_struBlackListInfo_pBuffer1"
                                            + ".jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strFaceSnapMatch.struBlockListInfo.pBuffer1.getByteBuffer(
                                            offset,
                                            strFaceSnapMatch.struBlockListInfo.dwBlockListPicLen);
                            byte[] bytes =
                                    new byte[strFaceSnapMatch.struBlockListInfo.dwBlockListPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    sAlarmType =
                            sAlarmType
                                    + "：人脸黑名单比对报警，相识度："
                                    + strFaceSnapMatch.fSimilarity
                                    + "，黑名单姓名："
                                    + new String(
                                    strFaceSnapMatch
                                            .struBlockListInfo
                                            .struBlockListInfo
                                            .struAttribute
                                            .byName,
                                    "GBK")
                                    .trim()
                                    + "，\n黑名单证件信息："
                                    + new String(
                                    strFaceSnapMatch
                                            .struBlockListInfo
                                            .struBlockListInfo
                                            .struAttribute
                                            .byCertificateNumber)
                                    .trim();

                    // 获取人脸库ID
                    byte[] FDIDbytes;
                    if ((strFaceSnapMatch.struBlockListInfo.dwFDIDLen > 0)
                            && (strFaceSnapMatch.struBlockListInfo.pFDID != null)) {
                        ByteBuffer FDIDbuffers =
                                strFaceSnapMatch.struBlockListInfo.pFDID.getByteBuffer(
                                        0, strFaceSnapMatch.struBlockListInfo.dwFDIDLen);
                        FDIDbytes = new byte[strFaceSnapMatch.struBlockListInfo.dwFDIDLen];
                        FDIDbuffers.rewind();
                        FDIDbuffers.get(FDIDbytes);
                        sAlarmType = sAlarmType + "，人脸库ID:" + new String(FDIDbytes).trim();
                    }
                    // 获取人脸图片ID
                    byte[] PIDbytes;
                    if ((strFaceSnapMatch.struBlockListInfo.dwPIDLen > 0)
                            && (strFaceSnapMatch.struBlockListInfo.pPID != null)) {
                        ByteBuffer PIDbuffers =
                                strFaceSnapMatch.struBlockListInfo.pPID.getByteBuffer(
                                        0, strFaceSnapMatch.struBlockListInfo.dwPIDLen);
                        PIDbytes = new byte[strFaceSnapMatch.struBlockListInfo.dwPIDLen];
                        PIDbuffers.rewind();
                        PIDbuffers.get(PIDbytes);
                        sAlarmType = sAlarmType + "，人脸图片ID:" + new String(PIDbytes).trim();
                    }
                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
                case HCNetSDK.COMM_ALARM_ACS: // 门禁主机报警信息
                    HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo =
                            new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
                    getPointerData(strACSInfo, pAlarmInfo);

                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, strACSInfo.struTime.dwYear);
                    cal.set(Calendar.MONTH, strACSInfo.struTime.dwMonth - 1);
                    cal.set(Calendar.DAY_OF_MONTH, strACSInfo.struTime.dwDay);
                    cal.set(Calendar.HOUR_OF_DAY, strACSInfo.struTime.dwHour);
                    cal.set(Calendar.MINUTE, strACSInfo.struTime.dwMinute);
                    cal.set(Calendar.SECOND, strACSInfo.struTime.dwSecond);
                    String dateTime = DateUtil.formatDateTime(cal.getTime());
                    String ipAndPort =
                            new String(pAlarmer.sDeviceIP).trim() + ":" + pAlarmer.wLinkPort;
                    String employeeNo;
                    if (strACSInfo.byAcsEventInfoExtend == 1) {
                        HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND pJavaStu =
                                new HCNetSDK.NET_DVR_ACS_EVENT_INFO_EXTEND();
                        getPointerData(pJavaStu, strACSInfo.pAcsEventInfoExtend);
                        employeeNo = new String(pJavaStu.byEmployeeNo).trim();
                    } else {
                        employeeNo = String.valueOf(strACSInfo.struAcsEventInfo.dwEmployeeNo);
                    }
                    sAlarmType =
                            sAlarmType
                                    + "：门禁主机报警信息"
                                    + "，事件流水："
                                    + strACSInfo.struAcsEventInfo.dwSerialNo
                                    + "，设备ip端口："
                                    + ipAndPort
                                    + "，日期："
                                    + dateTime
                                    + "，卡号："
                                    + new String(strACSInfo.struAcsEventInfo.byCardNo).trim()
                                    + "，人员编号："
                                    + employeeNo
                                    + "，卡类型："
                                    + strACSInfo.struAcsEventInfo.byCardType
                                    + "，报警主类型："
                                    + strACSInfo.dwMajor
                                    + "，报警次类型："
                                    + strACSInfo.dwMinor;
                    log.debug(sAlarmType);
                    MonitorEventEntity monitorEventEntity = new MonitorEventEntity();
                    monitorEventEntity.setSerialNo(strACSInfo.struAcsEventInfo.dwSerialNo);
                    monitorEventEntity.setAddress(ipAndPort);
                    monitorEventEntity.setDate(dateTime);
                    monitorEventEntity.setCardNo(
                            new String(strACSInfo.struAcsEventInfo.byCardNo).trim());
                    monitorEventEntity.setEmployeeNo(employeeNo);
                    monitorEventEntity.setDwMajor(strACSInfo.dwMajor);
                    monitorEventEntity.setDwMinor(strACSInfo.dwMinor);
                    monitorEventEntity.setCardType(strACSInfo.struAcsEventInfo.byCardType);
                    monitorEventEntity.setIp(new String(pAlarmer.sDeviceIP).trim());
                    monitorEventEntity.setPort(pAlarmer.wLinkPort);

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    String filePath = null;
                    if (strACSInfo.dwPicDataLen > 0 && StrUtil.isNotBlank(getEventFilePath())) // 寻找图片路径
                    {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_byCardNo["
                                            + new String(strACSInfo.struAcsEventInfo.byCardNo)
                                            .trim()
                                            + "_"
                                            + newName
                                            + "_Acs.jpg";
                            // 创建路径
                            filePath = getEventFilePath() + filename;
                            FileUtil.mkParentDirs(filePath);
                            fout = new FileOutputStream(filePath);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strACSInfo.pPicData.getByteBuffer(
                                            offset, strACSInfo.dwPicDataLen);
                            byte[] bytes = new byte[strACSInfo.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    monitorEventEntity.setFilePath(filePath);

                    // 记录事件
                    IHikEventCallBackHandle hikEventCallBackHandle = SpringUtil.getBean(IHikEventCallBackHandle.class);
                    if (hikEventCallBackHandle != null) {
                        hikEventCallBackHandle.handle(JSONUtil.toJsonStr(monitorEventEntity));
                    }
                    if (StrUtil.isNotBlank(getCallBackUrl())) {
                        try {
                            String post =
                                    HttpUtil.post(
                                            getCallBackUrl(), JSONUtil.toJsonStr(monitorEventEntity));
                            System.out.println("回调结果：" + post);
                        } catch (Exception e) {
                            log.error("门禁事件回调地址异常！" + e.getMessage());
                        }
                    }

                    break;
                case HCNetSDK.COMM_ID_INFO_ALARM: // 身份证信息
                    HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo =
                            new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
                    getPointerData(strIDCardInfo, pAlarmInfo);

                    sAlarmType =
                            sAlarmType
                                    + "：门禁身份证刷卡信息，身份证号码："
                                    + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim()
                                    + "，姓名："
                                    + new String(strIDCardInfo.struIDCardCfg.byName).trim()
                                    + "，报警主类型："
                                    + strIDCardInfo.dwMajor
                                    + "，报警次类型："
                                    + strIDCardInfo.dwMinor;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    // 身份证图片
                    if (strIDCardInfo.dwPicDataLen > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_byCardNo["
                                            + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim()
                                            + "_"
                                            + newName
                                            + "_IDInfoPic.jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strIDCardInfo.pPicData.getByteBuffer(
                                            offset, strIDCardInfo.dwPicDataLen);
                            byte[] bytes = new byte[strIDCardInfo.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // 抓拍图片
                    if (strIDCardInfo.dwCapturePicDataLen > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_byCardNo["
                                            + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim()
                                            + "_"
                                            + newName
                                            + "_IDInfoCapturePic.jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    strIDCardInfo.pCapturePicData.getByteBuffer(
                                            offset, strIDCardInfo.dwCapturePicDataLen);
                            byte[] bytes = new byte[strIDCardInfo.dwCapturePicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_UPLOAD_AIOP_VIDEO: // 设备支持AI开放平台接入，上传视频检测数据
                    HCNetSDK.NET_AIOP_VIDEO_HEAD struAIOPVideo = new HCNetSDK.NET_AIOP_VIDEO_HEAD();
                    getPointerData(struAIOPVideo, pAlarmInfo);

                    String strTime =
                            String.format("%04d", struAIOPVideo.struTime.wYear)
                                    + String.format("%02d", struAIOPVideo.struTime.wMonth)
                                    + String.format("%02d", struAIOPVideo.struTime.wDay)
                                    + String.format("%02d", struAIOPVideo.struTime.wHour)
                                    + String.format("%02d", struAIOPVideo.struTime.wMinute)
                                    + String.format("%02d", struAIOPVideo.struTime.wSecond)
                                    + String.format("%03d", struAIOPVideo.struTime.wMilliSec);

                    sAlarmType =
                            sAlarmType
                                    + "：AI开放平台接入，上传视频检测数据，通道号:"
                                    + struAIOPVideo.dwChannel
                                    + ", 时间:"
                                    + strTime;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    if (struAIOPVideo.dwAIOPDataSize > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_"
                                            + newName
                                            + "_AIO_VideoData.json";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    struAIOPVideo.pBufferAIOPData.getByteBuffer(
                                            offset, struAIOPVideo.dwAIOPDataSize);
                            byte[] bytes = new byte[struAIOPVideo.dwAIOPDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (struAIOPVideo.dwPictureSize > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_"
                                            + newName
                                            + "_AIO_VideoPic.jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    struAIOPVideo.pBufferPicture.getByteBuffer(
                                            offset, struAIOPVideo.dwPictureSize);
                            byte[] bytes = new byte[struAIOPVideo.dwPictureSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_UPLOAD_AIOP_PICTURE: // 设备支持AI开放平台接入，上传视频检测数据
                    HCNetSDK.NET_AIOP_PICTURE_HEAD struAIOPPic =
                            new HCNetSDK.NET_AIOP_PICTURE_HEAD();
                    getPointerData(struAIOPPic, pAlarmInfo);

                    String strPicTime =
                            String.format("%04d", struAIOPPic.struTime.wYear)
                                    + String.format("%02d", struAIOPPic.struTime.wMonth)
                                    + String.format("%02d", struAIOPPic.struTime.wDay)
                                    + String.format("%02d", struAIOPPic.struTime.wHour)
                                    + String.format("%02d", struAIOPPic.struTime.wMinute)
                                    + String.format("%02d", struAIOPPic.struTime.wSecond)
                                    + String.format("%03d", struAIOPPic.struTime.wMilliSec);

                    sAlarmType =
                            sAlarmType
                                    + "：AI开放平台接入，上传图片检测数据，通道号:"
                                    + new String(struAIOPPic.szPID)
                                    + ", 时间:"
                                    + strPicTime;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    if (struAIOPPic.dwAIOPDataSize > 0) {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + "_"
                                            + newName
                                            + "_AIO_PicData.json";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    struAIOPPic.pBufferAIOPData.getByteBuffer(
                                            offset, struAIOPPic.dwAIOPDataSize);
                            byte[] bytes = new byte[struAIOPPic.dwAIOPDataSize];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_ISAPI_ALARM: // ISAPI协议报警信息
                    HCNetSDK.NET_DVR_ALARM_ISAPI_INFO struEventISAPI =
                            new HCNetSDK.NET_DVR_ALARM_ISAPI_INFO();
                    getPointerData(struEventISAPI, pAlarmInfo);

                    sAlarmType =
                            sAlarmType
                                    + "：ISAPI协议报警信息, 数据格式:"
                                    + struEventISAPI.byDataType
                                    + ", 图片个数:"
                                    + struEventISAPI.byPicturesNumber;

                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];

                    SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
                    String curTime = sf1.format(new Date());
                    FileOutputStream foutdata;
                    try {
                        String jsonfilename =
                                ".\\pic\\"
                                        + new String(pAlarmer.sDeviceIP).trim()
                                        + curTime
                                        + "_ISAPI_Alarm_"
                                        + ".json";
                        foutdata = new FileOutputStream(jsonfilename);
                        // 将字节写入文件
                        ByteBuffer jsonbuffers =
                                struEventISAPI.pAlarmData.getByteBuffer(
                                        0, struEventISAPI.dwAlarmDataLen);
                        byte[] jsonbytes = new byte[struEventISAPI.dwAlarmDataLen];
                        jsonbuffers.rewind();
                        jsonbuffers.get(jsonbytes);
                        foutdata.write(jsonbytes);
                        foutdata.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < struEventISAPI.byPicturesNumber; i++) {
                        HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA struPicData =
                                new HCNetSDK.NET_DVR_ALARM_ISAPI_PICDATA();
                        struPicData.write();
                        Pointer pPicData = struPicData.getPointer();
                        pPicData.write(
                                0,
                                struEventISAPI.pPicPackData.getByteArray(
                                        (long) i * struPicData.size(), struPicData.size()),
                                0,
                                struPicData.size());
                        struPicData.read();

                        FileOutputStream fout;
                        try {
                            String filename =
                                    ".\\pic\\"
                                            + new String(pAlarmer.sDeviceIP).trim()
                                            + curTime
                                            + "_ISAPIPic_"
                                            + i
                                            + "_"
                                            + new String(struPicData.szFilename).trim()
                                            + ".jpg";
                            fout = new FileOutputStream(filename);
                            // 将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers =
                                    struPicData.pPicData.getByteBuffer(
                                            offset, struPicData.dwPicLen);
                            byte[] bytes = new byte[struPicData.dwPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    newRow[0] = dateFormat.format(today);
                    // 报警类型
                    newRow[1] = sAlarmType;
                    // 报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("解析事件失败", ex);
        }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {
        // 报警信息回调函数

        public boolean invoke(
                int lCommand,
                HCNetSDK.NET_DVR_ALARMER pAlarmer,
                Pointer pAlarmInfo,
                int dwBufLen,
                Pointer pUser) {
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }

    public class FMSGCallBack implements HCNetSDK.FMSGCallBack {
        // 报警信息回调函数

        public void invoke(
                int lCommand,
                HCNetSDK.NET_DVR_ALARMER pAlarmer,
                Pointer pAlarmInfo,
                int dwBufLen,
                Pointer pUser) {
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        }
    }
} // end
