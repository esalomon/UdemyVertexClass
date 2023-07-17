package com.example.finished_api2.resources;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestApiVerticle extends AbstractVerticle {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        log.info("The {} verticle is starting...", RestApiVerticle.class.getSimpleName());
        startPromise.complete();
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {

        log.info("The {} verticle was stopped.", RestApiVerticle.class.getSimpleName());
        stopPromise.complete();
    }

}
