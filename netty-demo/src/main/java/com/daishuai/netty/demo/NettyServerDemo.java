package com.daishuai.netty.demo;

import com.daishuai.netty.handler.ServerInboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName NettyServerDemo
 * @Author daishuai
 * @Date 2022/3/3 20:52
 * @Version 1.0
 */
@Slf4j
public class NettyServerDemo {

    public static void main(String[] args) {

        /**
         * 1、创建服务端Channel
         * bind()[用户代码入口]
         * -> initAndRegister()[初始化斌注册]
         * -> newChannel()[创建服务端Channel]
         * 反射创建服务端Channel
         * newSocket()[通过jdk来创建底层jdk Channel]
         * -> NioServerSocketChannelConfig()[tcp参数配置类]
         * -> AbstractNioChannel()
         *      -> configureBlocking(false)[非阻塞模式]
         *      -> AbstractChannel()[创建id, unsafe, pipeline]
         * 2、初始化服务端Channel
         * init()[初始化入口]
         *      -> set ChannelOptions, ChannelAttrs
         *      -> set ChildOptions, ChildAttrs
         *      -> config pipeline[配置服务端Pipeline]
         *      -> add ServerBootstrapAcceptor[添加连接器]
         * 3、注册Selector
         * AbstractChannel.register(channel)[入口]
         *      -> this.eventLoop = eventLoop[绑定线程]
         *      -> register0()[实际注册]
         *          -> doRegister()[调用jdk底层注册]
         *          -> invokeHandlerAddedIfNeeded()
         *          -> fireChannelRegistered()[传播事件]
         *
         * 4、NioEventLoop创建
         * new NioEventLoopGroup()[线程组，默认2*cpu]
         *      -> new ThreadPerTaskExecutor()[线程创建器]
         *          -> 每次执行任务都会创建一个线程实体
         *          -> NioEventLoop线程命名规则nioEventLoop-1-xx
         *      -> for(){ newChild() }[构造NioEventLoop]
         *          -> 保存线程执行器ThreadPerTaskExecutor
         *          -> 创建一个MpscQueue
         *          -> 创建一个selector
         *      -> chooserFactory.newChooser()[线程选择器]
         * 5、NioEventLoop启动
         * bind() -> execute(task)[入口]
         *      -> startThread() -> doStartThread()[创建线程]
         *          -> ThreadPerTaskExecutor.execute()
         *              -> thread = Thread.currentThread()
         *              NioEventLoop.run()[启动]
         * 6、NioEventLoop.run()
         * run() -> for(;;)
         *      -> select()[检查是否有io事件]
         *          -> deadline以及任务穿插逻辑处理
         *          -> 阻塞式select
         *          -> 避免jdk空轮训的bug
         *      -> processSelectedKeys()[处理io事件]
         *          -> selected keySet优化
         *          -> processSelectedKeysOptimized()
         *      -> runAllTasks()[处理异步任务队列]
         *          -> task的分类和添加
         *          -> 任务的聚合
         *          -> 任务的执行
         *
         * 7、Netty新链接接入处理逻辑
         *  -> 检测新链接
         *      -> processSelectedKey(key, channel)[入口]
         *          -> NioMessageUnsafe.read()
         *              -> doReadMessages()[while循环]
         *                  -> javaChannel().accept()
         *  -> 创建NioSocketChannel
         *      -> new NioSocketChannel(parent, ch)[入口]
         *          -> AbstractNioByteChannel(p, ch, op_read)
         *              -> configureBlocking(false) & save op
         *              -> create id, unsafe, pipeline
         *          -> new NioSocketChannelConfig()
         *              -> setTcpNoDelay(true)禁止Nagle算法
         *  -> 分配线程及注册selector
         *  -> 向selector注册读事件
         *
         * 8、服务端Channel的Pipeline构成
         *  Head
         *  -> ServerBootstrapAcceptor
         *      -> 添加childHandler
         *      -> 设置options和attrs
         *      -> 选择NioEventLoop并注册selector
         *  -> Tail
         *
         * 9、Pipeline初始化
         *  Pipeline在创建Channel的时候初始化
         *  Pipeline节点数据结构：ChannelHandlerContext
         *  Pipeline中的两大哨兵：Head和Tail
         *
         * 10、添加ChannelHandler
         *  判断是否重复添加
         *  创建节点并添加至链表
         *  回调添加完成事件
         *
         * 11、删除ChannelHandler
         *  找到节点
         *  链表的删除
         *  回调删除Handler事件
         *
         * 12、inBound事件的传播  Head -> Tail
         *  何为inBound事件一级ChannelInBoundHandler
         *  ChannelRead事件的传播
         *  SimpleInBoundHandler处理器
         *
         * 13、outBound事件的传播 Tail -> Head
         *  何为outBound事件以及ChannelOutBoundHandler
         *  write()事件的传播
         *
         * 14、异常的传播 发生异常的节点 -> Tail
         *  异常的触发链
         *  异常处理的最佳实践
         *      在Pipeline最后添加一个全局异常处理节点
         *
         * 15、ByteBuf
         *  内存的类别有哪些
         *  如何减少多线程内存分配之间的竞争
         *  不同大小内存是如何进行分配的
         *
         * 16、ByteBuf结构以及重要的API
         *  ByteBuf结构
         *      0 <= readerIndex <= writerIndex <= capacity
         *  read, write, set方法
         *  mark和reset方法
         *
         * 17、ByteBuf分类
         *  Pooled（预先分配好的内存中分配）和Unpooled（直接调用系统接口分配）
         *  Unsafe和非Unsafe
         *  Heap（在堆上进行分配的）和Direct（Jdk底层接口分配 ）
         *
         * 18、ByteBufAllocator分析
         *  ByteBufAllocator功能
         *  AbstractByteBufAllocator
         *  ByteBufAllocator两大子类
         *      UnpooledByteBufAllocator
         *      PooledByteBufAllocator
         *          -> 拿到线程局部缓存PoolThreadCache
         *          -> 在线程局部缓存的Area上进行内存分配
         *              -> directArena分配direct内存的流程
         *              -> 从缓存上进行内存分配
         *              -> 从内存堆里面进行内存分配
         * 19、内存规格介绍
         *  tiny     0-512B
         *  small    512B-8K
         *  normal   8K-16M
         *  huge     >16M
         *  16M    Chunk（内存是以Chunk为单位向操作系统共申请）
         *  8K     Page（以Page为单位对Chunk进行切分）
         *  0-8K   SubPage（以SubPage对Page进行切分）
         *
         * 20、命中缓存的分配逻辑
         *  MemoryRegionCache   queue, sizeClass, size
         *  tiny[32]: queue中大小是16B的整数倍
         *  small[4]:  512B 1K 2K 4K
         *  normal[3]: 8K 16K 32K
         *
         * 21、命中缓存的分配流程
         *  找到对应size的MemoryRegionCache
         *  从queue中弹出一个entry给ByteBuf初始化
         *  将弹出的entry扔到对象池进行复用
         *
         * 22、page级别的内存分配：allocateNormal()
         *  尝试在现有的chunk上分配
         *  创建一个chunk进行分配
         *  初始化PooledByteBuf
         *
         * 23、subPage级别的内存分配：allocateTiny()
         *   定位一个SubPage对象
         *   初始化SubPage
         *   初始化PooledByteBuf
         *
         * 24、ByteBuf的释放
         *  连续的内存区段加到缓存
         *  标记连续的内存区段为未使用
         *  ByteBuf加到对象池
         *
         * 25、ByteToMessageDecoder解码器
         *  累加字节流
         *  调用子类的decode方法进行解析
         *  将解析到的ByteBuf向下传播
         *
         * 26、编码器：writeAndFlush()
         *  从Tail节点开始往前传播
         *  逐个调用ChannelHandler的write方法
         *  逐个调用ChannelHandler的flush方法
         *
         * 27、编码器处理逻辑：MessageToByteEncoder
         *  匹配对象 -> 分配内存 -> 编码实现
         *  -> 释放对象 -> 传播数据 -> 释放内存
         *
         * 28、write-写buffer队列
         *  direct化ByteBuf
         *  插入写队列
         *  设置写状态
         *
         * 29、flush-刷新buffer队列
         *  添加刷新标志并设置写状态
         *  遍历buffer队列，过滤ByteBuf
         *  调用jdk底层api进行自旋写
         *
         * 30、Netty两大性能优化工具类
         *  FastThreadLocal
         *  Recycler
         *
         * 31、从Recycler获取对象
         *  获取当前线程的Stack
         *  从Stack里面弹出对象
         *  创建对象并绑定到Stack
         *
         * 32、异线程回收对象
         *  获取WeakOrderQueue
         *  创建WeakOrderQueue
         *  将对象追加到WeakOrderQueue
         *
         * 33、从Recycler获取对象
         *  获取当前线程的Stack
         *  从Stack里面弹出对象
         *  创建对象并绑定到Stack
         */

        EventLoopGroup boosGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workGroup)
                    // 创建一个ReflectiveChannelFactory，通过反射创建NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childAttr(AttributeKey.newInstance("childAttr"), "childAttr")
                    .handler(new ServerInboundHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            log.info(">>>>>>>>>>>>>>>>>>>>initChannel");
                        }
                    });
            System.out.println(Thread.currentThread());
            ChannelFuture future = bootstrap.bind(9999).sync();
            System.out.println(Thread.currentThread());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
