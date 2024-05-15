package com.cloud_disk.cloud_dream_disk.utils;

public class ThreadLocalUtil {
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal<>();

    public static void set(Object Value) {
        THREAD_LOCAL.set(Value);
    }

    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
