package com.ood.device.hik.core;

import cn.hutool.core.io.FileUtil;
import com.ood.core.entity.ResultData;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HikWebcamClientTest {

    IHikWebcamClient webcamClient;
    String login;

    /**
     * 初始化SDK
     */
    @Before
    public void setUp() {
        webcamClient = HikDeviceFactory.getWebcamClient();
        String dllPath = "C:\\Users\\dbg\\Documents\\hik\\6.1.6.45\\";
        ResultData init = webcamClient.init(dllPath + "HCNetSDK.dll", dllPath + "PlayCtrl.dll");
        Assert.assertNotNull("设备登录", init);
        login = (String) webcamClient.login("admin", "hc12345678", "192.168.65.20", 8000).getData();
        Assert.assertNotNull("设备登录", login);
    }

    /**
     * 关闭SDK
     */
    @After
    public void tearDown() {
        ResultData logout = webcamClient.logout(login);
        Assert.assertTrue("退出登录",logout.isSuccess());
        ResultData stop = webcamClient.stop();
        Assert.assertTrue("注销SDK",stop.isSuccess());
    }

    /**
     * 保存实时录像
     */
    @Test
    @Ignore
    public void saveRealData() {
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.mp4";
        ResultData saveRealData = webcamClient.saveRealData(login, filePath, 11*1000L);
        Assert.assertTrue("保存录像",saveRealData.isSuccess());
        ResultData resultData = webcamClient.stopRealPlay(login);
        Assert.assertTrue("退出登录",resultData.isSuccess());
    }

    /**
     * 定时抓拍
     */
    @Test
    @Ignore
    public void timedSnapshot(){
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.jpeg";
        ResultData resultData = webcamClient.timedSnapshot(login, filePath, 11*1000L);
        webcamClient.stopRealPlay(login);
        Assert.assertTrue("定时抓拍",resultData.isSuccess());
    }



    /**
     * 视频转码
     * @throws IOException IO异常
     * @throws InterruptedException 线程异常
     */
    @Test
    @Ignore
    public void ffmpeg() throws IOException, InterruptedException {
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.mp4";
        String outputPath = "C:\\Users\\dbg\\Desktop\\testHik00.mp4";
        //使用ffmpeg转码，到官网https://ffmpeg.org/download.html下载
        List<String> command = new ArrayList<String>();
        command.add("D:\\work\\ffmpeg-6.0-essentials_build\\bin\\ffmpeg");
        command.add("-i");
        command.add(filePath);
        command.add("-c:v");
        command.add("libx264");
        command.add("-mbd");
        command.add("0");
        command.add("-c:a");
        command.add("aac");
        command.add("-strict");
        command.add("-2");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-movflags");
        command.add("faststart");
        command.add(outputPath);
        Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
        Assert.assertTrue("转码进程失败", videoProcess.isAlive());
        //等待转码时间
        Thread.sleep(2000L);
        Assert.assertTrue("生成文件失败", FileUtil.exist(outputPath));
    }

    @Test
    @Ignore
    public void faceCheck() {
        //TODO 定时截图获取人脸比较实际
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.jpeg";
//        ResultData resultData = webcamClient.realPlay(login);
//        webcamClient.stopRealPlay(login);
//        Assert.assertTrue("视频流解释失败", resultData.isSuccess());


    }


}
