package flagma.server.app

import com.linecorp.armeria.common.SessionProtocol
import com.linecorp.armeria.common.metric.MeterIdPrefixFunction
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.healthcheck.HealthCheckService
import com.linecorp.armeria.server.metric.MetricCollectingService
import com.linecorp.armeria.server.metric.PrometheusExpositionService
import flagma.server.flag.FlagController
import flagma.server.flag.FlagStreamController
import flagma.server.project.ProjectController
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.future.await
import org.koin.core.Koin
import org.koin.core.context.startKoin

fun startKoin(): Koin {
    return startKoin { modules(Modules.appModule) }.koin
}

fun buildServer(koin: Koin, builder: ServerBuilder = Server.builder()): ServerBuilder {
    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

//    val distributionStatisticConfig = DistributionStatisticConfig.builder()
//        .percentilesHistogram(true)
//        .percentiles(0.0, 0.5, 0.9, 0.99, 1.0)
//        .percentilePrecision(2)
//        .minimumExpectedValue(1.0)
//        .maximumExpectedValue(Double.MAX_VALUE)
//        .expiry(3.minutes.toJavaDuration())
//        .bufferLength(3)
//        .build();
//
//    MoreMeters.setDistributionStatisticConfig(distributionStatisticConfig);


    return builder
        .port(9000, SessionProtocol.HTTP, SessionProtocol.HTTPS)
        .tlsSelfSigned()
        .meterRegistry(prometheusRegistry)
        .serviceUnder("/metrics", PrometheusExpositionService.of(prometheusRegistry.prometheusRegistry))
        .serviceUnder("/docs", DocService.builder().build())
        .annotatedService("/flags", koin.get<FlagController>())
        .annotatedService("/projects", koin.get<ProjectController>())
        .annotatedService("/stream/flags", koin.get<FlagStreamController>())
        .service("/healthcheck", HealthCheckService.of())
        .decorator(
            MetricCollectingService.builder
                (MeterIdPrefixFunction.ofDefault("flagma.http.service"))
                .newDecorator()
        )
}

suspend fun main() {
    val koin = startKoin()
    Initializer.initializeProject(koin.get())
    val server = buildServer(koin).build()

    server.start().await()
}
