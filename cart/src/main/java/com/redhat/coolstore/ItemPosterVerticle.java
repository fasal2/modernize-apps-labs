package com.redhat.coolstore;

import com.redhat.coolstore.model.Product;
import com.redhat.coolstore.utils.Generator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;

// -change- class added
public class ItemPosterVerticle extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(ItemPosterVerticle.class.getName());

    // use producer for interacting with Apache Kafka
    private KafkaProducer<String, String> producer;
    private String kafkaTopic;

    @Override
    public void start() {
        logger.info("Starting " + this.getClass().getSimpleName());

        kafkaTopic = config().getString("itemposter.kafkatopic");
        producer = KafkaProducer.create(vertx, kafkaConfig());

        EventBus eb = vertx.eventBus();
        MessageConsumer<String> consumer = eb.consumer("item");

        consumer.handler(message -> {
            logger.info("Posting item to the kafka topic" + message.body());

            producer.write(KafkaProducerRecord.create(kafkaTopic, message.body()));
        });
    }

    private Map<String, String> kafkaConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", config().getString("bootstrap.servers"));
        config.put("key.serializer", config().getString("key.serializer"));
        config.put("value.serializer", config().getString("value.serializer"));
        config.put("acks", config().getString("acks"));

        return config;
    }

}
