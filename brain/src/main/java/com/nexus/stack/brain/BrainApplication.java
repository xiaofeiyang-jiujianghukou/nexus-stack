package com.nexus.stack.brain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class BrainApplication {

	public static void main(String[] args) {
		//PluginLoader.loadFlinkPlugins("/data/plugins/flink");
		SpringApplication.run(BrainApplication.class, args);
	}

	/*@Bean
	public ApplicationRunner runFlinkJobs(
			UpgradeFlinkSqlGmvJob gmvJob,
			UpgradeFlinkSqlWideOrdersJob wideOrdersJob) {
		return args -> {
			ExecutorService executor = Executors.newFixedThreadPool(2);

			// 异步启动第一个作业
			executor.execute(() -> {
				try {
					log.info("🌊 [Flink] 启动实时流计算任务 [FlinkSqlGmvJob]...");
					gmvJob.run();
				} catch (Exception e) {
					log.error("GMV作业失败", e);
				}
			});

			// 异步启动第二个作业
			executor.execute(() -> {
				try {
					log.info("🌊 [Flink] 启动实时流计算任务 [FlinkSqlWideOrdersJob]...");
					wideOrdersJob.run();
				} catch (Exception e) {
					log.error("宽表作业失败", e);
				}
			});

			// 不关闭线程池，让作业持续运行
			// executor.shutdown();

			log.info("✅ 所有 Flink 作业已提交");
			log.info("🔗 Flink Web UI: http://localhost:8081");
		};
	}*/

}
