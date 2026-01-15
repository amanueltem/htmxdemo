package com.aman.htmxdemo;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import java.util.UUID;

public class MyProjectHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // This tells GraalVM: "I will definitely need to create UUID arrays at runtime"
        hints.reflection().registerType(UUID[].class);
    }
}