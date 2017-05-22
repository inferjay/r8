# Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.
FROM debian:jessie

RUN echo deb http://http.debian.net/debian jessie-backports main >> /etc/apt/sources.list

RUN apt-get update && apt-get install -y \
	bzip2 \
	g++ \
	gcc \
	git \
	nano \
	python \
	python-dev \
	sed \
	texinfo \
	unzip \
	wget \
    sudo

# Install OpenJDK 8 dependencies from jessi-backports (see https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=851667)
RUN apt-get install -y -t jessie-backports openjdk-8-jre-headless ca-certificates-java
RUN apt-get install -y openjdk-8-jdk

# Set the timezone.
RUN echo "Europe/Copenhagen" > /etc/timezone && \
    dpkg-reconfigure -f noninteractive tzdata

ENV user r8

# Create user without password and sudo access.
RUN useradd -m -G dialout,sudo $user && \
    echo "$user ALL=(ALL:ALL) NOPASSWD:ALL" > /etc/sudoers.d/$user && \
    chmod 440 /etc/sudoers.d/$user

USER $user

CMD (cd /home/$user && /bin/bash)
