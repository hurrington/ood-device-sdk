package com.ood.device.hik.core;

import com.ood.device.hik.entity.MonitorEventEntity;

/**
 * 海康事件处理接口
 *
 * @author zsj
 */
public interface IHikEventCallBackHandle {

	/**
	 * 处理事件
	 * @param eventData 数据，数据格式参考{@link MonitorEventEntity  }
	 */
	void handle(String eventData);
}
