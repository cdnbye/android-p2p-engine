**[English](README.md) | 简体中文**

<h1 align="center"><a href="" target="_blank" rel="noopener noreferrer"><img width="250" src="https://cdnbye.oss-cn-beijing.aliyuncs.com/pic/cdnbye.png" alt="cdnbye logo"></a></h1>
<h4 align="center">APP视频/直播、文件下载省流量&加速神器.</h4>
<p align="center">
<!--
<a href="https://bintray.com/cdnbye/maven/sdk/_latestVersion"><img src="https://api.bintray.com/packages/cdnbye/maven/sdk/images/download.svg?style=flat" alt="jcenter"></a>
-->
<a href="https://android-arsenal.com/api?level=19"><img src="https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat" alt="api"></a>
</p>

P2P技术使观看相同内容的用户之间可以相互分享数据，不仅能效降低视频/直播的带宽成本，还可以提升用户的播放体验，降低卡顿、二次缓存的发生率。本SDK传输能力基于WebRTC Datachannel，可以与CDNBye的[Web端插件](https://github.com/cdnbye/hlsjs-p2p-engine)互联互通，大大提高了P2P网络中的节点数量，打破了浏览器与移动端APP的隔阂，实现了真正意义上的全平台流媒体加速。只需要几行代码即可快速集成到现有项目中，配置自由度高，支持任何安卓播放器。

该插件的优势如下：
- 可与CDNBye Web端和[iOS端](https://github.com/cdnbye/ios-p2p-engine)SDK互联互通
- 支持基于HLS流媒体协议(m3u8)的直播和点播场景
- 支持加密HLS传输和防盗链技术
- 支持MP4点播场景
- 支持文件下载
- 支持文件缓存从而避免重复下载
- 支持任何安卓播放器
- 通过预加载形式实现P2P加速，完全不影响用户的播放体验
- 高可配置化，用户可以根据特定的使用环境调整各个参数
- 通过有效的调度策略来保证用户的播放体验以及p2p分享率
- Tracker服务器根据访问IP的ISP、地域和NAT类型等进行智能调度
- 支持基于安卓系统的手机和智能电视，完美兼容安卓机顶盒
- 已将WebRTC无用模块裁减掉，SDK体积不到2MB

## Demo
下载测试 [apk](https://cdnbye.oss-cn-beijing.aliyuncs.com/android_sdk/app-debug.apk)

## 系统要求
安卓4.4以上版本(API level >= 19)

## 集成方法
参照 [文档](https://www.cdnbye.com/cn/views/android/v2/usage.html)

## API文档
参照 [API.md](https://www.cdnbye.com/cn/views/android/v2/API.html)

## 反馈及意见
当你遇到任何问题时，可以通过在 GitHub 的 repo 提交 issues 来反馈问题，请尽可能的描述清楚遇到的问题，如果有错误信息也一同附带，并且在 Labels 中指明类型为 bug 或者其他。

## 客户案例
[<img src="https://cdnbye.oss-cn-beijing.aliyuncs.com/pic/dxxw.png" width="120">](https://sj.qq.com/myapp/detail.htm?apkName=com.hnr.dxxw)

## 相关项目
- [ios-p2p-engine](https://gitee.com/cdnbye/ios-p2p-engine) - iOS端P2P流媒体加速引擎。
- [flutter-p2p-engine](https://gitee.com/cdnbye/flutter-p2p-engine) - Flutter视频/直播APP省流量&加速神器, 由 [mjl0602](https://github.com/mjl0602) 贡献。
- [hlsjs-p2p-engine](https://gitee.com/cdnbye/hlsjs-p2p-engine) - 目前最好的Web端P2P流媒体方案。

## FAQ
我们收集了一些[常见问题](https://www.cdnbye.com/cn/views/FAQ.html)。在报告issue之前请先查看一下。

## 联系我们
邮箱：service@cdnbye.com
