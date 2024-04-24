# Springboot Netty脚手架

[![Build Status](https://img.shields.io/badge/Build-ZhiQinlsZhen-red)](https://github.com/ZhiQinIsZhen/spring-security-demo)
![Maven](https://img.shields.io/maven-central/v/org.apache.dubbo/dubbo.svg)
![License](https://img.shields.io/github/license/alibaba/dubbo.svg)
![Springboot Version](https://img.shields.io/badge/Springboot-2.7.18-brightgreen)
![Netty Version](https://img.shields.io/badge/Netty-4.1.109.Final-brightgreen)
![Swagger Version](https://img.shields.io/badge/knife4j-4.5.0-brightgreen)

---

## Auth-netty结构说明

### 项目结构

1. `auth-common`：Netty通用包
2. `auth-socket`：Socket通信服务，包含一个服务端，一个客户端
3. `auth-websocket`：Websocket推送服务，只有一个服务端

#### **auth-common**结构说明

1. `auth-common-codec`: socket通信自定义的编解码以及消息handler处理
> + AuthNettyEncode: Netty自定义编码器，基于长度头来进行编码的，参考RocketMQ
> + AuthNettyDecode: Netty自定义解码器，基于LengthFieldBasedFrameDecoder来进行解码的，参考RocketMQ
> + AuthChannelHandler: 消息处理Handler

2. `auth-common-util`: Netty工具包以及常量池
> + JsonMapperUtil: Jackson序列化工具
> + NettyToolUtil: Netty的Group Event的工具类

#### **auth-socket**结构说明
1. `auth-socket-client`: socket通信客户端
2. `auth-socket-server`: socket通信服务端

#### **auth-websocket**结构说明

1. `auth-websocket-server`: websocket推送服务端

---