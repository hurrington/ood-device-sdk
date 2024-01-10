package com.tygx.device.hik.constant;

/**
 * @title: AccessHikConstant
 * @description: 海康门禁设备常量
 * @author: zsj
 * @date: 2021/3/9 16:53
 * @updateBy: zsj
 * @updateTime: 2021/3/9 16:53
 * @company: tygx
 * @version: V1.0
 */
public class DeviceHikConstants {

    //人员类型
    public static final String MEMBER_TYPE_VISITOR = "visitor";//访客
    public static final String MEMBER_TYPE_NORMAL = "normal";//普通人员

    /**
     * 海康配置，数据类型
     */
    public static final int NET_DVR_JSON_CONFIG = 2550;
    public static final int NET_DVR_FACE_DATA_RECORD = 2551;
    public static final int NET_DVR_FACE_DATA_SEARCH = 2552;

    /**
     * 刷脸事件
     */
    public static final int DEVICE_EVENT_FACE_CHECK_DWMAJOR = 5;
    public static final int DEVICE_EVENT_FACE_CHECK_DWMINOR = 75;

    //事件时间类型
    public static final String DATE_TYPE_MONTH = "month";
    public static final String DATE_TYPE_DAY = "day";

    //门禁类型
    public static final String DEVICE_TYPE_CARD = "card";//以卡为中心
    public static final String DEVICE_TYPE_USER = "user";//以人为中心

    public static final String CACHE_KEY = "device:webcam:hik";

}
