package snippet.transport;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {

    /*
    boss线程和worker线程 ：
    可以这么说，ServerBootstrap监听的一个端口对应一个boss线程，它们一 一对应。比如你需要netty监听80和443端口，那么就会有两个boss线程分别负责处理来自两个端口的socket请求。在boss线程接受了socket连接求后，会产生一个channel（一个打开的socket对应一个打开的channel），并把这个channel交给ServerBootstrap初始化时指定的ServerSocketChannelFactory来处理，boss线程则继续处理socket的请求。 

    ServerSocketChannelFactory则会从worker线程池中找出一个worker线程来继续处理这个请求。 
    如果是OioServerSocketChannelFactory的话，那这个channel上所有的socket消息，从开始到channel（socket）关闭，都只由这个特定的worker来处理，也就是说一个打开的socket对应一个指定的worker线程，这个worker线程在socket没有关闭的情况下，也只能为这个socket处理消息，无法服务器他socket。 

    如果是NioServerSocketChannelFactory的话则不然，每个worker可以服务不同的socket或者说channel，worker线程和channel不再有一 一对应的关系。 
    所以，NioServerSocketChannelFactory只需要少量活动的worker线程就能很好的处理众多的channel，而OioServerSocketChannelFactory则需要与打开channel等量的worker线程来服务。 

    线程是一种资源，所以当netty服务器需要处理长连接的时候，最好选择NioServerSocketChannelFactory，这样可以避免创建大量的worker线程。在用作http服务器的时候，也最好选择NioServerSocketChannelFactory，因为现代浏览器都会使用http keepalive功能（可以让浏览器的不同http请求共享一个信道），这也是一种长连接。 
    */
    public void bind(int port) throws Exception {
        // 配置服务端的NIO，循环事件线程组。option是boss线程配置，childOption是work线程配置
        // 构造函数支持传入线程池，共享内存
        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();//配置类
            //NioServerSocketChannel作为channel类，它的功能对应于JDK NIO类库中的ServerSocketChannel
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT) // Boss线程内存池配置
                    .childHandler(new NettyServerHandler());//绑定事件处理类
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
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
        new NettyServer().bind(port);
    }

}