package com.tygx.device.hik.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HikWebcamClientTest {

    IHikWebcamClient webcamClient;
    String login;

    @Test
    @Ignore
    public void saveRealData() {
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.mp4";
        HashMap<String, Object> resultData = webcamClient.saveRealData(login, filePath, 11);
        webcamClient.stopRealPlay(login);
        Assert.equals(resultData.get("code"), 200);

    }

    @Test
    @Ignore
    public void ffmpeg() throws IOException {
        String filePath = "C:\\Users\\dbg\\Desktop\\testHik.mp4";
        String outputPath = "C:\\Users\\dbg\\Desktop\\testHik00.mp4";
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
        Console.log(videoProcess.isAlive());
        boolean exist = FileUtil.exist(outputPath);
        Console.log(exist);
    }

    @Before
    public void setUp() throws Exception {
        webcamClient = HikDeviceFactory.getWebcamClient();
        String dllPath = "C:\\Users\\dbg\\Documents\\hik\\6.1.6.45\\";
        webcamClient.init(dllPath + "HCNetSDK.dll", dllPath + "PlayCtrl.dll");
        login = (String) webcamClient.login("admin", "hc12345678", "192.168.65.20", 8000).getData();
    }

    @After
    public void tearDown() throws Exception {
    }
}
