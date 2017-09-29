package at.d4m.spring.data.rest.reactive

import org.springframework.http.server.reactive.ServerHttpRequest

/**
 * @author Christoph Muck
 */
val ServerHttpRequest.rootPath: String?
    get() {
        return this.path.pathWithinApplication().elements().getOrNull(1)?.value()
    }