package com.ood.device.hik.entity;

import lombok.Data;

/**
 * 监听事件
 */
@Data
public class MonitorEventEntity {
    /**
     * 序号
     */
    private int serialNo;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private short port;
    /**
     * 地址 ip:端口
     */
    private String address;
    /**
     * 日期（机器时间）
     */
    private String date;
    /**
     * 卡号
     */
    private String cardNo;
    /**
     * 人员编号
     */
    private String employeeNo;
    /**
     * 事件号（主）
     */
    private int dwMajor;
    /**
     * 事件号（次）
     */
    private int dwMinor;
    /**
     * 卡类型
     */
    private int cardType;
    /**
     * 图片路径
     */
    private String filePath;
}
