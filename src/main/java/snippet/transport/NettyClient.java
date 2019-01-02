package snippet.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public void connect(int port, String host) throws Exception {
        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    // .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // Boss线程内存池配置
                    .handler(new ChannelInitializer<SocketChannel>() {
                        //ChannelInitializer<SocketChannel>匿名类初始化channel的pipeline，将客户端事务处理类加入到队列中
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 当代客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception {
    int port = 8080;
    if (args != null && args.length > 0) {
        try {
        port = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
        // 采用默认值
        }
    }
    new NettyClient().connect(port, "127.0.0.1");
    }
}