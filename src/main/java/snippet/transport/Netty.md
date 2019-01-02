# 深入了解 Java-Netty高性能高并发理解

转发自<https://www.jianshu.com/p/ac7fb5c2640f>

## 一丶 Netty基础入门

Netty是一个高性能、异步事件驱动的NIO框架，它提供了对TCP、UDP和文件传输的支持，作为一个异步NIO框架，Netty的所有IO操作都是异步非阻塞的，通过Future-Listener机制，用户可以方便的主动获取或者通过通知机制获得IO操作结果。作为当前最流行的NIO框架，Netty在互联网领域、大数据分布式计算领域、游戏行业、通信行业等获得了广泛的应用，一些业界著名的开源组件也基于Netty的NIO框架构建。

## 二丶 Netty高性能之道

### RPC调用的性能模型分析

RPC 的全称是 Remote Procedure Call 是一种进程间通信方式。 它允许程序调用另一个地址空间（通常是共享网络的另一台机器上）的过程或函数，而不用程序员显式编码这个远程调用的细节。即程序员无论是调用本地的还是远程的函数，本质上编写的调用代码基本相同。
我们追溯下当初开发 RPC 的原动机是什么？在 Nelson 的论文 Implementing Remote Procedure Calls（参考[2]） 中他提到了几点：

简单：RPC 概念的语义十分清晰和简单，这样建立分布式计算就更容易。

高效：过程调用看起来十分简单而且高效。

通用：在单机计算中「过程」往往是不同算法部分间最重要的通信机制。

通俗一点说，就是一般程序员对于本地的过程调用很熟悉，那么我们把 RPC 做成和本地调用完全类似，那么就更容易被接受，使用起来毫无障碍。 Nelson 的论文发表于 30 年前，其观点今天看来确实高瞻远瞩，今天我们使用的 RPC 框架基本就是按这个目标来实现的。

### 传统RPC调用性能差的三大误区

网络传输方式问题：传统的RPC框架或者基于RMI等方式的远程服务（过程）调用采用了同步阻塞IO，当客户端的并发压力或者网络时延增大之后，同步阻塞IO会由于频繁的wait导致IO线程经常性的阻塞，由于线程无法高效的工作，IO处理能力自然下降。下面，我们通过BIO通信模型图看下BIO通信的弊端：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/BIO%E9%80%9A%E4%BF%A1%E6%A8%A1%E5%9E%8B%E5%9B%BE.jpeg)

BIO通信模型图

采用BIO通信模型的服务端，通常由一个独立的Acceptor线程负责监听客户端的连接，接收到客户端连接之后为客户端连接创建一个新的线程处理请求消息，处理完成之后，返回应答消息给客户端，线程销毁，这就是典型的一请求一应答模型。该架构最大的问题就是不具备弹性伸缩能力，当并发访问量增加后，服务端的线程个数和并发访问数成线性正比，由于线程是JAVA虚拟机非常宝贵的系统资源，当线程数膨胀之后，系统的性能急剧下降，随着并发量的继续增加，可能会发生句柄溢出、线程堆栈溢出等问题，并导致服务器最终宕机。

### 高性能的三大要素

1) 传输：用什么样的通道将数据发送给对方，BIO、NIO或者AIO，IO模型在很大程度上决定了框架的性能。

2) 协议：采用什么样的通信协议，HTTP或者内部私有协议。协议的选择不同，性能模型也不同。相比于公有协议，内部私有协议的性能通常可以被设计的更优。

3) 线程：数据报如何读取？读取之后的编解码在哪个线程进行，编解码后的消息如何派发，Reactor线程模型的不同，对性能的影响也非常大。

### 异步非阻塞通信

在IO编程过程中，当需要同时处理多个客户端接入请求时，可以利用多线程或者IO多路复用技术进行处理。IO多路复用技术通过把多个IO的阻塞复用到同一个select的阻塞上，从而使得系统在单线程的情况下可以同时处理多个客户端请求。与传统的多线程/多进程模型比，I/O多路复用的最大优势是系统开销小，系统不需要创建新的额外进程或者线程，也不需要维护这些进程和线程的运行，降低了系统的维护工作量，节省了系统资源。

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/NIO%E7%9A%84%E5%A4%9A%E8%B7%AF%E5%A4%8D%E7%94%A8%E6%A8%A1%E5%9E%8B%E5%9B%BE.jpeg)

NIO的多路复用模型图

与Socket类和ServerSocket类相对应，NIO也提供了SocketChannel和ServerSocketChannel两种不同的套接字通道实现。这两种新增的通道都支持阻塞和非阻塞两种模式。阻塞模式使用非常简单，但是性能和可靠性都不好，非阻塞模式正好相反。开发人员一般可以根据自己的需要来选择合适的模式，一般来说，低负载、低并发的应用程序可以选择同步阻塞IO以降低编程复杂度。但是对于高负载、高并发的网络应用，需要使用NIO的非阻塞模式进行开发。

### 零拷贝

零拷贝是Netty的重要特性之一，而究竟什么是零拷贝呢？

"Zero-copy" describes computer operations in which the CPU does not perform the task of copying data from one memory area to another.

从WIKI的定义中，我们看到“零拷贝”是指计算机操作的过程中，CPU不需要为数据在内存之间的拷贝消耗资源。而它通常是指计算机在网络上发送文件时，不需要将文件内容拷贝到用户空间（User Space）而直接在内核空间（Kernel Space）中传输到网络的方式。

Non-Zero Copy方式：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Non-Zero%20Copy%E6%96%B9%E5%BC%8F.jpeg)

Zero Copy方式

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Zero%20Copy%E6%96%B9%E5%BC%8F.jpeg)

从上图中可以清楚的看到，Zero Copy的模式中，避免了数据在用户空间和内存空间之间的拷贝，从而提高了系统的整体性能。Linux中的sendfile()以及Java NIO中的FileChannel.transferTo()方法都实现了零拷贝的功能，而在Netty中也通过在FileRegion中包装了NIO的FileChannel.transferTo()方法实现了零拷贝。

而在Netty中还有另一种形式的零拷贝，即Netty允许我们将多段数据合并为一整段虚拟数据供用户使用，而过程中不需要对数据进行拷贝操作，这也是我们今天要讲的重点。我们都知道在stream-based transport（如TCP/IP）的传输过程中，数据包有可能会被重新封装在不同的数据包中，例如当你发送如下数据时：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/%E5%8F%91%E9%80%81%E6%95%B0%E6%8D%AE.jpeg)

有可能实际收到的数据如下：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/%E5%AE%9E%E9%99%85%E6%94%B6%E5%88%B0%E6%95%B0%E6%8D%AE.jpeg)

因此在实际应用中，很有可能一条完整的消息被分割为多个数据包进行网络传输，而单个的数据包对你而言是没有意义的，只有当这些数据包组成一条完整的消息时你才能做出正确的处理，而Netty可以通过零拷贝的方式将这些数据包组合成一条完整的消息供你来使用。而此时，零拷贝的作用范围仅在用户空间中。

### 内存池

为什么要使用内存池？

随着JVM虚拟机和JIT即时编译技术的发展，对象的分配和回收是个非常轻量级的工作。但是对于缓冲区Buffer，情况却稍有不同，特别是对于堆外直接内存的分配和回收，是一件耗时的操作。而且这些实例随着消息的处理朝生夕灭，这就会给服务器带来沉重的GC压力，同时消耗大量的内存。为了尽量重用缓冲区，Netty提供了基于内存池的缓冲区重用机制。性能测试表明，采用内存池的ByteBuf相比于朝生夕灭的ByteBuf，性能高23倍左右（性能数据与使用场景强相关）。

如何启动并初始化内存池？

在Netty4或Netty5中实现了一个新的ByteBuf内存池，它是一个纯Java版本的 jemalloc （Facebook也在用）。现在，Netty不会再因为用零填充缓冲区而浪费内存带宽了。 不过，由于它不依赖于GC，开发人员需要小心内存泄漏。如果忘记在处理程序中释放缓冲区，那么内存使用率会无限地增长。 Netty默认不使用内存池，需要在创建客户端或者服务端的时候在引导辅助类中进行配置：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/work%E7%BA%BF%E7%A8%8B%E9%85%8D%E7%BD%AE.jpeg)

work线程配置

如何在自己的业务代码中使用内存池？

首先，介绍一下Netty的ByteBuf缓冲区的种类:ByteBuf支持堆缓冲区和堆外直接缓冲区，根据经验来说，底层IO处理线程的缓冲区使用堆外直接缓冲区，减少一次IO复制。业务消息的编解码使用堆缓冲区，分配效率更高，而且不涉及到内核缓冲区的复制问题。

ByteBuf的堆缓冲区又分为内存池缓冲区PooledByteBuf和普通内存缓冲区UnpooledHeapByteBuf。PooledByteBuf采用二叉树来实现一个内存池，集中管理内存的分配和释放，不用每次使用都新建一个缓冲区对象。UnpooledHeapByteBuf每次都会新建一个缓冲区对象。在高并发的情况下推荐使用PooledByteBuf，可以节约内存的分配。在性能能够保证的情况下，可以使用UnpooledHeapByteBuf，实现比较简单。

在此说明这是当我们在业务代码中要使用池化的ByteBuf时的方法：

第一种情况：若我们的业务代码只是为了将数据写入ByteBuf中并发送出去，那么我们应该使用堆外直接缓冲区DirectBuffer.使用方式如下：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/DirectBuffer.jpeg)

### 高效的Reactor线程模型

Reactor模式是事件驱动的，有一个或多个并发输入源，有一个Service Handler，有多个Request Handlers；这个Service Handler会同步的将输入的请求（Event）多路复用的分发给相应的Request Handler

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Request%20Handler1.jpeg)

从结构上，这有点类似生产者消费者模式，即有一个或多个生产者将事件放入一个Queue中，而一个或多个消费者主动的从这个Queue中Poll事件来处理；而Reactor模式则并没有Queue来做缓冲，每当一个Event输入到Service Handler之后，该Service Handler会立刻的根据不同的Event类型将其分发给对应的Request Handler来处理。

这个做的好处有很多，首先我们可以将处理event的Request handler实现一个单独的线程，即：

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Request%20Handler2.jpeg)

Request handler

线程这样Service Handler 和request Handler实现了异步，加快了service Handler处理event的速度，那么每一个request同样也可以以多线程的形式来处理自己的event,即Thread1 扩展成Thread pool 1,

Netty的Reactor线程模型1 Reactor单线程模型 Reactor机制中保证每次读写能非阻塞读写

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Reactor%E5%8D%95%E7%BA%BF%E7%A8%8B%E6%A8%A1%E5%9E%8B.jpeg)

Reactor单线程模型

一个线程(单线程)来处理CONNECT事件(Acceptor)，一个线程池（多线程）来处理read,一个线程池（多线程）来处理write,那么从Reactor Thread到handler都是异步的，从而IO操作也多线程化。

到这里跟BIO对比已经提升了很大的性能，但是还可以继续提升，由于Reactor Thread依然为单线程，从性能上考虑依然有所限制

### Reactor多线程模型

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/Reactor%E5%A4%9A%E7%BA%BF%E7%A8%8B%E6%A8%A1%E5%9E%8B.jpeg)

Reactor多线程模型、这样通过Reactor Thread Pool来提高event的分发能力

## 3 Reactor主从模型

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/%20Reactor%E4%B8%BB%E4%BB%8E%E6%A8%A1%E5%9E%8B.jpeg)

Netty的高效并发编程主要体现在如下几点：
1) volatile的大量、正确使用;
2) CAS和原子类的广泛使用；
3) 线程安全容器的使用；
4) 通过读写锁提升并发性能。

Netty除了使用reactor来提升性能，当然还有
1、零拷贝，IO性能优化
2、通信上的粘包拆包
3、同步的设计
4、高性能的序列
