FROM  dockerfile/ubuntu

RUN \
     apt-get update && \
     apt-get install -y fabric python-setuptools 

RUN wget https://bootstrap.pypa.io/get-pip.py && \
     python get-pip.py

RUN apt-get update && \
    pip install protobuf

ADD protobuf.socketrpc-1.3.2-py2.6.egg /data/.

RUN easy_install /data/protobuf.socketrpc-1.3.2-py2.6.egg

