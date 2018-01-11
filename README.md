# Instructions to play with Distributed Tracing

## Spring Boot running locally 

1. Install Jaeger on OpenShift to collect the traces

```bash
oc project jaeger
oc process -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/all-in-one/jaeger-all-in-one-template.yml | oc create -f -
```

2. Create a route to access the Jaeger collector

```bash
oc expose service jaeger-collector --port=14268 -n jaeger
```

3. Specify next the url address of the Jaeger Collector to be used

Get the route address

```bash
oc get route/jaeger-collector --template={{.spec.host}} -n jaeger     
```

Add the following `jaeger` properties to the application.yml file with the route address of the collector

```bash
jaeger:
  protocol: HTTP
  sender: http://jaeger-collector-jaeger.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/api/traces
  protocol: 0
```

and next configure the tracer to access the Jaeger collector running on OpenShift

```java
@Value("${jaeger.sender}")
String JAEGER_URL;

@Value("${jaeger.protocol}")
String JAEGER_PROTOCOL;

@Value("${jaeger.port}")
int JAEGER_PORT;

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
```

4. Start Spring Boot

```bash
mvn spring-boot:run -Dspring.profiles.active=local
```

5. Issue a http request

```bash
http http://localhost:8080/hello
HTTP/1.1 200 
Content-Length: 23
Content-Type: text/plain;charset=UTF-8
Date: Wed, 10 Jan 2018 16:00:50 GMT

Hello from Spring Boot running on a local machine !'
```

6. Open the Jaeger console to fetch the traces

```bash
oc get route/jaeger-query --template={{.spec.host}} 
open https://jaeger-query-jaeger.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/search
```

or query it from a terminal

```bash
http --verify=no https://jaeger-query-jaeger.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/api/traces?service=spring-boot
HTTP/1.1 200 OK
Cache-control: private
Content-Type: application/json
Date: Wed, 10 Jan 2018 17:13:34 GMT
Set-Cookie: f2a76eea670eef399f3e86a8a443f3e5=713f7dc386ce37099352855f8ec66619; path=/; HttpOnly
Transfer-Encoding: chunked

{
    "data": [
        {
            "processes": {
                "p1": {
                    "serviceName": "spring-boot",
                    "tags": [
                        {
                            "key": "hostname",
                            "type": "string",
                            "value": "dabou"
                        },

```

## Spring Boot deployed on OpenShift with Jaeger Agent as sidecar

As Fabric8 Maven Plugin doesn't allow to easily add a side car container within the DeploymentConfig yaml file generated, then an OpenShift Template should be created 
to build/deploy it on OpenShift !!!!

1. Instructions to install the template, create a new app/deployment from the template and next start the s2i build

```bash
oc new-project demo
oc create -f openshift/spring-boot-tracing.yml
oc new-app spring-boot-tracing-template \
  -p SOURCE_REPOSITORY_URL=https://github.com/snowdrop/spring-boot-opentracing-booster.git
...
oc logs -f bc/spring-boot-tracing-s2i
```

2. To start a new build

```bash
oc start-build spring-boot-tracing-s2i
```
3. Get the route and curl the service

```bash
oc get route/spring-boot-tracing --template={{.spec.host}} 
http http://spring-boot-tracing-demo.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/hello
```

## Spring Boot on Istio using jaeger

TODO

