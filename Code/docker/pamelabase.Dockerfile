# To build,
#   docker build -t registry.gitlab.com/dollabsp/docker/pamelabase -f pamelabase.Dockerfile .

# To push to Dollabs public docker repo,
#   docker push registry.gitlab.com/dollabsp/docker/pamelabase

FROM registry.gitlab.com/dollabsp/docker/javabase

ENV PATH $HOME/bin:$PATH
ENV PATH /root/bin:/root/src/github/dollabs/pamela/bin:$PATH
# Not sure if this is a good approach..
ENV BOOT_AS_ROOT=yes

RUN curl -fsSLo pamela-setup https://raw.githubusercontent.com/dollabs/pamela/master/bin/pamela-setup && chmod +x pamela-setup && ./pamela-setup

# Make pamela.jar available locally
WORKDIR /root/src/github/dollabs/pamela
RUN ls -l
RUN boot local

CMD pamela --version
