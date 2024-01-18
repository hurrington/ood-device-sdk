package com.ood.device.dh.entity;

import lombok.Data;

@Data
public class CardAddEntity {

    public String userId; // 用户ID
    public String cardNo; // 卡号

    /**
     * -1
     * 0  一般卡
     * 1  VIP卡
     * 2  来宾卡
     * 3  巡逻卡
     * 4  黑名单卡
     * 5  胁迫卡
     * 6  巡检卡
     * 0xff  母卡
     */
    public int emType; // 卡类型

    public CardAddEntity(String userId, String cardNo, int emType) {
        this.userId = userId;
        this.cardNo = cardNo;
        this.emType = emType;
    }

    public CardAddEntity() {
    }
}
