package com.igormaznitsa.jjjvm.testclasses;

public class TestIssue {

    public static int i;


    public static String trampoline(){
        return appendChars('a', 'b');
    }

    public static String appendChars(char a, char b) {
        StringBuilder sb = new StringBuilder();
        char c = (char) (a ^ b);
        sb.append(c);
        return sb.toString();
    }

    public static String appendInts(int a, int b) {
        StringBuilder sb = new StringBuilder();
        char c = (char) (a ^ b);
        sb.append(c);
        return sb.toString();
    }
}
