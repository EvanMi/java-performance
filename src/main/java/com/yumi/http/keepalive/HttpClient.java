package com.yumi.http.keepalive;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.net.URI;

public class HttpClient {
    private Channel channel;

    public String get(String requestUri, boolean keepAlive) throws Exception{
        URI uri = new URI("http://127.0.0.1:8080/");
        String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        Channel channelToUse;
        if (null != this.channel && this.channel.isActive()) {
            channelToUse = this.channel;
        } else {
            channelToUse = this.channel = NettyClientFactory.getChannel(host, port);
        }
        HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath() + "?name=lily&age=18");
        request.headers().set(HttpHeaderNames.HOST, host);
        if (!keepAlive) {
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        channelToUse.writeAndFlush(request);
        channelToUse.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).sync();
        NettyClientFactory.ClientHandler clientHandler = channelToUse.pipeline().get(NettyClientFactory.ClientHandler.class);
        return clientHandler.getResult();
    }

}
