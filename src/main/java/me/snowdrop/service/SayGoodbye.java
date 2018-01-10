package me.snowdrop.service;

import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;

public class SayGoodbye {

    @Autowired
    private static Tracer tracer;

    public static String getMessage() {
        tracer.buildSpan("Calling SayGoodBye service");
        return "Say Goodbye !";
    }
}
