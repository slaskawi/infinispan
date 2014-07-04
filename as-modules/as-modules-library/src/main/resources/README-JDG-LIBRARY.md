JBoss Data Grid Modules for EAP (Library Mode)
==============================================

This distribution contains a set of modules for Enterprise Application Platform 6.x. 
When these modules are installed, JDG libraries do not have to be included in the user 
deployment. In order not to conflict with the Infinispan modules which are already 
present in EAP (used by Clustering), the modules from this distribution are placed
within their own slot identified by the major.minor version of JDG.

In order to add dependencies from the JDG modules to application's classpath, the EAP deployer
must be provided with the list of dependencies in one of the following ways:

1) Adding dependency configuration to MANIFEST.MF
-------------------------------------------------

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services


2) Adding dependency configuration to jboss-deployment-structure.xml
--------------------------------------------------------------------

<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
    <deployment>
        <dependencies>
            <module name="org.infinispan" slot="jdg-6.3" services="export"/>
        </dependencies>
    </deployment>
</jboss-deployment-structure>

NOTE: For more information on jboss-deployment-structure.xml, see documentation for Enterprise Application Platform


Examples of MANIFEST.MF for some of the main JDG features
---------------------------------------------------------

* JDG Core

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services

* Embedded Query

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services, org.infinispan.query:jdg-6.3 services

* JDBC Cache Store

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services, org.infinispan.persistence.jdbc:jdg-6.3 services

* JPA Cache Store

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services, org.infinispan.persistence.jpa:jdg-6.3 services

* LevelDB Cache Store

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services, org.infinispan.persistence.leveldb:jdg-6.3 services

* CDI Extension

Manifest-Version: 1.0
Dependencies: org.infinispan:jdg-6.3 services, org.infinispan.cdi:jdg-6.3 meta-inf


NOTE: The file jboss-deployment-structure.xml with these dependencies can be created analogously 
      to the above example.
