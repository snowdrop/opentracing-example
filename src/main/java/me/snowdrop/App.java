package me.snowdrop;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.reporters.CompositeReporter;
import com.uber.jaeger.reporters.LoggingReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ProbabilisticSampler;

import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class App {

    @Value("${http.sender}")
    String URL;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public Tracer jaegerTracer() {
        Sender sender = new HttpSender(URL);
        Configuration.SenderConfiguration senderConfiguration = new Configuration.SenderConfiguration.Builder().sender(sender).build();
        return new Configuration("spring-boot",
                new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                new Configuration.ReporterConfiguration(true, 10, 10, senderConfiguration))
                .getTracer();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
