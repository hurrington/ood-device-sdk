package com.ood.device.dh.lib.enumeration;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 布控类型
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 20:21
 */
public class EM_NET_TRAFFIC_CAR_CONTROL_TYPE extends NetSDKLib.SdkStructure {

    /**
     *  其他
     */
    public static final int					NET_CAR_CONTROL_OTHER = 0;
    /**
     * "OverdueNoCheck"
     */
    public static final int					NET_CAR_CONTROL_OVERDUE_NO_CHECK = 1;
    /**
     * "BrigandageCar"
     */
    public static final int					NET_CAR_CONTROL_BRIGANDAGE_CAR = 2;
    /**
     * "Breaking"
     */
    public static final int					NET_CAR_CONTROL_BREAKING = 3;
    /**
     * "CausetroubleEscape"
     */
    public static final int					NET_CAR_CONTROL_CAUSETROUBLE_ESCAPE = 4;
    /**
     * "OverdueParking"
     */
    public static final int					NET_CAR_CONTROL_CAUSETROUBLE_OVERDUEPARKING = 5;
    /**
     * "CounterfeitPlateCar"
     */
    public static final int					NET_CAR_CONTROL_COUNTERFEI_PLATE_CAR = 6;
    /**
     * "FakePlateCar"
     */
    public static final int					NET_CAR_CONTROL_FAKE_PLATE_CAR = 7;
    /**
     * "FocalCar"
     */
    public static final int					NET_CAR_CONTROL_FOCAL_CAR = 8;
    /**
     * "GuaranteeCar"
     */
    public static final int					NET_CAR_CONTROL_GUARANTEE_CAR = 9;
    /**
     * "FollowCar"
     */
    public static final int					NET_CAR_CONTROL_FOLLOW_CAR = 10;
}
