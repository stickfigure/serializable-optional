package com.voodoodyne.opt;

import java.io.Serial;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// A container that may or may not contain a non-null value.
///
/// This class has the same public API and behavior as Java 25's
/// [java.util.Optional], with the addition that it implements
/// [Serializable]. A populated instance can only be serialized when its
/// contained value is also serializable.
///
/// @param <T> the type of the contained value
public final class Optional<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Optional<?> EMPTY = new Optional<>(null);

	private final T value;

	private Optional(T value) {
		this.value = value;
	}

	/// Returns an empty `Optional`.
	///
	/// @param <T> the type of the absent value
	/// @return an empty `Optional`
	public static <T> Optional<T> empty() {
		@SuppressWarnings("unchecked")
		Optional<T> empty = (Optional<T>) EMPTY;
		return empty;
	}

	/// Returns an `Optional` containing the given non-null value.
	///
	/// @param value the value to contain
	/// @param <T> the type of the value
	/// @return an `Optional` containing `value`
	/// @throws NullPointerException if `value` is null
	public static <T> Optional<T> of(T value) {
		return new Optional<>(Objects.requireNonNull(value));
	}

	/// Returns an `Optional` containing `value`, or an empty
	/// `Optional` if `value` is null.
	///
	/// @param value the possibly-null value
	/// @param <T> the type of the value
	/// @return an `Optional` describing `value`
	public static <T> Optional<T> ofNullable(T value) {
		return value == null ? empty() : new Optional<>(value);
	}

	/// Returns the contained value.
	///
	/// @return the contained value
	/// @throws NoSuchElementException if no value is present
	public T get() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/// Returns whether a value is present.
	///
	/// @return `true` if a value is present
	public boolean isPresent() {
		return value != null;
	}

	/// Returns whether no value is present.
	///
	/// @return `true` if no value is present
	public boolean isEmpty() {
		return value == null;
	}

	/// Performs the action when a value is present.
	///
	/// @param action the action to perform
	public void ifPresent(Consumer<? super T> action) {
		if (value != null) {
			action.accept(value);
		}
	}

	/// Performs one of the given actions according to whether a value is
	/// present.
	///
	/// @param action the action to perform with a present value
	/// @param emptyAction the action to perform when no value is present
	public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
		if (value != null) {
			action.accept(value);
		} else {
			emptyAction.run();
		}
	}

	/// Returns this instance if empty or if its value matches the predicate;
	/// otherwise returns an empty `Optional`.
	///
	/// @param predicate the predicate to test
	/// @return the filtered `Optional`
	public Optional<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		if (isEmpty()) {
			return this;
		}
		return predicate.test(value) ? this : empty();
	}

	/// Maps a present value, treating a null mapping result as empty.
	///
	/// @param mapper the mapping function
	/// @param <U> the mapped value type
	/// @return the mapped `Optional`
	public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (isEmpty()) {
			return empty();
		}
		return Optional.ofNullable(mapper.apply(value));
	}

	/// Maps a present value to another `Optional` without wrapping it.
	///
	/// @param mapper the mapping function
	/// @param <U> the mapped value type
	/// @return the mapped `Optional`
	public <U> Optional<U> flatMap(
			Function<? super T, ? extends Optional<? extends U>> mapper) {
		Objects.requireNonNull(mapper);
		if (isEmpty()) {
			return empty();
		}

		@SuppressWarnings("unchecked")
		Optional<U> result = (Optional<U>) mapper.apply(value);
		return Objects.requireNonNull(result);
	}

	/// Returns this instance when populated, or an `Optional` supplied by
	/// `supplier` when empty.
	///
	/// @param supplier the fallback supplier
	/// @return this instance or the supplied fallback
	public Optional<T> or(Supplier<? extends Optional<? extends T>> supplier) {
		Objects.requireNonNull(supplier);
		if (isPresent()) {
			return this;
		}

		@SuppressWarnings("unchecked")
		Optional<T> result = (Optional<T>) supplier.get();
		return Objects.requireNonNull(result);
	}

	/// Returns a stream containing the value, or an empty stream.
	///
	/// @return a stream of zero or one value
	public Stream<T> stream() {
		return isEmpty() ? Stream.empty() : Stream.of(value);
	}

	/// Returns the value when present, otherwise `other`.
	///
	/// @param other the fallback value
	/// @return the present value or `other`
	public T orElse(T other) {
		return value != null ? value : other;
	}

	/// Returns the value when present, otherwise the supplied value.
	///
	/// @param supplier the fallback supplier
	/// @return the present or supplied value
	public T orElseGet(Supplier<? extends T> supplier) {
		return value != null ? value : supplier.get();
	}

	/// Returns the value when present.
	///
	/// @return the present value
	/// @throws NoSuchElementException if no value is present
	public T orElseThrow() {
		if (value == null) {
			throw new NoSuchElementException("No value present");
		}
		return value;
	}

	/// Returns the value when present, otherwise throws the supplied exception.
	///
	/// @param exceptionSupplier the exception supplier
	/// @param <X> the type of exception to throw
	/// @return the present value
	/// @throws X if no value is present
	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (value != null) {
			return value;
		}
		throw exceptionSupplier.get();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		return object instanceof Optional<?> other
				&& Objects.equals(value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}

	@Override
	public String toString() {
		return value != null ? "Optional[" + value + "]" : "Optional.empty";
	}

	@Serial
	private Object readResolve() {
		return value == null ? EMPTY : this;
	}
}
