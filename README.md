**sanfs**
==================

##1.  Description

sanfs - A simple distributed file system for learning.

##2.  Modules

###1) sanfs-common
Some common class.

###2) sanfs-client
The client of sanfs.

###3) sanfs-nameserver
Name Server is responsible for:

1. manage meta data, the directory structure.
2. manage status data, the storage server status.

###4) sanfs-storageserver
Storage Server can communicate with name server, deal with call from name server. It also handle client calls, transport file streams. Finally, StorageServers inter-communicate for duplication and load balance.

###2）sanfs-assembly
Build the project to compress package. Finally, it will generator a file of suffix of tar.gz

##3.  Compile

        $ cd $SANFS_SRC_HOME
        $ mvn -Dmaven.test.skip=true clean assembly:assembly

##4.  Use

###1) Configuring Environment Variables
Setting environment variables, you need to revise the file called sanfs-env.sh.

        vim $SANFS_HOME/conf/sanfs-env.sh
$SANFS_HOME is project home directory.

###2) Start nameserver

        $SANFS_HOME/bin/sanfs-nameserver.sh
###3) Start storageserver

        $SANFS_HOME/bin/sanfs-storageserver.sh
###4) Start client

        $SANFS_HOME/bin/sanfs-client.sh
##5. Acknowledge

Early sanfs based on the job of [@potatola](https://github.com/potatola), [@dshnightmare](https://github.com/dshnightmare), [@southerncross](https://github.com/southerncross). thanks for them.

##6. Other

Note: sanfs uses [Semantic Versioning][1] for its release versioning.

[1]:http://semver.org/