package com.redis.filteringapp;

import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisStreamService {

    private final JedisPooled jedisPooled;

    public RedisStreamService(JedisPooled jedisPooled) {
        this.jedisPooled = jedisPooled;
    }

    public void addToStream(String streamName, Map<String, String> hash) {
        jedisPooled.xadd(
                streamName,
                XAddParams.xAddParams()
                        .id(StreamEntryID.NEW_ENTRY)
                        .maxLen(1_000_000)
                        .exactTrimming(),
                hash
        );
    }

    public void acknowledgeMessage(
            String streamName,
            String consumerGroup,
            StreamEntry entry) {
        jedisPooled.xack(streamName, consumerGroup, entry.getID());
    }

    public void createConsumerGroup(String streamName, String consumerGroupName) {
        try {
            jedisPooled.xgroupCreate(streamName, consumerGroupName, new StreamEntryID("0-0"), true);
        } catch (JedisDataException e) {
            System.out.println("Group already exists");
        }
    }

    public List<Map.Entry<String, List<StreamEntry>>> readFromStream(
            String streamName, String consumerGroup, String consumer, int count) {

        Map<String, StreamEntryID> streams = new HashMap<>();
        streams.put(streamName, StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY);

        List<Map.Entry<String, List<StreamEntry>>> entries = jedisPooled.xreadGroup(
                consumerGroup,
                consumer,
                XReadGroupParams.xReadGroupParams().count(count),
                streams
        );

        return entries != null ? entries : Collections.emptyList();
    }
}
