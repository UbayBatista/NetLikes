package software.ulpgc.netlikes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetlikesApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetlikesApplication.class, args);
	}

}
