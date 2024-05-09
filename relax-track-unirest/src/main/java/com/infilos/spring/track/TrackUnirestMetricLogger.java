package com.infilos.spring.track;

import com.infilos.utils.Throws;
import kong.unirest.HttpRequestSummary;
import kong.unirest.MetricContext;
import kong.unirest.UniMetric;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class TrackUnirestMetricLogger implements UniMetric {

    @Override
    public MetricContext begin(HttpRequestSummary request) {
        long startTime = System.currentTimeMillis();
        return (responseSummary, exception) -> log.info("RequestMetrics==>: uri: {}, cost: {}ms, status: {}",
            request.getRawPath(),
            (System.currentTimeMillis() - startTime),
            Objects.nonNull(exception) ? Throws.getClassMessage(exception) : responseSummary.getStatusText()
        );
    }
}
