package at.d4m.spring.data.rest.reactive

import at.d4m.spring.data.repository.reactive.Change
import at.d4m.spring.data.repository.reactive.RxJava2ChangeFeedRepository
import at.d4m.spring.data.rethinkdb.template.NotFoundException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.core.ResolvableType
import org.springframework.core.convert.ConversionService
import org.springframework.data.repository.reactive.RxJava2CrudRepository
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.TimeUnit

/**
 * @author Christoph Muck
 */
@ResponseBody
open class ReactiveRestController(val conversionService: ConversionService) {

    @GetMapping("/{repository}")
    fun getAllEntities(info: ReactiveRestResourceInformation): Flowable<*> {
        val findAll = info.repositoryInformation.crudMethods.findAllMethod
        if (findAll.isPresent) {
            return findAll.get().invoke(info.repository) as Flowable<*>
        }
        throw MethodNotAllowedException(HttpMethod.GET, listOf(HttpMethod.POST))
    }

    @PostMapping("/{repository}")
    fun postEntity(entity: Any, info: ReactiveRestResourceInformation): Single<*> {
        val save = info.repositoryInformation.crudMethods.saveMethod
        if (save.isPresent) {
            return save.get().invoke(info.repository, entity) as Single<*>
        }
        throw MethodNotAllowedException(HttpMethod.POST, listOf(HttpMethod.GET))
    }

    @GetMapping("/{repository}/changes", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun getChanges(info: ReactiveRestResourceInformation): ResponseEntity<Flowable<out Change<Any?>>> {
        val repo = info.repository as? RxJava2ChangeFeedRepository<*, *>

        val responseHeaders = HttpHeaders()
        responseHeaders.cacheControl = "no-cache"
        responseHeaders.set("X-Accel-Buffering", "no")

        return repo?.let { ResponseEntity.ok().headers(responseHeaders).body(repo.changeFeed()) }
                ?: ResponseEntity.notFound().headers(responseHeaders).build()
    }

    @DeleteMapping("/{repository}")
    fun deleteAll(info: ReactiveRestResourceInformation): Completable {
        return (info.repository as? RxJava2CrudRepository<*, *>)?.deleteAll()
                ?: throw MethodNotAllowedException(HttpMethod.DELETE, listOf(HttpMethod.GET, HttpMethod.POST))
    }

    @DeleteMapping("/{repository}/{id}")
    fun deleteEntity(@PathVariable id: String, info: ReactiveRestResourceInformation): Completable {
        val idType = info.repositoryInformation.idType
        val convertedId = conversionService.convert(id, idType)!!
        return ((info.repository as? RxJava2CrudRepository<*, Any>)?.deleteById(convertedId)
                ?: throw MethodNotAllowedException(HttpMethod.DELETE, listOf()))
                .onErrorResumeNext {
                    var error: Throwable = it
                    if (it is NotFoundException) {
                        error = ResponseStatusException(HttpStatus.NOT_FOUND, null, it)
                    }
                    Completable.error(error)
                }
    }
}
