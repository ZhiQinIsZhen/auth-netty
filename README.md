# Springboot Netty脚手架

[![Build Status](https://img.shields.io/badge/Build-ZhiQinlsZhen-red)](https://github.com/ZhiQinIsZhen/spring-security-demo)
![Maven](https://img.shields.io/maven-central/v/org.apache.dubbo/dubbo.svg)
![License](https://img.shields.io/github/license/alibaba/dubbo.svg)
![Springboot Version](https://img.shields.io/badge/Springboot-2.7.9-brightgreen)
![Netty Version](https://img.shields.io/badge/Netty-4.1.90.Final-brightgreen)
![Swagger Version](https://img.shields.io/badge/knife4j-2.0.9-brightgreen)

---

### Auth-netty结构说明
1.**auth-common-netty**: Netty的通用加框的封装以及工具类
> + AuthNettyEncode: Netty自定义编码器，基于长度头来进行编码的，参考RocketMQ
> + AuthNettyDecode: Netty自定义解码器，基于LengthFieldBasedFrameDecoder来进行解码的，参考RocketMQ
> + AuthChannelHandler: 业务处理Handler
> + ChannelContext: Netty channel上下文，可以查看具体连接数
> + NettyToolUtil: Netty的Group Event的工具类

2.**auth-client-netty**: Netty client端

3.**auth-server-netty**: Netty server端

4.**auth-server-websocket**: Netty WebSocket端

---