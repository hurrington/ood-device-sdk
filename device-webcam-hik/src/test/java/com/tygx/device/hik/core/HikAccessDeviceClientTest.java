package com.tygx.device.hik.core;

import com.ood.core.entity.ResultData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class HikAccessDeviceClientTest {

    IHikAccessDeviceClient hikClient;
    //登录信息
    String login;

    /**
     * 初始化
     */
    @Before
    public void initSdk() {
        hikClient = HikDeviceFactory.getAccessControlClient();

        //win
        ResultData init = hikClient.init("C:\\Users\\dbg\\Documents\\hik\\6.1.6.45\\HCNetSDK.dll");
        //linux
        // boolean init = hikClient.init("/usr/lib/hik/6.1.6.45/libhcnetsdk.so");
        Assert.assertTrue(init.isSuccess());
        ResultData loginResult = hikClient.login("admin", "tanklight123456", "192.168.65.160", 8000);
        Assert.assertEquals(loginResult.getData(), "192.168.65.160_8000_admin");
        login = (String) loginResult.getData();
    }

    /**
     * 设置SDK日志
     */
    @Test
    @Ignore
    public void setSdkLog() {
        String logPath = "C:\\logs";
        hikClient.setLogPath(logPath);
        hikClient.generateLogsRegularly();
    }

    /**
     * 设置SDK回调
     */
    @Test
    @Ignore
    public void setCallBack() {
        //监听回调转发http
        hikClient.setCallBackUrl("http://127.0.0.1/call");
        //回调图片（抓拍）
        hikClient.setEventFilePath("C:\\picture");
    }

    /**
     * 设置SDK
     */
    @Test
    @Ignore
    public void searchUsers() {
        ResultData resultData = hikClient.searchUser(login, null, 10, 1);
        Assert.assertTrue(resultData.isSuccess());
    }


    /**
     * 查找并下载人脸
     */
    @Test
    @Ignore
    public void testSearchFaceInfo() {
        String employeeNo = "1000";
        hikClient.searchUser(login, new String[]{employeeNo}, null, null);
        hikClient.searchFaceInfo(login, employeeNo, "C:\\Users\\dbg\\Desktop\\faceDownload");
    }

    /**
     * 删除单个人员
     */
    @Test
    @Ignore
    public void testDeleteOne() {
        String employeeNo = "1000";
        ResultData resultData = hikClient.delUser(login, employeeNo);
        Assert.assertTrue(resultData.isSuccess());
    }


    /**
     * 清空人员
     */
    @Test
    @Ignore
    public void testDeleteAll() {
        ResultData resultData = hikClient.clearUser(login);
        Assert.assertTrue(resultData.isSuccess());
    }

    /**
     * 添加人员和人脸
     */
    @Test
    @Ignore
    public void testAddUserFace() {
        String employeeNo = "ENGLISH01";
        ResultData addUserR = hikClient.addUser(login, employeeNo, "测试编码", null, null, null);
        ResultData addUserFaceR = hikClient.addUserFace(login, employeeNo, "C:\\Users\\dbg\\Desktop\\人脸.jpg");
        Assert.assertTrue("添加用户", addUserR.isSuccess());
        Assert.assertTrue("添加人脸", addUserFaceR.isSuccess());
    }

    /**
     * 获取门禁记录
     */
    @Test
    @Ignore
    public void testGetEventRecord() {
        ResultData eventRecord = hikClient.getEventRecord(login, "2023-05-11", "day");
        Assert.assertNotNull(eventRecord.getData());
    }

    /**
     * 开启监听和关闭
     */
    @Test
    @Ignore
    public void testSetupAlarmChan() throws InterruptedException {
        ResultData setupAlarmChan = hikClient.setupAlarmChan(login);
        Assert.assertTrue(setupAlarmChan.isSuccess());
        Thread.sleep(1000 * 60 * 5);
        ResultData closeAlarmChan = hikClient.closeAlarmChan(login);
        Thread.sleep(1000 * 60 * 5);
    }

    /**
     * 重启机器
     */
    @Test
    @Ignore
    public void testReboot() {
        ResultData reboot = hikClient.reboot(login);
        Assert.assertTrue(reboot.isSuccess());
    }

    /**
     * 开门
     */
    @Test
    @Ignore
    public void testOpenDoor() {
        ResultData resultData = hikClient.controlGateway(login, 1, 1);
        Assert.assertTrue(resultData.isSuccess());
    }

}
