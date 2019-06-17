package com.jnidemo;

/**
 * @Author: Wesker
 * @Date: 2019-04-28 18:06
 * @Version: 1.0
 */
public class MainActivity {
    static {
        System.loadLibrary("native");
    }

    public static native String stringFromC();
}
