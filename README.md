# Serializable Optional<?>

The Java language designers had their reasons for _not_ making `java.util.Optional`
serializable; I just don't think those reasons are very good.

This library mirrors the Java 25 `Optional` API but implements `Serializable`.
