package com.ood.device.face;

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
 * @author dbg
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
        config.setAppId("");
        config.setSdkKey("");
        config.setLibPath("");
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
        String face1 = arcsoftFaceEngine.extractFace(new File("")).getData().toString();
        String face2 = arcsoftFaceEngine.extractFace(new File("")).getData().toString();
        Assert.assertNotNull(face1);
        Assert.assertNotNull(face2);
        ResultData resultData = arcsoftFaceEngine.compareFace(face1, face2);
        Assert.assertTrue(resultData.isSuccess());
    }
}
