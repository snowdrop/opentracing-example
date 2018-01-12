package me.snowdrop;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;
import com.uber.jaeger.senders.UdpSender;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties(JaegerProperties.class)
public class App {

    private static Logger LOG = LoggerFactory.getLogger(App.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    @Qualifier("app-tracer")
    public Tracer JaegerTracer(JaegerProperties jaegerProperties) {
        Sender sender;
        if (jaegerProperties.getProtocol().equalsIgnoreCase("HTTP")) {
            LOG.info(">>> Jaeger Tracer calling the collector using a Http sender !");
            sender = new HttpSender(jaegerProperties.getSender());
        } else {
            LOG.info(">>> Jaeger Tracer calling the Jaeger Agent running as a container sidecar with Udp Sender");
            // If maxPacketSize is null, then ThriftSender will set it to 65000
            sender = new UdpSender(jaegerProperties.getSender(), jaegerProperties.getPort(),0);
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
