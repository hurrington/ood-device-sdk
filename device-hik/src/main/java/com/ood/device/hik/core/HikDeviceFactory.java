package com.ood.device.hik.core;
/**
 * 海康设备
 * @author zsj
 */
public class HikDeviceFactory {

	public static IHikAccessDeviceClient getAccessControlClient() {
		return HikAccessDeviceClient.getInstance();
	}

	public static IHikWebcamClient getWebcamClient() {
		return HikWebcamClient.getInstance();
	}

}
