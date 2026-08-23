package com.voodoodyne.opt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OptionalTest {

    @Test
    void createsPresentAndEmptyValues() {
        Optional<String> present = Optional.of("value");
        Optional<String> empty = Optional.empty();

        assertTrue(present.isPresent());
        assertFalse(present.isEmpty());
        assertEquals("value", present.get());
        assertFalse(empty.isPresent());
        assertTrue(empty.isEmpty());
        assertSame(empty, Optional.ofNullable(null));
        assertEquals("value", Optional.ofNullable("value").get());
        assertThrows(NullPointerException.class, () -> Optional.of(null));
        assertThrows(NoSuchElementException.class, empty::get);
    }

    @Test
    void runsPresenceSpecificActions() {
        AtomicReference<String> consumed = new AtomicReference<>();
        AtomicBoolean emptyActionRan = new AtomicBoolean();

        Optional.of("value").ifPresent(consumed::set);
        Optional.<String>empty().ifPresent(value -> consumed.set("unexpected"));
        assertEquals("value", consumed.get());

        Optional.of("present").ifPresentOrElse(
                consumed::set, () -> emptyActionRan.set(true));
        assertEquals("present", consumed.get());
        assertFalse(emptyActionRan.get());

        Optional.<String>empty().ifPresentOrElse(
                consumed::set, () -> emptyActionRan.set(true));
        assertTrue(emptyActionRan.get());
    }

    @Test
    void filtersMapsAndFlatMaps() {
        Optional<String> value = Optional.of("abcd");

        assertSame(value, value.filter(text -> text.length() == 4));
        assertTrue(value.filter(text -> false).isEmpty());
        assertEquals(Optional.of(4), value.map(String::length));
        assertTrue(value.map(ignored -> null).isEmpty());
        assertEquals(Optional.of(4), value.flatMap(text -> Optional.of(text.length())));
        assertTrue(Optional.<String>empty().map(String::length).isEmpty());

        assertThrows(NullPointerException.class, () -> value.filter(null));
        assertThrows(NullPointerException.class, () -> value.map(null));
        assertThrows(NullPointerException.class, () -> value.flatMap(null));
        assertThrows(NullPointerException.class, () -> value.flatMap(ignored -> null));
    }

    @Test
    void suppliesFallbacksLazily() {
        Optional<String> present = Optional.of("present");
        Optional<String> empty = Optional.empty();
        AtomicBoolean supplierRan = new AtomicBoolean();

        assertSame(present, present.or(() -> {
            supplierRan.set(true);
            return Optional.of("fallback");
        }));
        assertFalse(supplierRan.get());
        assertEquals(Optional.of("fallback"), empty.or(() -> Optional.of("fallback")));
        assertThrows(NullPointerException.class, () -> present.or(null));
        assertThrows(NullPointerException.class, () -> empty.or(() -> null));

        assertEquals("present", present.orElse("fallback"));
        assertEquals("fallback", empty.orElse("fallback"));
        assertEquals("present", present.orElseGet(null));
        assertEquals("fallback", empty.orElseGet(() -> "fallback"));
        assertEquals("present", present.orElseThrow());
        assertThrows(NoSuchElementException.class, empty::orElseThrow);
        assertEquals("present", present.orElseThrow(null));
        assertThrows(IllegalStateException.class,
                () -> empty.orElseThrow(IllegalStateException::new));
    }

    @Test
    void exposesOptionalValueSemanticsAndStreams() {
        Optional<String> value = Optional.of("value");

        assertEquals(Optional.of("value"), value);
        assertNotEquals(Optional.of("other"), value);
        assertNotEquals(java.util.Optional.of("value"), value);
        assertEquals("value".hashCode(), value.hashCode());
        assertEquals(0, Optional.empty().hashCode());
        assertEquals("Optional[value]", value.toString());
        assertEquals("Optional.empty", Optional.empty().toString());
        assertEquals("value", value.stream().findFirst().orElseThrow());
        assertEquals(0, Optional.empty().stream().count());
    }

    @Test
    void serializesPresentAndEmptyValues() throws Exception {
        assertInstanceOf(Serializable.class, Optional.empty());
        assertEquals(1L, ObjectStreamClass.lookup(Optional.class).getSerialVersionUID());
        assertEquals(Optional.of("value"), roundTrip(Optional.of("value")));
        assertSame(Optional.empty(), roundTrip(Optional.empty()));
    }

    @Test
    void requiresPresentValueToBeSerializable() {
        Optional<Object> optional = Optional.of(new Object());

        assertThrows(NotSerializableException.class, () -> serialize(optional));
    }

    private static byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
        }
        return bytes.toByteArray();
    }

    private static Object roundTrip(Object object) throws Exception {
        byte[] bytes = serialize(object);
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return input.readObject();
        }
    }
}
