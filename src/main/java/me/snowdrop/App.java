package me.snowdrop;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;

import io.opentracing.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class App {

    private static Logger LOG = LoggerFactory.getLogger(App.class);

    @Value("${http.sender}")
    String URL;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    @ConditionalOnExpression("'System.getenv(\"HOSTNAME\")'=='null'")
    public Tracer localJaegerTracer() {
        LOG.info(">>> Using Jaeger Tracer calling the collector using Http sender !");
        Sender sender = new HttpSender(URL);
        Configuration.SenderConfiguration senderConfiguration = new Configuration.SenderConfiguration.Builder().sender(sender).build();
        return new Configuration("spring-boot",
          new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
          new Configuration.ReporterConfiguration(true, 10, 10, senderConfiguration))
          .getTracer();
    }

    @Bean
    public Tracer jaegerTracer() {
        LOG.info(">>> Using Jaeger Tracer calling the Jaeger Agent running as a container sidecar");
        return new Configuration("spring-boot",
           new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
           new Configuration.ReporterConfiguration(true, System.getenv("HOSTNAME"),null,null, null))
           .getTracer();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
