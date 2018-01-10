# Instructions to play with Distributed Tracing

1. Install Jaeger on OpenShift to collect the traces

```bash
oc project jaeger
oc process -f https://raw.githubusercontent.com/jaegertracing/jaeger-openshift/master/all-in-one/jaeger-all-in-one-template.yml | oc create -f -
```

3. Create a route to access the Jaeger collector

```bash
oc expose service jaeger-collector --port=14268
```

2. Specify next the url address of the Jaeger Collector to be used

Get the route address

```bash
oc get route/jaeger-collector --template={{.spec.host}}      
```

Add the following property `http.sender` to the application.properties file with the route address of the collector

```bash
http.sender= http://jaeger-collector-jaeger.ocp.spring-boot.osepool.centralci.eng.rdu2.redhat.com/api/traces
```

and next configure the tracer to access the Jaeger collector running on Openshift

```java
@Value("${http.sender}")
String URL;

@Bean
public Tracer jaegerTracer() {
    Sender sender = new HttpSender(URL);
    Configuration.SenderConfiguration senderConfiguration = new Configuration.SenderConfiguration.Builder().sender(sender).build();
    return new Configuration("spring-boot",
            new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
            new Configuration.ReporterConfiguration(true, 10, 10, senderConfiguration))
            .getTracer();
}
```

3. Start Spring Boot

```bash
mvn spring-boot:run
```

4. Issue a http request

```bash
http http://localhost:8080/hello
HTTP/1.1 200 
Content-Length: 23
Content-Type: text/plain;charset=UTF-8
Date: Wed, 10 Jan 2018 16:00:50 GMT

Hello from Spring Boot!
```

5. Open the Jaeger console to fetch the traces

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