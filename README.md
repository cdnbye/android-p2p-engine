**English | [简体中文](Readme_zh.md)**

<h1 align="center"><a href="" target="_blank" rel="noopener noreferrer"><img width="250" src="https://github.com/cdnbye/hlsjs-p2p-engine/blob/master/figs/cdnbye.png" alt="cdnbye logo"></a></h1>
<h4 align="center">Live/VOD P2P Engine for Android and Android TV</h4>
<p align="center">
<a href="https://android-arsenal.com/api?level=21"><img src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" alt="api"></a>
</p>

CDNBye Android P2P Engine scales live/vod video streaming by peer-to-peer network using bittorrent-like protocol. Powered by WebRTC Datachannel, this SDK can interconnect with the Web side [plug-in](https://github.com/cdnbye/hlsjs-p2p-engine) of CDNBye, which greatly increases the number of nodes in the P2P network, breaking the gap between the browser and mobile application. Merely a few lines of codes are required to quickly integrate into existing projects. As expected, it supports any Android player!

## Features
- Interconnect with CDNBye [hlsjs-p2p-engine](https://github.com/cdnbye/hlsjs-p2p-engine) and [ios-p2p-engine](https://github.com/cdnbye/ios-p2p-engine)
- Support live and VOD streams over HLS protocol(m3u8)
- Support encrypted HLS stream
- Support cache to avoid repeating the download of TS file
- Very easy to  integrate with an existing Android Mobile and Android TV project
- Support any Android player
- Efficient scheduling policies to enhance the performance of P2P streaming
- Highly configurable
- Use IP database to group up peers by ISP and regions

## Requirements
Android Version Support: Kitkat 4.4(API level >= 19)

## Integration
See [document](https://docs.cdnbye.com/#/en/android/usage)

## API and Configuration
See [API.md](https://docs.cdnbye.com/#/android/API)

## Issue & Feature Request
- If you found a bug, open an issue.
- If you have a feature request, open an issue.

## Related Projects
- [hlsjs-p2p-engine](https://github.com/cdnbye/hlsjs-p2p-engine) - Web Video Delivery Technology with No Plugins.
- [ios-p2p-engine](https://github.com/cdnbye/ios-p2p-engine) -  iOS Video P2P Engine for Any Player.

## FAQ
We have collected some [frequently asked questions](https://docs.cdnbye.com/#/en/FAQ). Before reporting an issue, please search if the FAQ has the answer to your problem.

## Contact Us
Email：service@cdnbye.com
