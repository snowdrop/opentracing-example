package me.snowdrop;

import me.snowdrop.service.SayGoodbye;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {

    @Autowired
    private RestTemplate restTemplate;

    public String getHostname() {
        return System.getenv("HOSTNAME") == null ? "a local machine" : System.getenv("HOSTNAME") + "pod";
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot running on " + getHostname() + " !'";
    }

    @RequestMapping("/goodbye")
    public String goodbye() {
        return SayGoodbye.getMessage();
    }

    @RequestMapping("/chaining")
    public String chaining() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/hello", String.class);
        return "Chaining + " + response.getBody();
    }
}