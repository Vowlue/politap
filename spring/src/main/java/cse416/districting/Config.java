package cse416.districting;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("cse416/districting")
public class Config {

    @Bean
    public JobManager jobManager(){
        return new JobManager();
    }

    @Bean
    public StateManager stateManager(){
        return new StateManager();
        
    }

    @Bean
    public JobResultsManager jobResultsManager(){
        return new JobResultsManager();
    }
    
}