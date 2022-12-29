package com.azhon.jtt808.netty;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.listener.OnConnectionListener;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class JTT808ClientLocal  {
    private static final String TAG = "JTT808ClientLocal";
    private String ip;
    private int port;
    private OnConnectionListener listener;
    private static JTT808ClientLocal jtt808Client = new JTT808ClientLocal();
    private Channel channel;
    private Bootstrap bootstrap;
    //心跳间隔 秒
    private static final int HEART_TIME = 15;
    private NioEventLoopGroup group;


    public static JTT808ClientLocal getInstance() {
        return jtt808Client;
    }

    /**
     * 初始化连接
     */
    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectServer();
            }
        }).start();
    }

    private void connectServer() {
        try {
            group = new NioEventLoopGroup();
            bootstrap = new Bootstrap()
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        ByteBuf delimiter = Unpooled.buffer(1);

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            delimiter.writeByte(0x7E);
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new DelimiterBasedFrameDecoder(65535, delimiter));
                            //写空闲30秒发送心跳
                            pipeline.addLast(new IdleStateHandler(60, HEART_TIME, 0));
                            pipeline.addLast(new JTT808Decoder());
                            pipeline.addLast(new JTT808Encoder());
                            pipeline.addLast(new JTT808HandlerLocal(JTT808ClientLocal.this, listener));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
            channelFuture.addListener(channelFutureListener);
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onLocalConnectionSateChange(OnConnectionListener.DIS_CONNECT);
            }
        }
    }

    /**
     * 发起重连
     */
    void reConnect() {
        try {
            Log.e(TAG, "与服务器发起重连... ip " + ip);
            if (bootstrap == null) return;
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
            channelFuture.addListener(channelFutureListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                final EventLoop loop = channelFuture.channel().eventLoop();
                loop.schedule(new Runnable() {
                    @Override
                    public void run() {
                        reConnect();
                        if (listener != null) {
                            listener.onLocalConnectionSateChange(OnConnectionListener.RE_CONNECT);
                        }
                    }
                }, 3, TimeUnit.SECONDS);
            } else {
                channel = channelFuture.channel();
            }
        }
    };

    /**
     * 设置服务器信息
     *
     * @param ip
     * @param port
     */
    public void setServerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * 设置连接监听
     *
     * @param listener
     */
    public void setConnectionListener(OnConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * 发送数据至服务器
     *
     * @param bean
     */
    public void writeAndFlush(JTT808Bean bean) {
        if (channel != null) {
            channel.writeAndFlush(bean.getData());
        }
    }

    /**
     * 主动断开连接
     */
    public void disconnect() {
        if (channel == null) return;
        channel.disconnect();
        channel.close();
        group.shutdownGracefully();
        group = null;
        bootstrap = null;
        channel = null;
    }
}