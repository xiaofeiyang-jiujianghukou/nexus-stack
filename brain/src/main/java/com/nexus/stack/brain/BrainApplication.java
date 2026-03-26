package com.nexus.stack.brain;

import com.nexus.stack.brain.job.cdc.mysql.FlinkSqlGmvJob;
import com.nexus.stack.brain.job.cdc.mysql.FlinkSqlWideOrdersJob;
import com.nexus.stack.brain.service.DimensionSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@SpringBootApplication(exclude = {
//		DataSourceAutoConfiguration.class,
//		DataSourceTransactionManagerAutoConfiguration.class,
//		JdbcTemplateAutoConfiguration.class
//})
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class BrainApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrainApplication.class, args);
	}

	@Bean
	public ApplicationRunner runFlinkJobs(
			FlinkSqlGmvJob gmvJob,
			FlinkSqlWideOrdersJob wideOrdersJob) {
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
	}

	/*@Bean(name = "gmvJob")
	public CommandLineRunner runFlinkJob(FlinkSqlGmvJob gmvJob) {
		return args -> {
			// 启动 Flink 任务 (注意：Flink 的 run 是阻塞的，会占用主线程)
			log.info("🌊 [Flink] 启动实时流计算任务 [FlinkSqlGmvJob]...");
			gmvJob.run();
		};
	}*/

	/*@Bean(name = "wideOrdersJob")
	public CommandLineRunner runFlinkSqlWideOrdersJob(FlinkSqlWideOrdersJob wideOrdersJob) {
		return args -> {

			// 启动 Flink 任务 (注意：Flink 的 run 是阻塞的，会占用主线程)
			log.info("🌊 [Flink] 启动实时流计算任务 [FlinkSqlWideOrdersJob]...");
			wideOrdersJob.run();
		};
	}*/

}
