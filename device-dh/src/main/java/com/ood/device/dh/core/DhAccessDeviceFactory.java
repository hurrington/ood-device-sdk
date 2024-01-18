package com.ood.device.dh.core;

public class DhAccessDeviceFactory {


    public static IDhAccessDeviceClient getAccessDeviceClient() {
        return DhAccessDeviceClient.getInstance();
    }

}
