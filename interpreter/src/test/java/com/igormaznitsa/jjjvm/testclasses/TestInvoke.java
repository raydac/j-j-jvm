package com.igormaznitsa.jjjvm.testclasses;

public class TestInvoke implements Runnable {

    private int result;
    private long resultLong;

    private static Runnable runnable;

    public int calc(int a) {
        runnable = this;

        this.result = calc2(calc1(a));
        runnable.run();

        return this.result;
    }

    public long calc(long a) {
        runnable = this;

        this.resultLong = calc2(calc1(a));
        runnable.run();

        return this.resultLong;
    }

    private int calc1(int a) {
        return a * a;
    }

    private long calc1(long a) {
        return a * a;
    }

    public static int calc2(int a) {
        return a / 2;
    }

    public static long calc2(long a) {
        return a / 2L;
    }

    public void run() {
        this.result += 10;
        this.resultLong += 10L;
    }
}
