

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;







import java.util.concurrent.TimeUnit;

import rpcdata.ConfigProtos;
import rpcdata.MyProtoService;
import rpcdata.ConfigProtos.ConfigReply;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.ClientRpcController;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;


public class Client {
	private static RpcClientChannel channel = null;

	public static void main(String[] args) throws IOException {

		PeerInfo client = new PeerInfo("127.0.0.1", 1234);
		PeerInfo server = new PeerInfo("127.0.0.1", 9192);

		try {
			DuplexTcpClientPipelineFactory clientFactory = new DuplexTcpClientPipelineFactory();
			// force the use of a local port
			// - normally you don't need this
			clientFactory.setClientInfo(client);
			
	    	ExtensionRegistry r = ExtensionRegistry.newInstance();
	    	MyProtoService.registerAllExtensions(r);
			clientFactory.setExtensionRegistry(r);

			clientFactory.setConnectResponseTimeoutMillis(10000);
	    	RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(50, 100, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10000000, false), Executors.defaultThreadFactory());
			clientFactory.setRpcServerCallExecutor(rpcExecutor);			
			
			// RPC payloads are uncompressed when logged - so reduce logging
			CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
			logger.setLogRequestProto(false);
			logger.setLogResponseProto(false);
			clientFactory.setRpcLogger(logger);
			
			// Set up the event pipeline factory.
	        // setup a RPC event listener - it just logs what happens
	        RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
	        
	        final RpcConnectionEventListener listener = new RpcConnectionEventListener() {
				
				@Override
				public void connectionReestablished(RpcClientChannel clientChannel) {
					System.out.println("connectionReestablished " + clientChannel);
					System.out.println();
					channel = clientChannel;
				}
				
				@Override
				public void connectionOpened(RpcClientChannel clientChannel) {
					System.out.println("connectionOpened " + clientChannel);
					channel = clientChannel;
				}
				
				@Override
				public void connectionLost(RpcClientChannel clientChannel) {
					System.out.println("connectionLost " + clientChannel);
				}
				
				@Override
				public void connectionChanged(RpcClientChannel clientChannel) {
					System.out.println("connectionChanged " + clientChannel);
					channel = clientChannel;
				}
			};
			rpcEventNotifier.addEventListener(listener);
			clientFactory.registerConnectionEventListener(rpcEventNotifier);

			Bootstrap bootstrap = new Bootstrap();
			final EventLoopGroup workers = new NioEventLoopGroup(16,new RenamingThreadFactoryProxy("workers", Executors.defaultThreadFactory()));

	        bootstrap.group(workers);
	        bootstrap.handler(clientFactory);
	        bootstrap.channel(NioSocketChannel.class);
	        bootstrap.option(ChannelOption.TCP_NODELAY, true);
	    	bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10000);
	        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
	        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);

			RpcClientConnectionWatchdog watchdog = new RpcClientConnectionWatchdog(clientFactory,bootstrap);
			rpcEventNotifier.addEventListener(watchdog);
	        watchdog.start();

			CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
			shutdownHandler.addResource(workers);
			shutdownHandler.addResource(rpcExecutor);
			
	        Runtime.getRuntime().addShutdownHook(new Thread()
	        {
	            public void run()
	            {
                try
                {
                    System.out.println("Received signal TERM");
                    //workers.shutdownGracefully();
                }
                catch(Exception e)
                {
                	System.out.println("exception while shutting down..");
                	e.printStackTrace();
                }
	            }
	        });    
	        clientFactory.peerWith(server, bootstrap);
	        
	       
			
	        int i =0;
	        final RpcController controller = channel.newRpcController();
			while (true && channel != null) {
				
//				MyProtoService.GetConfigService.BlockingInterface  pingpongService = MyProtoService.GetConfigService.newBlockingStub(channel);
//				final String  requestId = ""+(i++);
//				ConfigProtos.ConfigRequest.Builder cr = ConfigProtos.ConfigRequest.newBuilder();
//				cr.setVersion(requestId);
//				System.out.println(new Date() + " Sending Request:"+requestId);
//				ConfigProtos.ConfigReply response = pingpongService.getConfig(controller, cr.build());
//				System.out.println(new Date() + " Received Response:"+response);
				
				MyProtoService.GetConfigService.Stub  myService = MyProtoService.GetConfigService.newStub(channel);
				final String  requestId = ""+(i++);
				ConfigProtos.ConfigRequest.Builder cr = ConfigProtos.ConfigRequest.newBuilder();
				cr.setVersion(requestId);
				System.out.println(new Date() + " Sending Request:"+requestId);
				myService.getConfig(controller, cr.build(), new RpcCallback<ConfigProtos.ConfigReply>()
            {
                public void run(ConfigProtos.ConfigReply response)
                {
                    System.out.println(new Date() + " Received Request Id: "+requestId +" response Id: "+ response.getHttpServerIp());
                    controller.reset();
                }
            });
				
		        // Check success
		        if (controller.failed())
		        {
		            System.err.println(String.format("Rpc failed %s ", controller.errorText()));
		        } 
			}

		} catch ( Exception e ) {
			System.out.println("Failure."+ e);
			e.printStackTrace();
		} finally {			 
			channel.close();
			System.exit(0);
		}		
	}


}
