package at.d4m.spring.data.rest.reactive

import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.server.ServerWebExchange

/**
 * @author Christoph Muck
 */
class ReactiveRestHandlerMapping(
        val repositories: Map<String, ReactiveRestResourceInformation>
) : RequestMappingHandlerMapping() {

    init {
        order = 0
    }

    override fun isHandler(beanType: Class<*>): Boolean {
        return beanType == ReactiveRestController::class.java
    }

    override fun lookupHandlerMethod(exchange: ServerWebExchange): HandlerMethod? {
        val path = exchange.request.path.pathWithinApplication().elements().getOrNull(1)?.value()
        if (repositories.containsKey(path)) {
            return super.lookupHandlerMethod(exchange)
        }
        return null
    }
}
