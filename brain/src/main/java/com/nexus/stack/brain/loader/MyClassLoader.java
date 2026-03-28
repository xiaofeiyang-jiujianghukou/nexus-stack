package com.nexus.stack.brain.loader;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MyClassLoader {

    @PostConstruct
    public void fixClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
    }
}
