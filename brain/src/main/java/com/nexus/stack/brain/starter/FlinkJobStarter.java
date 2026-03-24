package com.nexus.stack.brain.starter;

import com.nexus.stack.brain.job.gmv.GmvFlinkJob;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FlinkJobStarter {

    @PostConstruct
    public void start() {
        /*new Thread(() -> {
            try {
                GmvFlinkJob.run();
            } catch (Exception e) {
                log.error("Flink job start failed", e);
            }
        }).start();*/
    }
}
