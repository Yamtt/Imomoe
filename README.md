![readme_cover](image/readme_cover.png)

<div align="center">
    <h1>樱花动漫</h1>
    <p>
        <a href="https://codebeat.co/projects/github-com-skyd666-imomoe-master" style="text-decoration:none">
            <img src="https://codebeat.co/badges/63fb5d12-bece-4d8f-913e-5bcde2f01242" alt="codebeat badge"  />
        </a>
        <a href="https://github.com/SkyD666/Imomoe/actions" style="text-decoration:none">
            <img src="https://img.shields.io/github/workflow/status/SkyD666/Imomoe/Pre-Release" alt="GitHub Workflow Status"  />
        </a>
        <a href="https://github.com/SkyD666/Imomoe/releases/latest" style="text-decoration:none">
            <img src="https://img.shields.io/github/v/release/SkyD666/Imomoe?display_name=release" alt="GitHub release (latest by date)"/>
        </a>
        <a href="https://github.com/SkyD666/Imomoe/releases/latest" style="text-decoration:none" >
            <img src="https://img.shields.io/github/downloads/SkyD666/Imomoe/total" alt="GitHub all downloads"/>
        </a>
        <a href="https://img.shields.io/badge/Android-5.0%2B-brightgreen" style="text-decoration:none" >
            <img src="https://img.shields.io/badge/Android-5.0%2B-brightgreen" alt="Android version"/>
        </a>
        <a href="https://github.com/SkyD666/Imomoe/blob/master/LICENSE" style="text-decoration:none" >
            <img src="https://img.shields.io/github/license/SkyD666/Imomoe" alt="GitHub license"/>
        </a>
	</p>
    <p>
        樱花动漫第三方安卓Android客户端，免费开源，目的是学习Android开发。（仅支持Android 5及以上版本）
    </p>
    <p>
        <b>（Android 5用户请尽快升级到6及以上，后续可能会将最低支持版本提高为Android 6）</b>
    </p>
</div>

----
## Discord频道：https://discord.gg/MyaRtRGEzr

### 可以选择将自制数据源共享到[DataSourceRepository](https://github.com/SkyD666/DataSourceRepository)仓库，自制数据源/后端[帮助文档](doc/customdatasource/RV_ITEM.md)

## [>>必看安全说明<<](#安全说明)

## 特色功能

1. 支持显示**排行榜**
2. 支持显示**每日更新**的番剧
3. 支持**分类查看**动漫
4. 支持**双指缩放**、**移动**、**旋转**视频
5. 支持视频**投屏**到电视
5. 支持**WebDAV**备份恢复数据
6. 支持部分视频**显示**、**发送弹幕**（需要数据源支持弹幕）
7. 支持输入某站弹幕链接播放网络弹幕（例如<a href="https://api.bilibili.com/x/v1/dm/list.so?oid=97495910" target="_blank">https://api.bilibili.com/x/v1/dm/list.so?oid=97495910</a>）
8. 支持**缓存视频**到本地
9. 支持**追番**（数据保存在本地）
10. 支持显示**观看历史**记录
11. 支持显示**搜索历史**记录
12. 支持改变视频**播放速度**
13. 支持改变**视频**显示**比例**（16:9, 4:3, 全屏等）
14. 支持**本地记忆**视频**播放进度**
15. 支持**播放本地音视频**
16. ......

## 运行截图

![main](screenshot/main.jpg) ![anime_detail](screenshot/anime_detail.jpg)

![new_anime](screenshot/new_anime.jpg) ![everyday_anime](screenshot/everyday_anime.jpg) 

![search](screenshot/search.jpg) ![play](screenshot/play.jpg) 

![classify](screenshot/classify.jpg) ![rank](screenshot/rank.jpg)

![favorite](screenshot/favorite.jpg) ![history](screenshot/history.jpg) 

<img src="screenshot/player.png" alt="player" style="zoom:77%;" /> 

## 主要技术栈

- MVVM
- ViewModel
- Flow
- Kotlin Coroutine
- Hilt
- Room
- Jetpack Compose
- SplashScreen
- DiffUtil
- Retrofit
- OkHttp
- Jsoup
- ......

## 安全说明

**请勿**私自**传播APK**安装包，GitHub仓库为唯一长期仓库，**请仅在GitHub仓库下载安装包**，请勿下载来历不明的应用与ads包，谨防隐私泄露，谨防受骗！

### 已发现未知来源的APK

其APK可能经过未知修改，**存在风险**，建议**立即卸载**！

包括但不限于以下版本：

- 9.9.9至尊
- 9.9.9至尊II
- 9.9.9至尊终极版
- 9.9.9II
- 12.14妹妹专属版
- 1.2.2
- 1.2.3
- 果哥9.9.1706

若确实想定制，请**自行编译**源码生成APK，**而不是破解**已有的APK安装包！

## 应用主要权限说明

### 存储

1. 读取存储卡中的内容：缓存动漫功能需要读取本地存储卡中缓存的视频文件
2. 修改或删除存储卡中的内容：缓存动漫功能需要修改记录缓存信息的xml文件

### 位置信息

1. 访问大致、确切位置：Flurry SDK会通过地理位置校准报表数据准确性，提供基础反作弊能力

### 其它应用功能

2. 防止手机休眠：投屏到电视功能需要
3. 允许接收WLAN多播：投屏到电视功能需要

## 附加说明

App内不提供默认数据源，需要用户自行导入或从APP内的数据源商店下载使用。 

## 免责声明

1. 此软件**只提供数据显示**，**不提供原始数据**。
2. 此软件显示的所有内容，其**版权**均**归原作者**所有。
3. 此软件**仅可用作学习交流**，未经授权，**禁止用于其他用途**，请在下载**24小时内删除**。
4. 因使用此软件产生的版权问题，软件作者概不负责。

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=SkyD666/Imomoe)](https://star-history.com/)

## 许可证

使用此软件代码需**遵循以下许可证协议**

[**GNU General Public License v3.0**](LICENSE)

