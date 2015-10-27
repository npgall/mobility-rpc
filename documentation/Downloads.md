## Download for Maven Projects ##

Mobility-RPC is in Maven Central, and can be added to a Maven project as follows:
```
<dependency>
    <groupId>com.googlecode.mobilityrpc</groupId>
    <artifactId>mobility-rpc</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Download for non-Maven projects or standalone server ##

The library has some dependencies of it's own (see TechnologiesUsed), which, above, Maven would take care of automatically.

For non-Maven projects, a special build of the library is also provided, built with [maven-shade-plugin](http://maven.apache.org/plugins/maven-shade-plugin/), which contains the library and all of the library's own dependencies packaged in a single jar file (ending "-all"). This build can be downloaded directly from [here](http://code.google.com/p/mobility-rpc/downloads/list).

## Download statistics, to June 2013 ##

![http://chart.googleapis.com/chart?chxl=0:|07%2F2012|08%2F2012|09%2F2012|10%2F2012|11%2F2012|12%2F2012|01%2F2013|02%2F2013|03%2F2013|04%2F2013|05%2F2013|06%2F2013&chxr=0,0,10|1,0,275&chxs=0,676767,11.5,-0.333,t,676767&chxt=x,y&chs=600x300&cht=lc&chds=0,275&chd=t:55,0,1,16,27,138,25,8,33,36,53,249&chdl=Downloads+-+Maven+Central+per+month&chdlp=b&chls=0.667&chma=2,0,7|17,28&chm=B,C5D4B5BB,0,0,0&chtt=Mobility-RPC+Maven+Central&dummy=foo.png](http://chart.googleapis.com/chart?chxl=0:|07%2F2012|08%2F2012|09%2F2012|10%2F2012|11%2F2012|12%2F2012|01%2F2013|02%2F2013|03%2F2013|04%2F2013|05%2F2013|06%2F2013&chxr=0,0,10|1,0,275&chxs=0,676767,11.5,-0.333,t,676767&chxt=x,y&chs=600x300&cht=lc&chds=0,275&chd=t:55,0,1,16,27,138,25,8,33,36,53,249&chdl=Downloads+-+Maven+Central+per+month&chdlp=b&chls=0.667&chma=2,0,7|17,28&chm=B,C5D4B5BB,0,0,0&chtt=Mobility-RPC+Maven+Central&dummy=foo.png)

784 downloads to-date (641 Maven, 143 non-Maven).