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

import static com.example.finished_api2.constants.ConstantsApp.*;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

@Slf4j
public class ProductRestAPI {

    private ProductRestAPI() {
    }

    public static void attach(Vertx vertx, Router router) {

        router.get("/api/v1/products").handler(context -> getProductList(vertx, context));
        router.get("/api/v1/products/:" + PRODUCT_ID).handler(context -> getProductById(vertx, context));
    }

    private static void getProductList(Vertx vertx, RoutingContext context) {

        log.info("{} {} received a request and sent a message",
            GET_PRODUCT_LIST,
            context.normalizedPath());

        var command = new JsonObject()
            .put(COMMAND, GET_PRODUCT_LIST);

        vertx.eventBus().request(ConstantsApp.PRODUCT_MESSAGE, command.toString(), reply -> {

            if (reply.succeeded()) {
                handleSuccess(GET_PRODUCT_LIST, context, reply);
            }
            else {
                handleFailure(GET_PRODUCT_LIST, context, reply);
            }
        });
    }


    private static void getProductById(Vertx vertx, RoutingContext context) {

        String productId = context.request().getParam(PRODUCT_ID);
        log.info("{} {} received a request and sent a message",
            GET_PRODUCT_BY_ID,
            context.normalizedPath()
        );
        var command = new JsonObject()
            .put(COMMAND, GET_PRODUCT_BY_ID)
            .put(PRODUCT_ID, productId);

        vertx.eventBus().request(ConstantsApp.PRODUCT_MESSAGE, command.toString(), reply -> {

            if (reply.succeeded()) {
                if (NOT_FOUND.equals(reply.result().body().toString())) {
                    handleNotFount(GET_PRODUCT_BY_ID, context, reply);
                }
                else {
                    handleSuccess(GET_PRODUCT_BY_ID, context, reply);
                }
            }
            else {
                handleFailure(GET_PRODUCT_BY_ID, context, reply);
            }
        });
    }

    private static void handleNotFount(String action, RoutingContext context, AsyncResult<Message<Object>> reply) {

        log.info("{} {} got not found message",
            action,
            context.normalizedPath());

        var replyResults = new JsonObject()
            .put("message", "There are no product with the requested id");

        context.response()
            .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .end(replyResults.toBuffer());
    }

    private static void handleSuccess(String action, RoutingContext context, AsyncResult<Message<Object>> reply) {

        var replyResults = Json.encodePrettily(
            new JsonObject(reply.result().body().toString())
        );
        log.info("{} {} got success message: {}",
            action,
            context.normalizedPath(),
            replyResults);

        context.response()
            .setStatusCode(HttpResponseStatus.OK.code())
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .end(replyResults);
    }

    private static void handleFailure(String action, RoutingContext context, AsyncResult<Message<Object>> reply) {

        var replyResults = Json.encodePrettily(
            new JsonObject()
                .put("code", "ERROR-001")
                .put("message", reply.cause().getMessage())
        );
        log.info("{} {} got failure message:",
            action,
            context.normalizedPath(),
            reply.cause());

        context.response()
            .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
            .putHeader(CONTENT_TYPE, APPLICATION_JSON)
            .end(replyResults);
    }

}
