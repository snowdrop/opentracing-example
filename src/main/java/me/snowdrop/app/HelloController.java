package me.snowdrop.app;

import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    public String getHostname() {
        return System.getenv("HOSTNAME") == null ? "a local machine" : System.getenv("HOSTNAME") + "pod";
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot running on " + getHostname() + " !'";
    }

    @RequestMapping("/goodbye")
    public String goodbye() {
       tracer.buildSpan("Calling SayGoodBye service");
       return "Say Goodbye !";
    }

    @RequestMapping("/chaining")
    public String chaining() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/hello", String.class);
        return "Chaining + " + response.getBody();
    }
}