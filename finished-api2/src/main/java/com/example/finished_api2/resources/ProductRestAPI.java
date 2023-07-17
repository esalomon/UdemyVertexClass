package com.example.finished_api2.resources;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductRestAPI {

    private ProductRestAPI() {
    }

    public static void attach(Router router) {

        router.get("/api/v1/products").handler(ProductRestAPI::getProductList);
    }

    private static void getProductList(RoutingContext context) {

        var response = new JsonObject()
            .put("message", "get product list was executed");

        log.info("Get product list {} responds with {}", context.normalizedPath(), response.encode());
        context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(response.toBuffer());
    }
}
