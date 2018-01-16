package com.igormaznitsa.jjjvm.testclasses;

public class TestInnerClasses {
    private interface Calculator {
        int doCalc();
    }

    private class NonStaticClass implements Calculator {
        private final int a;
        private final int b;

        public NonStaticClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int doCalc() {
            return a + b;
        }
    }


    private static class StaticClass implements Calculator {
        private final int a;
        private final int b;

        public StaticClass(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public int doCalc() {
            return a / b;
        }
    }

    public int test(final int a, final int b) {
        int c = new Calculator() {

            public int doCalc() {
                return a * b;
            }
        }.doCalc();
        return new StaticClass(c, new NonStaticClass(a, b).doCalc()).doCalc();
    }
}
