package carpet.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class JavaVersionUtil {
	public static final int JAVA_VERSION = getJavaVersion();

	private JavaVersionUtil() {
	}

	private static int getJavaVersion() {
		String version = System.getProperty("java.version");
		if (version.startsWith("1.")) {
			// old format (Java 8 and below)
			return version.charAt(2) - '0';
		} else {
			// new format (Java 9 and above)
			int dotIndex = version.indexOf('.');
			if (dotIndex == -1) {
				return Integer.parseInt(version);
			} else {
				return Integer.parseInt(version.substring(0, dotIndex));
			}
		}
	}

	public static <T> FieldAccessor<T> objectFieldAccessor(Class<?> ownerClass, String name, Class<T> fieldType) {
		Field field;
		try {
			field = ownerClass.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Could not find field", e);
		}
		if (field.getType() != fieldType) {
			throw new RuntimeException("Field has wrong type, expected \"" + fieldType.getName() + "\", got \"" + field.getType().getName() + "\"");
		}
		if (fieldType.isPrimitive()) {
			throw new RuntimeException("objectFieldAccessor does not work for primitive field types");
		}

		try {
			field.setAccessible(true);
		} catch (RuntimeException e) { // InaccessibleObjectException
			if (JAVA_VERSION <= 8) {
				throw e;
			}
			// allows us to comment out it below and don't get problems here
			try {
				@SuppressWarnings("unchecked")
				FieldAccessor<T> r = (FieldAccessor<T>) JavaVersionUtil.class.getDeclaredMethod("newUnsafeFieldAcessor", Class.class, Field.class, Class.class)
						.invoke(null, ownerClass, field, fieldType);
				return r;
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
			}
		}

		try {
			return MethodHandleFieldAccessor.of(ownerClass, field, fieldType);
		} catch (IllegalAccessException e) {
			return ReflectionFieldAccessor.of(ownerClass, field, fieldType);
		}
	}

	public interface FieldAccessor<T> {
		T get(Object instance);
	}

	private static class MethodHandleFieldAccessor<T> implements FieldAccessor<T> {
		private final MethodHandle getter;

		private MethodHandleFieldAccessor(MethodHandle getter) {
			this.getter = getter;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get(Object instance) {
			try {
				return (T) getter.invoke(instance);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

		private static <T> FieldAccessor<T> of(Class<?> ownerClass, Field field, Class<T> fieldType) throws IllegalAccessException {
			return new MethodHandleFieldAccessor<>(MethodHandles.lookup().unreflectGetter(field));
		}
	}

	private static class ReflectionFieldAccessor<T> implements FieldAccessor<T> {
		private final Field field;

		private ReflectionFieldAccessor(Field field) {
			this.field = field;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get(Object instance) {
			try {
				return (T) field.get(instance);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		private static <T> FieldAccessor<T> of(Class<?> ownerClass, Field field, Class<T> fieldType) {
			return new ReflectionFieldAccessor<>(field);
		}
	}

	/*
	// currently commented out, uncomment if you need to, if this works for enough people (in production) remove this
	@SuppressWarnings("unused")
	private static <T> FieldAccessor<T> newUnsafeFieldAccessor(Class<?> ownerClass, Field field, Class<T> fieldType) {
		return UnsafeFieldAccessor.of(ownerClass, field, fieldType);
	}

	private static class UnsafeFieldAccessor<T> implements FieldAccessor<T> {
		private static final sun.misc.Unsafe unsafe = getUnsafe();
		private static sun.misc.Unsafe getUnsafe() {
			try {
				if (true)
					return sun.misc.Unsafe.getUnsafe();

				for (Field field : sun.misc.Unsafe.class.getDeclaredFields()) {
					if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == sun.misc.Unsafe.class) {
						field.setAccessible(true);
						return (sun.misc.Unsafe) field.get(null);
					}
				}
				throw new RuntimeException("Unable to get Unsafe instance");
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Unable to get Unsafe instance", e);
			}
		}

		private final Class<?> ownerClass;
		private final long fieldOffset;

		private UnsafeFieldAccessor(Class<?> ownerClass, long fieldOffset) {
			this.ownerClass = ownerClass;
			this.fieldOffset = fieldOffset;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get(Object instance) {
			return (T) unsafe.getObject(ownerClass.cast(instance), fieldOffset);
		}

		private static <T> FieldAccessor<T> of(Class<?> ownerClass, Field field, Class<T> fieldType) {
			long fieldOffset = unsafe.objectFieldOffset(field);
			return new UnsafeFieldAccessor<>(ownerClass, fieldOffset);
		}
	}
	//*/
}