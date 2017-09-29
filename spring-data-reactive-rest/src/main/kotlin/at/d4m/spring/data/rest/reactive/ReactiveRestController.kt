package at.d4m.spring.data.rest.reactive

import at.d4m.spring.data.repository.reactive.Change
import at.d4m.spring.data.repository.reactive.RxJava2ChangeFeedRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.springframework.data.repository.reactive.RxJava2CrudRepository
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.MethodNotAllowedException
import java.util.concurrent.TimeUnit

/**
 * @author Christoph Muck
 */
@ResponseBody
open class ReactiveRestController {

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

    @GetMapping("/{repository}/changes", produces = arrayOf(MediaType.TEXT_EVENT_STREAM_VALUE))
    fun getChanges(info: ReactiveRestResourceInformation): ResponseEntity<Flowable<out Change<Any?>>> {
        val repo = info.repository as? RxJava2ChangeFeedRepository<*, *>
        return repo?.let { ResponseEntity.ok(repo.changeFeed()) } ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{repository}")
    fun deleteAll(info: ReactiveRestResourceInformation): Completable {
        return (info.repository as? RxJava2CrudRepository<*, *>)?.deleteAll()
                ?: throw MethodNotAllowedException(HttpMethod.DELETE, listOf(HttpMethod.GET, HttpMethod.POST))
    }
}
