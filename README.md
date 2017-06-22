# easyvaas_Android
## 阅读对象
本文档面向所有使用易视云SDK的开发、测试人员，读者应该具有一定的Android编程开发经验

### 1 概述
易视云Android SDK包括Android推流端、播放端、消息交互三个部分。

* **Android推流端SDK**封装了Android Camera视频采集、麦克风音频采集、H.264编码、AAC音频编码、RTMP交织输出等一整套流程，支持分辨率、码率等编码参数可调节。
* **Android播放端SDK**，帮助开发者在Android平台上快速开发播放器应用。
* **Android消息交互SDK**封装了直播间消息互动流程，方便开发者快速实现直播间互动功能。

#### 1.1 功能特点
易视云Android SDK包含以下功能点：

* 实时水印
* 实时美颜
* 连麦互动
* 背景音乐播放
* 混音
* 静音功能
* 弱网络下码率自适应
* 网络异常情况下自动重连
* 秒开优化
* 多协议、多格式支持
* 消息互动

#### 1.2 运行环境
* 最低支持版本为Android 4.0 (API level 15)
* 支持的cpu架构：armv7, arm64

### 2 文档地址
易视云Android SDK在线文档地址：https://easyvaas.github.io/doc/chapter4/android.html ，介绍了SDK接入指南。
### 3 集成方式
* 推荐使用gradle方式集成：

```
//添加易视云maven库地址
allprojects {
		repositories {
			jcenter()
			maven { url 'https://git.yizhibo.tv/android/mvn-repo/raw/master' }
		}
}
	
compile 'com.easyvaas.sdk:evcore:1.1.6'
compile 'com.easyvaas.sdk:evlive:1.1.3'
compile 'com.easyvaas.sdk:evmessage:1.1.5'
compile 'com.easyvaas.sdk:evplayer:1.1.3'
```



