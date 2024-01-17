package com.ood.device.dh.core;

public class DhAccessDeviceFactory {


    public static IDhAccessDeviceClient getDhClient() {
        return DhAccessDeviceClient.getInstance();
    }

}
