JBoss Data Grid Modules for EAP (Remote Hot Rod Client)
======================================================

This distribution contains a set of modules for Enterprise Application Platform 6.x. 
When these modules are installed, JDG libraries for HotRod client do not have to be 
included in the user deployment. In order not to conflict with the Infinispan modules
which are already present in EAP (used by Clustering), the modules from this distribution 
are placed within their own slot identified by the major.minor version of JDG.

In order to add dependencies from the JDG modules to application's classpath, the EAP deployer
must be provided with the list of dependencies in one of the following ways:

1) Adding dependency configuration to MANIFEST.MF
-------------------------------------------------

Manifest-Version: 1.0
Dependencies: org.infinispan.commons:jdg-6.3 services, org.infinispan.client.hotrod:jdg-6.3 services


2) Adding dependency configuration to jboss-deployment-structure.xml
--------------------------------------------------------------------

<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
    <deployment>
        <dependencies>
            <module name="org.infinispan.commons" slot="jdg-6.3" services="export"/>
            <module name="org.infinispan.client.hotrod" slot="jdg-6.3" services="export"/>
        </dependencies>
    </deployment>
</jboss-deployment-structure>

NOTE: For more information on jboss-deployment-structure.xml, see documentation for Enterprise Application Platform


Examples of MANIFEST.MF for HotRod client
-----------------------------------------

* Basic HotRod client

Manifest-Version: 1.0
Dependencies: org.infinispan.commons:jdg-6.3 services, org.infinispan.client.hotrod:jdg-6.3 services, 

* HotRod client with Remote Query functionality

Manifest-Version: 1.0
Dependencies: org.infinispan.commons:jdg-6.3 services, org.infinispan.client.hotrod:jdg-6.3 services, org.infinispan.infinispan-remote-query-client:jdg-6.3 services, org.infinispan.infinispan-query-dsl:jdg-6.3 services
