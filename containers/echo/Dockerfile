FROM ubuntu:latest
MAINTAINER Ben Weber 

RUN apt-get update \
  && apt-get install -y python3-pip python3-dev \
  && cd /usr/local/bin \
  && ln -s /usr/bin/python3 python \
  && pip3 install flask 

COPY echo.py echo.py

ENTRYPOINT ["python3","echo.py"]
