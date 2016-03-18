## SwitchYard

SwitchYard is a component-based development framework focused on building structured, maintainable services and applications using the concepts and best practices of SOA. It works with Apache Camel to provide a fast, simple, flexible integration runtime with comprehensive connectivity and transports.

This repository serves as the codebase for SwitchYard 2.0 and above.   

## How do I build this thing?

Prerequisites : 

- JBoss EAP 6.4.x - grab the [newest 6.4.x release](http://www.jboss.org/products/eap/overview/)
- Apache Maven 3 (we’re currently using 3.2.3, but other 3.x versions should work)

SwitchYard can be built for use with three different containers - EAP, WildFly, and Karaf.   Each container has a profile that you’ll want to build against (-Peap, -Pwildfly, -Pkaraf) if you want to deploy or test against them.

To build, unzip your EAP distribution somewhere and :

```
sh% cd switchyard
sh% mvn -Deap.home=<PATH-TO-EAP-HOME> -Peap clean install
```

## What versions of the containers are supported currently?

EAP : 6.4.x
WildFly : 10.0.0.CR4
Karaf : 2.4.0.Final

## How do I install my build of SwitchYard into a container?

If you built with -Peap, your build of SwitchYard will automatically be installed into the $EAP.HOME that you specified.
If you built with -Pwildfly, install the results of your build in release/jboss-as7/wildfly/dist/target into a WildFly 10 server.
If you built with -Pkaraf, you need to install the features URL into Karaf.  

```
karaf@root> features:addurl mvn:org.switchyard.karaf/switchyard/{SWITCHYARD-VERSION}/xml/features
```

## How to run checkstyle on your contributions 

In core and components modules, we suggest that you run checkstyle to make sure your contribution passes the style check :

sh% mvn checkstyle:check


## Where do I get this goodness?

Head over to our [downloads page](http://switchyard.jboss.org/downloads) for the newest releases.  Check out our awesome [documentation](https://docs.jboss.org/author/display/SWITCHYARDDOC/Home) as well.

## Where can I ask some questions?

If you have questions on how to use SwitchYard head on over to our [User Forum](https://developer.jboss.org/en/switchyard?view=discussions)

If you have questions on how to build / contribute to SwitchYard, head on over to our [Dev Forum](https://developer.jboss.org/en/switchyard/dev)
