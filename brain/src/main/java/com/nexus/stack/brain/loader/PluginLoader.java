package com.nexus.stack.brain.loader;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

@Slf4j
public class PluginLoader {

    public static void loadFlinkPlugins(String pluginDir) {
        log.info("===========准备加载插件===========");
        File dir = new File(pluginDir);
        File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null) {
            Thread.currentThread().getContextClassLoader();
            return;
        }

        URL[] urls = Arrays.stream(jars)
                .map(f -> {
                    try {
                        log.info("===========正在加载插件{}===========", f.toURI().toURL());
                        return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);

        // URLClassLoader 独立加载 Flink
        URLClassLoader flinkClassLoader = new URLClassLoader(urls,
                Thread.currentThread().getContextClassLoader());
//        URLClassLoader flinkClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        // 设置为当前线程上下文 ClassLoader
        Thread.currentThread().setContextClassLoader(flinkClassLoader);
        log.info("===========结束加载插件===========");
    }

}
