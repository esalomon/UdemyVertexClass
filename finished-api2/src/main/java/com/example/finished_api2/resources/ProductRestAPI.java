package com.example.finished_api2.resources;

import com.example.finished_api2.constants.ConstantsApp;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductRestAPI {

    private ProductRestAPI() {
    }

    public static void attach(Vertx vertx, Router router) {

        router.get("/api/v1/products").handler(context -> getProductList(vertx, context));
    }

    private static void getProductList(Vertx vertx, RoutingContext context) {

        log.info("Get product list {} received a request and sent a message", context.normalizedPath());
        var command = new JsonObject()
            .put("command", "get-product-list");

        vertx.eventBus().request(ConstantsApp.MONGO_DB_MESSAGE, command.toString(), reply -> {

            if (reply.succeeded()) {
                handleSuccess(context, reply);
            }
            else {
                handleFailure(context, reply);
            }
        });
    }

    private static void handleSuccess(RoutingContext context, AsyncResult<Message<Object>> reply) {

        var replyResults = Json.encodePrettily(
            new JsonObject(reply.result().body().toString())
        );
        log.info("Get product list {} got success message: {}",
            context.normalizedPath(),
            replyResults);

        context.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(replyResults);
    }

    private static void handleFailure(RoutingContext context, AsyncResult<Message<Object>> reply) {

        var replyResults = Json.encodePrettily(
            new JsonObject()
                .put("code", "ERROR-001")
                .put("message", reply.cause().getMessage())
        );
        log.info("Get product list {} got failure message: {}",
            context.normalizedPath(),
            reply.cause());

        context.response()
            .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
            .putHeader("content-type", "application/json")
            .end(replyResults);
    }

}
