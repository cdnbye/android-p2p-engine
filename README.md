**English | [简体中文](Readme_zh.md)**

<h1 align="center"><a href="" target="_blank" rel="noopener noreferrer"><img width="250" src="https://cdnbye.oss-cn-beijing.aliyuncs.com/pic/cdnbye.png" alt="cdnbye logo"></a></h1>
<h4 align="center">Live/VOD P2P Engine for Android and Android TV</h4>
<p align="center">
<!--
<a href="https://bintray.com/cdnbye/maven/sdk/_latestVersion"><img src="https://api.bintray.com/packages/cdnbye/maven/sdk/images/download.svg?style=flat" alt="jcenter"></a>
-->
<a href="https://android-arsenal.com/api?level=19"><img src="https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat" alt="api"></a>
</p>

CDNBye Android P2P Engine scales live/vod video streaming by peer-to-peer network using bittorrent-like protocol. Powered by WebRTC Datachannel, this SDK can interconnect with the Web side [plug-in](https://github.com/cdnbye/hlsjs-p2p-engine) of CDNBye, which greatly increases the number of nodes in the P2P network, breaking the gap between the browser and mobile application. Merely a few lines of codes are required to quickly integrate into existing projects. As expected, it supports any Android player!

## Features
- Interconnect with CDNBye [hlsjs-p2p-engine](https://github.com/cdnbye/hlsjs-p2p-engine) and [ios-p2p-engine](https://github.com/cdnbye/ios-p2p-engine)
- Support live and VOD streams over HLS protocol(m3u8)
- Support encrypted HLS stream
- Support cache to avoid repeating the download of TS file
- Support any Android player
- Efficient scheduling policies to enhance the performance of P2P streaming
- Highly configurable
- Use IP database to group up peers by ISP and regions
- Only use Datachannels and PeerConnection modules in WenRTC, which made smaller SDK size(< 2MB)
- API frozen, new releases will not break your code

## Requirements
Android Version Support: Kitkat 4.4(API level >= 19)

## Integration
See [document](https://p2p.cdnbye.com/en/views/android/usage.html)

## API and Configuration
See [API.md](https://p2p.cdnbye.com/en/views/android/API.html)

## Issue & Feature Request
- If you found a bug, open an issue.
- If you have a feature request, open an issue.

## They are using CDNBye
[<img src="https://cdnbye.oss-cn-beijing.aliyuncs.com/pic/dxxw.png" width="120">](https://sj.qq.com/myapp/detail.htm?apkName=com.hnr.dxxw)

## Related Projects
- [ios-p2p-engine](https://github.com/cdnbye/ios-p2p-engine) -  iOS Video P2P Engine for Any Player.
- [flutter-p2p-engine](https://github.com/cdnbye/flutter-p2p-engine) - Live/VOD P2P Engine for Flutter, contributed by [mjl0602](https://github.com/mjl0602).
- [hlsjs-p2p-engine](https://github.com/cdnbye/hlsjs-p2p-engine) - Web Video Delivery Technology with No Plugins.

## FAQ
We have collected some [frequently asked questions](https://p2p.cdnbye.com/en/views/FAQ.html). Before reporting an issue, please search if the FAQ has the answer to your problem.

## Contact Us
Email：service@cdnbye.com
<br>
Telegram: @cdnbye
<br>
Skype: live:86755838

## Join the Discussion
[Telegram Group](https://t.me/cdnbye_group)
