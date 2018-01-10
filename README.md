# Instructions

1. Create new Spring Boot Booster using this command

```bash
mvn archetype:generate -DarchetypeGroupId=me.snowdrop \
                       -DarchetypeArtifactId=booster-archetype \
                       -DarchetypeVersion=1.0.0-SNAPSHOT \
  					   -DgroupId=me.snowdrop \
  					   -DartifactId=booster-opentracing \
  					   -Dversion=1.0-SNAPSHOT
```

2. Add missing dependencies

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Dep required - Why ? -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- OpenTracing -->
<dependency>
    <groupId>io.opentracing.contrib</groupId>
    <artifactId>opentracing-spring-cloud-starter</artifactId>
    <version>0.0.7</version>
</dependency>
<!-- Jaeger -->
<dependency>
    <groupId>com.uber.jaeger</groupId>
    <artifactId>jaeger-core</artifactId>
    <version>0.21.0</version>
</dependency>
```
3. Next move to the maven project and add the Tracer Bean Definition

```bash
    @Bean
    public io.opentracing.Tracer jaegerTracer() {
     return new Configuration("spring-boot", new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
                new Configuration.ReporterConfiguration())
                .getTracer();
    }
```

4. Install Jaeger on Openshift to collect the traces

```bash
oc project jaeger
oc process -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/all-in-one/jaeger-all-in-one-template.yml | oc create -f -
```

5. Specify the url address of the Jaeger Collector

```bash
http.sender= http://jaeger-collector-jaeger.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/api/traces
```

and next define the httpSender class to access the Jaeger collector runnin on Openshift

```java
@Value("${http.sender}")
String URL;

@Bean
public Tracer jaegerTracer() {
    Sender sender = new HttpSender(URL);
    return new Configuration("spring-boot",
            new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
            new Configuration.ReporterConfiguration(sender))
            .getTracer();
}
```

6. Start Spring Boot

```bash
mvn spring-boot:run
```
7. Issue a http request and open the Jaeger console to fetch the traces

```bash
http http://localhost:8080/hello
HTTP/1.1 200 
Content-Length: 23
Content-Type: text/plain;charset=UTF-8
Date: Wed, 10 Jan 2018 16:00:50 GMT

Hello from Spring Boot!
```