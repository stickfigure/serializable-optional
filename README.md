# Serializable Optional<?>

The Java language designers had their reasons for _not_ making `java.util.Optional`
serializable; I just don't think those reasons are very good.

This library mirrors the Java 25 `Optional` API but implements `Serializable`.

```xml
<dependency>
    <groupId>com.voodoodyne</groupId>
    <artifactId>serializable-optional</artifactId>
    <version>1.0.1</version>
</dependency>
```

```java
import com.voodoodyne.opt.Optional;
```

## Changes

* 2026-10-07 - 1.0.1
    * Add a `toJdk()` method and a static `from()` method for easy conversion.
