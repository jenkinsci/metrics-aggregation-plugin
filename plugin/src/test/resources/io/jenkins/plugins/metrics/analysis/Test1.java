package com.example.anotherpackage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Test1 {

    void veryImportantMethod() {
        Map<String, Integer> interestingMap = new HashMap<>();
        Set<String> set = new HashSet<>();
        LocalDateTime time = LocalDateTime.now();

        interestingMap.put("1", 1);
    }

    public void doNothing() {
        //nothing to see here
    }

    public static class StaticInnerClass {
        public void staticInnerClassMethod() {
            Set<Double> anotherSet = new TreeSet<>();
        }
    }
}