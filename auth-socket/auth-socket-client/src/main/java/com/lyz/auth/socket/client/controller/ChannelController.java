package com.lyz.auth.socket.client.controller;

import com.lyz.auth.common.codec.util.ChannelContext;
import com.lyz.auth.common.util.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DESC:
 *
 * @author lyz
 * @version 1.0.0
 * @date 2023/3/25 22:47
 */
@Api(tags = "Netty客户端")
@ApiResponses(value = {
        @ApiResponse(code = 0, message = "成功"),
        @ApiResponse(code = 1, message = "失败")
})
@Slf4j
@RestController
@RequestMapping("/netty/client")
public class ChannelController {

    @ApiOperation("查询连接数量")
    @GetMapping("/channel/count")
    public Result<Integer> register() {
        return Result.success(ChannelContext.count());
    }
}
