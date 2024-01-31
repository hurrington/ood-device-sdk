package com.ood.device.dh.core;

import com.ood.core.entity.ResultData;
import org.junit.*;

public class IDhAccessDeviceClientTest {

    public static final String WIN_DLL = "C:\\Users\\dbg\\Documents\\dh\\dh-win32-x86-64";
    public static final String LINUX_DLL = "/mnt/c/Users/dbg/Documents/dh/dh-linux-x86-64";
    private IDhAccessDeviceClient dhClient;
    private String login;

    /**
     * 初始化和登录
     */
    @Before
    public void setUp() {
        dhClient = DhAccessDeviceFactory.getAccessDeviceClient();
        //开启日志
//        dhClient.setLogPath("C:\\Users\\dbg\\Desktop");
        //初始化SDK
        ResultData init = dhClient.init(WIN_DLL + "\\dhnetsdk.dll", WIN_DLL + "\\dhconfigsdk.dll");
        Assert.assertTrue("初始化", init.isSuccess());
        //登录
        ResultData loginR = dhClient.login("admin", "pppppp000", "192.168.1.171", 37777);
        Assert.assertTrue("登录", loginR.isSuccess());
        login = (String) loginR.getData();
    }

    /**
     * 退出登录
     */
    @After
    public void tearDown() {
        ResultData logout = dhClient.logout(login);
        Assert.assertTrue("测试结束", logout.isSuccess());
        ResultData stop = dhClient.stop();
        Assert.assertTrue("注销SDK", stop.isSuccess());
    }

    @Test
    @Ignore
    public void controlGateway() {
        ResultData result = dhClient.controlGateway(login);
        Assert.assertTrue("远程开门", result.isSuccess());
    }

    @Test
    @Ignore
    public void setCallBackUrl() throws InterruptedException {
        ResultData result = dhClient.setupAlarmChan(login);
        Assert.assertTrue("开启回调设置", result.isSuccess());
        Thread.sleep(1000 * 60 * 5);
        ResultData resultData = dhClient.closeAlarmChan(login);
        Assert.assertTrue("关闭回调设置", resultData.isSuccess());
        Thread.sleep(1000 * 60 * 5);

    }

    @Test
    @Ignore
    public void addUser() {
        String employeeNo = "TEST001";
        ResultData result =
                dhClient.addUser(login, employeeNo, "测试员", "normal", null, null);
        Assert.assertTrue("新增用户", result.isSuccess());
    }

    @Test
    @Ignore
    public void searchUser() {
        String employeeNo = "TEST001";
        ResultData result1 =
                dhClient.searchUser(login, new String[]{employeeNo}, 1, 100);
        Assert.assertTrue("查询用户", result1.isSuccess());
    }

    @Test
    @Ignore
    public void addFace() {
        String employeeNo = "ENGLISH01";
        ResultData result2 =
                dhClient.addUserFace(login, employeeNo, "C:\\Users\\dbg\\Desktop\\face5.jpg");
        Assert.assertTrue("添加人脸", result2.isSuccess());
    }

    @Test
    @Ignore
    public void delUserFace() {
        String employeeNo = "ENGLISH01";
        ResultData result2 = dhClient.delUserFace(login, employeeNo);
        Assert.assertTrue("删除人脸", result2.isSuccess());
    }

    @Test
    @Ignore
    public void searchFaceInfo() {
        String employeeNo = "ENGLISH01";
        ResultData result2 =
                dhClient.searchFaceInfo(login, employeeNo, "C:\\Users\\dbg\\Desktop");
        Assert.assertTrue("查询人脸",result2.isSuccess());
    }

    @Test
    @Ignore
    public void reboot() {
        String employeeNo = "ENGLISH01";
        ResultData result2 = dhClient.reboot(login);
        Assert.assertTrue("重启",result2.isSuccess());
    }

    @Test
    @Ignore
    public void clearUser() {
        String employeeNo = "ENGLISH01";
        ResultData result2 = dhClient.clearUser(login);
        Assert.assertTrue("清空人员",result2.isSuccess());
    }

    @Test
    @Ignore
    public void manualSnap(){
        String filePath= "C:\\Users\\dbg\\Desktop\\testSnap.png";
        ResultData resultData = this.dhClient.manualSnap(login, filePath);
        Assert.assertTrue("抓拍",resultData.isSuccess());
    }


}
