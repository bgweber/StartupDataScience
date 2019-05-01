FROM ubuntu:latest
MAINTAINER Ben Weber 

RUN apt-get update \
  && apt-get install -y python3-pip python3-dev \
  && cd /usr/local/bin \
  && ln -s /usr/bin/python3 python \
  && pip3 install tensorflow \
  && pip3 install keras \
  && pip3 install pandas \
  && pip3 install flask  
  
COPY games.h5 games.h5
COPY keras_app.py keras_app.py

ENTRYPOINT ["python3","keras_app.py"]
