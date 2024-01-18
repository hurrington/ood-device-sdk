package com.ood.device.dh.lib.structure;

import com.ood.device.dh.lib.NetSDKLib;

import static com.ood.device.dh.lib.NetSDKLib.MAX_INSIDEOBJECT_NUM;

/**
 * X光机关联图片类型
 *
 * @author ： 260611
 * @since ： Created in 2021/10/19 14:46
 */
public class DEV_EVENT_XRAY_DETECTION_INFO extends NetSDKLib.SdkStructure {

    /**
     *  通道号
     */
    public int					nChannelID;
    /**
     *  事件名称
     */
    public byte					szName[] = new byte[128];

    /**
     *  保留字节对齐
     */
    public byte					Reserved[] = new byte[4];

    /**
     *  时间戳(单位是毫秒)
     */
    public double					PTS;
    /**
     *  事件发生的时间
     */
    public NET_TIME_EX					UTC = new NET_TIME_EX();
    /**
     *  事件ID
     */
    public int					nEventID;
    /**
     *  所属大类
     */
    public int					emClassType;

    /**
     *  包裹信息
     */
    public NET_PACKAGE_INFO					stuPacketInfo = new NET_PACKAGE_INFO();

    /**
     *  保留字节对齐
     */
    public byte					Reserved1[] = new byte[4];

    /**
     *  主视角包裹内物品个数
     */
    public int					nObjectNum;
    /**
     *  主视角包裹内物品信息
     */
    public NET_INSIDE_OBJECT					stuInsideObj[] = (NET_INSIDE_OBJECT[])new NET_INSIDE_OBJECT().toArray(MAX_INSIDEOBJECT_NUM);

    /**
     *  从视角包裹内物品个数
     */
    public int					nSlaveViewObjectNum;
    /**
     *  从视角包裹内物品信息
     */
    public NET_INSIDE_OBJECT					stuSlaveViewInsideObj[] = (NET_INSIDE_OBJECT[])new NET_INSIDE_OBJECT().toArray(MAX_INSIDEOBJECT_NUM);

    /**
     *  图片数量
     */
    public int					nImageCount;
    /**
     *  图片信息
     */
    public NET_XRAY_IMAGE_INFO					stuImageInfo[] = (NET_XRAY_IMAGE_INFO[])new NET_XRAY_IMAGE_INFO().toArray(8);

    /**
     *  客户自定义信息个数
     */
    public int					nViewCustomInfoNum;
    /**
     *  客户自定义信息, X光机定制专用
     */
    public NetSDKLib.NET_XRAY_CUSTOM_INFO stuViewCustomInfo[] =(NetSDKLib.NET_XRAY_CUSTOM_INFO[])new NetSDKLib.NET_XRAY_CUSTOM_INFO().toArray(4);

    /**
     *  包裹标识, 用来唯一标识一个包裹
     */
    public byte					szPackageTag[] = new byte[32];

    /**
     *  包裹产生方式
     */
    public int					emPackageMode;

    /**
     *  关联图片数量
     */
    public int					nRelatedImageNum;
    /**
     *  关联图片
     */
    public NET_XRAY_RELATED_IMAGE_INFO					stuRelatedImageInfo[] = (NET_XRAY_RELATED_IMAGE_INFO[])new NET_XRAY_RELATED_IMAGE_INFO().toArray(8);
    /**
     *  与包裹关联的单号的个数
     */
    public int								nBarCodeCount;
    /**
     * 与包裹关联的单号的内容
     */
	public NET_BAR_CODE_INFO				stuBarCodeInfo[] =new NET_BAR_CODE_INFO[32];
	/**
	 * 事件公共扩展字段结构体
	 */
	public NET_EVENT_INFO_EXTEND	stuEventInfoEx = new NET_EVENT_INFO_EXTEND();

    /**
     *  保留字节,留待扩展
     */
    public byte					byReserved[] = new byte[372];

    public DEV_EVENT_XRAY_DETECTION_INFO() {
    	for (int i = 0; i < stuBarCodeInfo.length; i++) {
    		stuBarCodeInfo[i] = new NET_BAR_CODE_INFO();
		}
    }

}
