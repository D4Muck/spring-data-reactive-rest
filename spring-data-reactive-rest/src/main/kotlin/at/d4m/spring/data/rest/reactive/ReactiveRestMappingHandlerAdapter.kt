package at.d4m.spring.data.rest.reactive

import at.d4m.spring.data.rest.reactive.override.AbstractMessageReaderArgumentResolver
import org.springframework.core.MethodParameter
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * @author Christoph Muck
 */
@Order(-1)
open class ReactiveRestMappingHandlerAdapter(
        val repositories: Map<String, ReactiveRestResourceInformation>
) : RequestMappingHandlerAdapter() {

    init {
        val configurer = ArgumentResolverConfigurer()
        configurer.addCustomResolver(
                RepositoryInformationArgumentResolver(),
                EntityBodyArgumentResolver()
        )
        argumentResolverConfigurer = configurer
    }

    override fun supports(handler: Any): Boolean {
        return handler is HandlerMethod && handler.method.declaringClass == ReactiveRestController::class.java
    }

    override fun handle(exchange: ServerWebExchange?, handler: Any?): Mono<HandlerResult> {
        return super.handle(exchange, handler)
    }

    inner class RepositoryInformationArgumentResolver : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.parameterType == ReactiveRestResourceInformation::class.java
        }

        override fun resolveArgument(parameter: MethodParameter, bindingContext: BindingContext, exchange: ServerWebExchange): Mono<Any> {
            val path = exchange.request.path.pathWithinApplication().elements().getOrNull(1)?.value()
            return Mono.just(repositories[path]!!)
        }
    }

    inner class EntityBodyArgumentResolver : AbstractMessageReaderArgumentResolver(ServerCodecConfigurer.create().readers) {

        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.parameterType == Any::class.java
        }

        override fun resolveArgument(parameter: MethodParameter, bindingContext: BindingContext, exchange: ServerWebExchange): Mono<Any> {
            return super.readBody(parameter, true, bindingContext, exchange)
        }

        override fun getCustomBodyType(bodyParameter: MethodParameter, isBodyRequired: Boolean, bindingContext: BindingContext, exchange: ServerWebExchange): ResolvableType {
            val path = exchange.request.path.pathWithinApplication().elements().getOrNull(1)?.value()
            return ResolvableType.forClass(repositories[path]!!.repositoryInformation.domainType)
        }
    }
}
