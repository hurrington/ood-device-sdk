package com.ood.device.dh.core;
/**
 * 海康事件处理接口
 *
 * @author zsj
 */
public interface IDhEventCallBackHandle {

	/**
	 * 处理事件
	 * @param eventData 数据
	 */
	void handle(String eventData);
}
