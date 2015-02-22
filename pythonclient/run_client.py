# Add main protobuf module to classpath
import sys
import traceback
sys.path.append('../../main')
# Import required RPC modules
import getConfigService_pb2
import testData_pb2
from protobuf.socketrpc import RpcService

# Configure logging
import logging
log = logging.getLogger(__name__)
logging.basicConfig(level=logging.DEBUG)

# Server details
hostname = 'localhost'
port = 9192


# Create a request
request = testData_pb2.ConfigRequest()
request.version = "1"

# Create a new service instance
service = RpcService(getConfigService_pb2.GetConfigService_Stub,9192,"javaserver")

def callback(request, response):
    """Define a simple async callback."""
    log.info('Asynchronous response :' + response.__str__())

# Make an asynchronous call
try:
    log.info('Making asynchronous call')
    response = service.getConfig(request, callback=callback)
    log.info('Asynchronous response: ' + response.__str__())
except Exception, ex:
    print "exception=",ex

# Make a synchronous call
try:
    log.info('Making synchronous call')
    response = service.getConfig(request, timeout=10000)
    log.info('Synchronous response: ' + response.__str__())
except Exception, ex:
    print "exception=",ex
