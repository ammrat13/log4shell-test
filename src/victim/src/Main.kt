import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.LogManager


// Get the logger to use for output
val logger: Logger = LogManager.getLogger()

fun main() {

    logger.info(
        "LDAP Trust: {}",
        System.getProperty("com.sun.jndi.ldap.object.trustURLCodebase"),
    )

    // Get the payload to log
    val payload: String? = System.getProperty("victim-payload")
    // Check for null
    if(payload == null) {
        logger.error("Payload to trace is null")
        return
    }

    // Log the payload infinitely
    // Sleep for a second between each one
    while(true) {
        logger.trace("Attempted injection: {}", payload)
        Thread.sleep(1000)
    }
}
