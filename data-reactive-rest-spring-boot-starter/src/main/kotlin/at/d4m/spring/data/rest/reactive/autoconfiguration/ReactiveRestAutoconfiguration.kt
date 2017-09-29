package at.d4m.spring.data.rest.reactive.autoconfiguration

import at.d4m.spring.data.rest.reactive.ReactiveRestController
import at.d4m.spring.data.rest.reactive.ReactiveRestHandlerMapping
import at.d4m.spring.data.rest.reactive.ReactiveRestMappingHandlerAdapter
import at.d4m.spring.data.rest.reactive.ReactiveRestResourceInformation
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.support.Repositories

/**
 * @author Christoph Muck
 */
@Configuration
open class ReactiveRestAutoconfiguration : InitializingBean {

    @Autowired private lateinit var applicationContext: ApplicationContext

    private lateinit var pathMappedRepositoryInformation: Map<String, ReactiveRestResourceInformation>

    override fun afterPropertiesSet() {
        val repositories = Repositories(applicationContext)
        pathMappedRepositoryInformation = repositories.associateBy { it.simpleName.decapitalize() }
                .mapValues {
                    ReactiveRestResourceInformation(
                            repositoryInformation = repositories.getRequiredRepositoryInformation(it.value),
                            repository = repositories.getRepositoryFor(it.value).get()
                    )
                }
    }

    @Bean
    @ConditionalOnMissingBean
    open fun reactiveRestController(): ReactiveRestController {
        return ReactiveRestController()
    }

    @Bean
    @ConditionalOnMissingBean
    open fun reactiveRestHandlerMapping(): ReactiveRestHandlerMapping {
        return ReactiveRestHandlerMapping(pathMappedRepositoryInformation)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun reactiveRestMappingHandlerAdapter(): ReactiveRestMappingHandlerAdapter {
        return ReactiveRestMappingHandlerAdapter(pathMappedRepositoryInformation)
    }
}