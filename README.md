# ood-device-sdk

#### 介绍
个人项目，设备接入量尚缺，可能会有兼容问题。
使用java集成和解决常见厂家设备对接，已解决海康大华sdk中的jna版本统一和兼容。
常见监控设备SDK接口工具，目前主要对接海康、大华，后续对接更多。
功能：
1. 海康
- 人脸机：卡片、人脸管理、打卡记录、抓拍、开门
- 监控：视频录制、人脸识别（简易版，开发中）、ffmpeg视频转码
2. 大华
- 人脸机：卡片、人脸管理、打卡记录、开门

#### 软件架构
引用lombok、hutool、jna、第三方库
lombok:1.18.30
hutool:5.8.25
jna-platform:5.13.0

#### 使用说明
1.  海康SDK，采用6.1.6.45版本，支持win64和linux64。通过HikDeviceUtil获取海康设备SDK
2.  大华SDK，采用3.057版本，支持win64和linux64。通过DhDeviceUtil获取大华设备SDK
3.  需要先使用调用init初始化SDK，再调用登录login，使用其他功能接口

更多设备调试可联系邮箱hurrington_z@foxmail.com
