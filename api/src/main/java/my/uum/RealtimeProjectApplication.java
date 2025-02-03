package my.uum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"my.uum.config", "my.uum.controller", "my.uum.repository", "my.uum.entity"})
public class RealtimeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeProjectApplication.class, args);
    }
}
