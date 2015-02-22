import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.SameThreadExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;


public class Server {

	public static void main(String[] args) {
		PeerInfo serverInfo = new PeerInfo("localhost", 9192);
        RpcServerCallExecutor executor = new ThreadPoolCallExecutor(50, 100, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100000000, false), Executors.defaultThreadFactory());
        
        DuplexTcpServerPipelineFactory serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);
        serverFactory.setRpcServerCallExecutor(executor);
        
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(0,new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory())),
                        new NioEventLoopGroup(0,new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()))
                        );
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(serverFactory);
        bootstrap.localAddress(serverInfo.getPort());        
        
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        serverFactory.getRpcServiceRegistry().registerService(new MyProtoServiceImpl());
        
        bootstrap.bind(new InetSocketAddress(9192));
        
        System.out.println("started server...");
		

	}

}
