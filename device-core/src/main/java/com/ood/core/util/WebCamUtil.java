package com.ood.core.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络监控工具
 *
 * @author zsj
 */
public class WebCamUtil {

    public static boolean videoTranscoder(String ffmpeg, String inFile, String outFile) {
        try {
            List<String> command = new ArrayList<String>();
            command.add(ffmpeg);
            command.add("-i");
            command.add(inFile);
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
            command.add(outFile);
            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
        } catch (IOException e) {
            Log.get().error("视频转换失败", e);
            return false;
        }
        if (FileUtil.exist(outFile)) {
            return true;
        }
        return false;
    }
}
