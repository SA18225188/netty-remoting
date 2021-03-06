/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.shinnlove.netty.protocol.websocket.server;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

/**
 * WebSocket服务端处理器。
 *
 * @author shinnlove.jinsheng
 * @version $Id: WebSocketServerHandler.java, v 0.1 2018-06-29 下午1:23 shinnlove.jinsheng Exp $$
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger       logger = Logger.getLogger(WebSocketServerHandler.class
                                                 .getName());
    /** WebSocket协议握手 */
    private WebSocketServerHandshaker handshaker;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 第一次握手消息由HTTP协议承载，是一个HTTP消息
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }
        // WebSocket接入，消息类型是`WebSocketFrame`
        else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

        // 如果HTTP解码失败，返回HTTP异常
        if (!req.getDecoderResult().isSuccess()
            || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // 构造握手响应返回，本机测试
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
            "ws://localhost:8080/websocket", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            // `WebSocket`协议握手
            /**
             * 在handshake的时候动态加入了`websocket`协议所需的`newWebsocketDecoder`和`newWebSocketEncoder`编解码器，
             * 所以后续发来的`WebSocket`消息才能直接变成`WebSocketFrame`类型消息。
             * @see {@link WebSocketServerHandshaker#handshake(io.netty.channel.Channel, io.netty.handler.codec.http.FullHttpRequest, io.netty.handler.codec.http.HttpHeaders, io.netty.channel.ChannelPromise)}
             * 可以获取通道`pipeline`**动态添加**新的`handler`。
             */
            handshaker.handshake(ctx.channel(), req);
        }
    }

    /**
     * 处理`WebSocketFrame`类型消息。
     *
     * @param ctx       netty通道上下文
     * @param frame     WebSocketFrame类型消息
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // 判断是否是关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否是Ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息(BinaryWebSocketFrame)
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported",
                frame.getClass().getName()));
        }

        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("%s received %s", ctx.channel(), request));
        }
        ctx.channel().write(
            new TextWebSocketFrame(request + " , 欢迎使用Netty WebSocket服务，现在时刻："
                                   + new java.util.Date().toString()));
    }

    /**
     * 处理Http请求类型数据。
     * 
     * @param ctx       channel通道上下文
     * @param req       原始http请求
     * @param res       应答http响应(传入res的status是400，bad request，不支持http协议，所以肯定会被关闭)
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req,
                                         FullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // 异步输出响应
        ChannelFuture f = ctx.channel().writeAndFlush(res);

        // 如果是非Keep-Alive，关闭连接(传入res的status是400，bad request，不支持http协议，所以肯定会被关闭)
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}