# Smartcard-based client authentication for the BouncyCastle TLS Injection Mechanism (TLS-IM)

*by Sergejs Kozlovičs, 2024*

This repository complements our BouncyCastle TLS Injection Mechanism with the ability to use external smartcard-based client authentication. The client has to implement the `sign()` function, which should contact the smartcard (or some other external devices) to sign the requested data. The data will be part of the TLS Server Hello message, which needs to be signed by the client to verify its identity at the server side.

## Before Use

First, clone the BC TLS-IM into some subdirectory of your project (e.g., into `src/tls-injection-mechanism`).

Add the following BC directories as source sets, e.g., (for Gradle):

```
sourceSets.main {
    java {
        srcDirs 'src/main/java',
                'src/tls-injection-mechanism/core/src/main/java',
                'src/tls-injection-mechanism/pkix/src/main/java',
                'src/tls-injection-mechanism/prov/src/main/java',
                'src/tls-injection-mechanism/tls/src/main/java',
                'src/tls-injection-mechanism/tls/src/main/jdk1.9/org/bouncycastle/jsse/provider',
                // ^^^ important that we do not include module-info.java (otherwise, the whole BC module farm is needed)
                // ^^^ and org/bouncycastle/tls/crypto/impl/jcajce/** (otherwise, there are duplicate class files)
                'src/tls-injection-mechanism/util/src/main/java'
                
                ...
    }
}
```

Second, clone this repository, e.g., into `src/tls-injection-smartcard`. Then add the following directory to srcDirs:

```
                'src/tls-injection-smartcard/src/main/java'
```

## How to Use

Just create an instance of `InjectableSmartCardRSA` by passing the sign() function to it. This sign() function must encrypt the message using pure SHA256WITHRSA (not the PSS variant).

> The sign() function has the following declaration:
>
> ```
> public interface SmartCardSignFunction {
>     byte[] sign(byte[] message) throws Exception;
> }
> ```

Then add that instance to an instance of `InjectableAlgorithms`.

Finally, `push()` the injectable algorithms into the TLS `InjectionPoint`.

```
import org.bouncycastle.tls.injection.InjectionPoint;
import lv.lumii.pqc.InjectableSmartCardRSA;


...
InjectableSmartCardRSA myRSA = new InjectableSmartCardRSA(smartCardSignFunction);
InjectableAlgorithms algs = new InjectableAlgorithms()
                .withSigAlg("SHA256WITHRSA", List.of(new String[]{}), myRSA.oid(), myRSA.codePoint(), myRSA)
                .withSigAlg("RSA", List.of(new String[]{}), myRSA.oid(), myRSA.codePoint(), myRSA);
                
InjectionPoint.theInstance().push(algs);
```

Notice that for RSA, we need both names SHA256WITHRSA and RSA for the same `myRSA` object. That is due to specifics of BC invocations of the RSA algorithm from TLS.
