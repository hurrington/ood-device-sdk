package com.ood.device.face;

import cn.hutool.core.lang.Opt;
import com.ood.core.entity.ResultData;
import com.ood.device.face.constant.FaceEngineConstant;
import com.ood.device.face.core.ArcsoftFaceEngine;
import com.ood.device.face.entity.ArcsoftConfig;
import com.ood.device.face.utils.FaceEngineUtil;
import org.junit.*;

import java.io.File;

/**
 * 测试类
 *
 * @author 西某川
 * @date 2024/03/27
 */
public class FaceEngineTest {

    ArcsoftFaceEngine arcsoftFaceEngine;

    /**
     * 初始化
     */
    @Before
    public void initSdk() {
        arcsoftFaceEngine = (ArcsoftFaceEngine) FaceEngineUtil.getFaceEngine(FaceEngineConstant.TYPE_ARCSOFT);

        ArcsoftConfig config = new ArcsoftConfig();
        config.setAppId("***");
        config.setSdkKey("***");
        config.setLibPath("C:\\Users\\dbg\\Documents\\libs\\WIN64_2024");
        ResultData setConfig = arcsoftFaceEngine.setConfig(config);
        Assert.assertTrue("设置配置", setConfig.isSuccess());
        ResultData init = arcsoftFaceEngine.init();
        Assert.assertTrue("初始化", init.isSuccess());

    }

    @After
    public void closeSdk() {
        ResultData stop = arcsoftFaceEngine.unInit();
        Assert.assertTrue("注销SDK", stop.isSuccess());
    }


    /**
     * 设置SDK日志
     */
    @Test
    @Ignore
    public void compareFace() {
        ResultData face1R = arcsoftFaceEngine.extractFace(new File("C:\\Users\\dbg\\Pictures\\人脸2\\人脸.jpg"));
        String face1 = Opt.ofNullable(face1R.getData()).toString();
        ResultData face2R = arcsoftFaceEngine.extractFace(new File("C:\\Users\\dbg\\Pictures\\人脸2\\face2.jpg"));
        String face2 = Opt.ofNullable(face2R.getData()).toString();
        Assert.assertNotNull(face1);
        Assert.assertNotNull(face2);
        ResultData resultData = arcsoftFaceEngine.compareFace(face1, face2);
        Assert.assertTrue(resultData.isSuccess());
    }
}
