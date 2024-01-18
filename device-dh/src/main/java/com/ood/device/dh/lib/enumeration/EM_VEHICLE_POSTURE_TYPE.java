package com.ood.device.dh.lib.enumeration;

import com.ood.device.dh.lib.NetSDKLib;

/**
 * 车辆姿势
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 20:21
 */
public class EM_VEHICLE_POSTURE_TYPE extends NetSDKLib.SdkStructure {

    /**
     *  未知
     */
    public static final int					EM_VEHICLE_POSTURE_TYPE_UNKNOWN = 0;
    /**
     *  车头
     */
    public static final int					EM_VEHICLE_POSTURE_TYPE_VEHICLE_HEAD = 1;
    /**
     *  车侧
     */
    public static final int					EM_VEHICLE_POSTURE_TYPE_VEHICLE_SIDE = 2;
    /**
     *  车尾
     */
    public static final int					EM_VEHICLE_POSTURE_TYPE_VEHICLE_TAIL = 3;
}
