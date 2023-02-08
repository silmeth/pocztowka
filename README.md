# Pocztówka z kotliny

Proof-of-concept [`postcard` format](https://postcard.jamesmunns.com/wire-format.html) serialization using [`kotlinx.serialization`](https://github.com/Kotlin/kotlinx.serialization). Postcard is a size-efficient binary serialization format first as [a Rust library](https://crates.io/crates/postcard) intended mostly for use in embedded and other size-constrained contexts, it has been adopted by The Unicode Consortium’s [`ICU4X`](https://github.com/unicode-org/icu4x/issues/869).

The name *pocztówka z kotliny* means ‘a postcard from the valley’ in Polish, *pocztówka* being the word for ‘a postcard’.

It’s not finished and nowhere near production-ready.

Usage (after building it and adding to your classpath):

```kotlin
import com.gitlab.silmeth.pocztowka.Postcard
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.decodeFromByteArray

// …
@Serializable
data class Foo(val i: Int, val s: String)

val postcard = Postcard()

val foo = Foo(1, "foo")
// serialization
val bytes = postcard.encodeToByteArray(foo)
// deserialization
val deserialized: Foo = postcard.decodeFromByteArray(bytes)
```

It does support non-zig-zag serialization and deserialization of Kotlin unsigned types (`UShort`, `UInt`, `ULong`) by default as opposed to zig-zaged signed integers.

There are some tests in [`PostcardDecoderTest`](lib/src/test/kotlin/com/gitlab/silmeth/pocztowka/PostcardDecoderTest.kt) and [`PostcardEncoderTest`](lib/src/test/kotlin/com/gitlab/silmeth/pocztowka/PostcardEncoderTest.kt).

### Current restrictions
* JVM-only – the library was developed using Kotlin JVM and currently does not support Kotlin/Native or Kotlin/JS.
* Polymorphism – polymorphic types marked as `@Serializable` can be serialized and deserialized but the format is not compatible with Rust’s data-carrying enums as the type/variant is by default serialized as a string with the full type’s path, and can be customized to any string – but `postcard` in Rust typically writes the variant discriminator as an unsigned integer.
* Arrays – in Rust arrays are statically-sized, so data formats containing fixed-sized arrays can be serialized without any array size tag. In Kotlin `Array`s are dynamically-sized and thus they behave like `List`s for serialization, serializing their number of elements first. If you need Rust-like behaviour, you can just explicitly list all elements as separate fields in your structure (instead of using array), or write a custom serializer for it.
* Flavors – the Rust library supports “flavors” which is a term for middleware processing the input or output of the serializator on the fly (compressing it, calculating and adding crc checksums, etc.). Nothing like that is implemented here (to achieve the same, you need to pass the data through a separate processing stage yourself).
