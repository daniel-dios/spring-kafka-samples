package com.example;

import com.google.protobuf.MessageLite;
import org.apache.kafka.common.serialization.Serializer;

public class ProtoSerDer<T extends MessageLite> implements Serializer<T> {

    @Override
    public byte[] serialize(final String topic, final T data) {
        return data.toByteArray();
    }
}