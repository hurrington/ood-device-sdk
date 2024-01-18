package com.ood.device.dh.entity;

import lombok.Data;

@Data
public class PersonAddEntitiy {
    public String userId; // 用户ID
    public String userName; // 用户名
    public String passwd; // 密码
    public String roomNo; // 房间号
    public String startTime;
    public String endTime;
    public String memberType;

    /**
     *
     * @param userId 编号
     * @param userName 名称
     * @param passwd 密码
     * @param roomNo 房号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param memberType 人员类型
     */
    public PersonAddEntitiy(String userId, String userName, String passwd, String roomNo, String startTime, String endTime, String memberType) {
        this.userId = userId;
        this.userName = userName;
        this.passwd = passwd;
        this.roomNo = roomNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memberType = memberType;
    }

    public PersonAddEntitiy() {
    }
}
