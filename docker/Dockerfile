FROM ubuntu:20.04
RUN apt-get update && apt-get install -y --no-install-recommends python3 libpcap0.8 openjdk-11-jre-headless wireless-tools gettext-base curl \
    && apt-get clean && rm -rf /var/lib/apt/lists/* \
    && export VERSION=`curl -s https://www.nzyme.org/blog/rss.xml | grep -E '[0-9]+\.[0-9]+\.[0-9]+' -o | head -n1` \
    && echo $VERSION && curl -s https://assets.nzyme.org/releases/nzyme-$VERSION.deb -o nzyme.deb  \
    && dpkg -i nzyme.deb && rm nzyme.deb

# Entrypoint: Replace variables in config 
COPY docker-entrypoint.sh /bin/
RUN chmod +x /usr/bin/docker-entrypoint.sh

ENTRYPOINT ["docker-entrypoint.sh"]

CMD /bin/bash /usr/share/nzyme/bin/nzyme