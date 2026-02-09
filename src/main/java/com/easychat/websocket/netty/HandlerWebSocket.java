package com.easychat.websocket.netty;

import com.easychat.entity.dto.TokenUserInfoDto;
import com.easychat.entity.po.UserInfo;
import com.easychat.entity.query.UserInfoQuery;
import com.easychat.mappers.UserInfoMapper;
import com.easychat.redis.RedisComponent;
import com.easychat.redis.RedisUtils;
import com.easychat.utils.StringTools;
import com.easychat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName: HandlerWebSocket
 * @Description: 自定义websocket处理
 * @Author: 丁铭涛
 * @DateTime: 2025/6/1 15:07
 **/
@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChannelContextUtils channelContextUtils;

    private static final Logger logger = LoggerFactory.getLogger(HandlerWebSocket.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新的连接加入....");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有连接断开....");
        channelContextUtils.removeContext(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        //logger.info("收到用户userId{}消息{}", userId, textWebSocketFrame.text());
        redisComponent.saveHeartBeat(userId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            String token = getToke(url);
            if (token == null) {
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (null == tokenUserInfoDto) {
                ctx.channel().close();
                return;
            }

            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());
        }
    }

    private String getToke(String url) {
        if (StringTools.isEmpty(url) || url.indexOf("?") == -1) {
            return null;
        }
        String[] queryParams = url.split("\\?");
        if (queryParams.length != 2) {
            return null;
        }
        String[] params = queryParams[1].split("=");
        if (params.length != 2) {
            return null;
        }
        return params[1];
    }
}
