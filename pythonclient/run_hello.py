import sys
sys.path.append('../../main')
import traceback

# Import required RPC modules
import hello_world_pb2
from protobuf.socketrpc import RpcService

# Configure logging
import logging
log = logging.getLogger(__name__)
logging.basicConfig(level=logging.DEBUG)

# Server details
hostname = 'localhost'
port = 8090


# Create a request
request = hello_world_pb2.HelloRequest()
request.my_name = 'Zach'

# Create a new service instance
service = RpcService(hello_world_pb2.HelloWorldService_Stub,
                     port,
                     hostname)


def callback(request, response):
    """Define a simple async callback."""
    log.info('Asynchronous response :' + response.__str__())

# Make an asynchronous call
try:
    log.info('Making asynchronous call')
    response = service.HelloWorld(request, callback=callback)
except Exception, ex:
    log.exception(ex)

# Make a synchronous call
try:
    log.info('Making synchronous call')
    response = service.HelloWorld(request, timeout=10000)
    log.info('Synchronous response: ' + response.__str__())
except Exception, ex:
    log.exception(ex)