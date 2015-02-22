import java.util.Date;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import rpcdata.ConfigProtos.ConfigReply;
import rpcdata.ConfigProtos.ConfigRequest;
import rpcdata.MyProtoService.GetConfigService;


public class MyProtoServiceImpl extends GetConfigService
{
    @Override
    public void getConfig(RpcController controller, ConfigRequest request, RpcCallback<ConfigReply> done)
    {
    	    System.out.println(new Date() +" processing request="+request.getVersion());
            ConfigReply.Builder cr = ConfigReply.newBuilder();
            cr.setHttpServerIp("Currtime="+System.currentTimeMillis());
            cr.setBinaryShareIp("1.1.1.2");
            cr.setPlayerType(ConfigReply.PlayerType.BCM97208);
            cr.setTcIp("1.1.1.3");
            cr.setTcIf("eth0");
            
            done.run(cr.build());
            int sleeptime = 10000;
            if(request.getVersion().equals("0"))
            {
            	sleeptime = 2*10000;
            }
            try {
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Sleep Interrupted for request="+request.getVersion());
			}
            System.out.println(new Date() +" processing done request="+request.getVersion());
    }  
}