package com.yumi.http.keepalive;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.ExecutionException;

public final class NettyClientFactory {
    private static final EventLoopGroup group = new NioEventLoopGroup();
    public static void shutdown() {
        group.shutdownGracefully();
    }
    public static void releaseChannel(Channel channel) throws Exception{
        channel.close();
    }
    public static Channel getChannel(String host, int port) {

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());
            return b.connect(host, port).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static class ClientInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpClientCodec());
            p.addLast(new HttpObjectAggregator(1048576));
            p.addLast(new ChunkedWriteHandler());
            p.addLast(new ClientHandler());
        }
    }

    public static class ClientHandler extends ChannelDuplexHandler {
        private final StringBuilder sb = new StringBuilder();
        DefaultPromise<String> stringDefaultPromise;

        public String getResult() throws Exception {
            return this.stringDefaultPromise.get();
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (null == stringDefaultPromise || stringDefaultPromise.isDone()) {
                stringDefaultPromise = new DefaultPromise<>(ctx.executor());
            }
            super.write(ctx, msg, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            boolean release = true;
            try {
                if (msg instanceof HttpObject) {
                    HttpObject imsg = (HttpObject) msg;
                    channelRead0(ctx, imsg);
                } else {
                    release = false;
                    ctx.fireChannelRead(msg);
                }
            } finally {
                if (release) {
                    ReferenceCountUtil.release(msg);
                }
            }
        }

        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                sb.append("STATUS: ").append(response.status()).append(" VERSION: ")
                        .append(response.protocolVersion()).append("\n");
                if (!response.headers().isEmpty()) {
                    for (CharSequence name: response.headers().names()) {
                        for (CharSequence value: response.headers().getAll(name)) {
                            sb.append("HEADER: ").append(name).append(" = ").append(value).append("\n");
                        }
                    }
                    sb.append("\n");
                }

                if (HttpUtil.isTransferEncodingChunked(response)) {
                    sb.append("CHUNKED CONTENT {\n");
                } else {
                    sb.append("CONTENT {\n");
                }
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                sb.append(content.content().toString(CharsetUtil.UTF_8)).append("\n");

                if (content instanceof LastHttpContent) {
                    sb.append("} END OF CONTENT\n");
                }
                stringDefaultPromise.setSuccess(sb.toString());
                sb.setLength(0);
            }
        }
    }
}
