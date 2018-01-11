package me.snowdrop;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;

import com.uber.jaeger.senders.UdpSender;
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

    @Value("${jaeger.sender}")
    String JAEGER_URL;

    @Value("${jaeger.protocol}")
    String JAEGER_PROTOCOL;

    @Value("${jaeger.port}")
    int JAEGER_PORT;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public Tracer JaegerTracer() {
        Sender sender;
        if (JAEGER_PROTOCOL.equals("HTTP")) {
            LOG.info(">>> Jaeger Tracer calling the collector using a Http sender !");
            sender = new HttpSender(JAEGER_URL);
        } else {
            LOG.info(">>> Jaeger Tracer calling the Jaeger Agent running as a container sidecar with Udp Sender");
            // If maxPacketSize is null, then ThriftSender will set it to 65000
            sender = new UdpSender(JAEGER_URL,JAEGER_PORT,0);
        }

        Configuration.SenderConfiguration senderConfiguration = new Configuration
                .SenderConfiguration.Builder()
                .sender(sender)
                .build();

        return new Configuration("spring-boot",
                new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                new Configuration.ReporterConfiguration(true, 10, 10, senderConfiguration))
                .getTracer();
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
