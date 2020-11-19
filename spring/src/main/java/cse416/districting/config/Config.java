package cse416.districting.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cse416.districting.manager.*;

@Configuration
@EnableAsync
@ComponentScan("cse416/districting")
public class Config {

    public SimpMessagingTemplate simpMessagingTemplate;
    
    @Bean
    public JobManager jobManager(){
        return new JobManager(simpMessagingTemplate);
    }

    @Bean
    public StateManager stateManager(){
        return new StateManager();
        
    }

    @Bean
    public JobResultsManager jobResultsManager(){
        return new JobResultsManager();
    }
    
    @Bean("threadPoolTaskExecutor")
    public Executor taskExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(5);
      executor.setMaxPoolSize(10);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("Async-");
      executor.initialize();
      return executor;
    }
}