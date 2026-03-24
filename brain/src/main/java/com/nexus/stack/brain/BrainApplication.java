package com.nexus.stack.brain;

import com.nexus.stack.brain.job.joins.OrderUserMemberJoinJob;
import com.nexus.stack.brain.service.DimensionSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@Slf4j
public class BrainApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrainApplication.class, args);
	}

	@Bean
	public CommandLineRunner runFlinkJob(DimensionSyncService syncService, OrderUserMemberJoinJob flinkJob) {
		return args -> {
			// 1. 先同步数据库数据到 Kafka
			syncService.syncUserDimensions();

			// 2. 启动 Flink 任务 (注意：Flink 的 run 是阻塞的，会占用主线程)
			log.info("🌊 [Flink] 启动实时流计算任务...");
			flinkJob.run();
		};
	}

}
