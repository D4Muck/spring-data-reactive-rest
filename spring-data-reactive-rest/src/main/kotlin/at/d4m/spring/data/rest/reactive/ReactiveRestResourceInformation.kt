package at.d4m.spring.data.rest.reactive

import org.springframework.data.repository.core.RepositoryInformation

data class ReactiveRestResourceInformation(
        val repositoryInformation: RepositoryInformation,
        val repository: Any
)