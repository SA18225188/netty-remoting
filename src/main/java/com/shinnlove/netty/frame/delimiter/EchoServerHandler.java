/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.shinnlove.netty.frame.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;

/**
 * 指定分隔符消息接收netty服务端处理器。
 *
 * @author shinnlove.jinsheng
 * @version $Id: EchoServerHandler.java, v 0.1 2018-06-29 下午1:35 shinnlove.jinsheng Exp $$
 */
@Sharable
public class EchoServerHandler extends ChannelHandlerAdapter {

    int counter = 0;

    /**
     * 服务端通道读取客户端通道写入信息并输出响应。
     *
     * 末尾直接writeAndFlush了，就不需要`channelReadComplete`方法了，一般write后在`channelReadComplete`中flush发送数据。
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("This is " + ++counter + " times receive client : [" + body + "]");
        body += "$_";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(echo);
    }

    /**
     * 发生异常的处理。
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();// 发生异常，关闭链路
    }

}