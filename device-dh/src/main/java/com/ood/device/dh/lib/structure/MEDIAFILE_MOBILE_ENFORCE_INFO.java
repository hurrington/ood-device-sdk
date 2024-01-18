package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;
import com.ood.device.dh.lib.enumeration.EM_MOBILE_ENFORCE_FILE_TYPE;
import com.ood.device.dh.lib.enumeration.EM_MOBILE_ENFORCE_FORMAT;

/**
 * 采集站和手持终端文件信息查询结果
 *
 * @author 47040
 * @since Created at 2021/5/31 12:09
 */
public class MEDIAFILE_MOBILE_ENFORCE_INFO extends NetSDKLib.SdkStructure {

	/**
	 * 结构体大小
	 */
	public int dwSize;
	/**
	 * 通道号从0开始,-1表示查询所有通道
	 */
	public int nChannelID;
	/**
	 * 开始时间
	 */
	public NET_TIME stuStartTime;
	/**
	 * 结束时间
	 */
	public NET_TIME stuEndTime;
	/**
	 * 文件路径
	 */
	public byte[] szFilePath = new byte[260];
	/**
	 * 如果查询条件中指定的时间在文件内，此处Length表示整个文件长度，而不表示指定时间段的长度，单位字节
	 */
	public int nLength;
	/**
	 * 对应文件的唯一标示
	 */
	public byte[] szUniqueID = new byte[128];
	/**
	 * 表示原始文件创建设备的序列号ID
	 */
	public byte[] szOriginalDeviceID = new byte[128];
	/**
	 * 文件的真实类型 枚举值 {@link EM_MOBILE_ENFORCE_FORMAT}
	 */
	public int emFormat;
	/**
	 * 文件后缀类型 枚举值 {@link EM_MOBILE_ENFORCE_FILE_TYPE}
	 */
	public int emFileType;
	/**
	 * 文件名称
	 */
	public byte[] szFileName = new byte[256];

	/**
	 * 录像文件附加信息
	 */
	public byte[] szExtraInfos = new byte[256];

	/**
	 * 文件采集时间
	 */
	public NET_TIME stuCollectTime;

	/**
	 * 文件完整性校验ID
	 */
	public byte[] szIntegrityCheckID = new byte[128];

	/**
	 * 为TRUE表示仅stuStartTimeRealUTC和stuEndTimeRealUTC有效(仅使用stuStartTimeRealUTC和stuEndTimeRealUTC),
	 * 为FALSE表示仅stuStartTime和stuEndTime有效(仅使用stuStartTime和stuEndTime)
	 */
	public int bRealUTC;

	/**
	 * UTC开始时间(标准UTC时间), 与stuEndTimeRealUTC配对使用
	 */
	public NET_TIME stuStartTimeRealUTC;

	/**
	 * UTC结束时间(标准UTC时间), 与stuStartTimeRealUTC配对使用
	 */
	public NET_TIME stuEndTimeRealUTC;

	/**
	 * 文件UTC采集时间(标准UTC时间)
	 */
	public NET_TIME stuCollectTimeRealUTC;

	public MEDIAFILE_MOBILE_ENFORCE_INFO() {
		dwSize = this.size();
	}
}
