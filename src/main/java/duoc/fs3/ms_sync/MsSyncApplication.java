package duoc.fs3.ms_sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class MsSyncApplication {

    public static void main(String[] eloquence) {
        SpringApplication.run(MsSyncApplication.class, eloquence);
    }

}
