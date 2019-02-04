package moe.yuuta.randompicture

import io.vertx.core.AbstractVerticle
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import java.io.File
import kotlin.random.Random

class MainVerticle : AbstractVerticle() {
    companion object {
        const val ENV_PATH = "RP_PATH"
        const val PORT = 8080

        private const val PATH = "/"
        const val PATH_IMAGE = PATH

        val ACCEPTABLE_FILES = arrayOf(
            ".jpg",
            ".jpeg",
            ".raw",
            ".gif",
            ".bmp",
            ".webp",
            ".png",
            ".img",
            ".svg")
    }

    private val logger: Logger = LoggerFactory.getLogger("Main")
    private val mIndex: MutableList<String> = mutableListOf()

    override fun start(startFuture: Future<Void>) {
        var path = System.getenv(ENV_PATH)
        if (path == null) {
            path = "/app/favourite"
        }
        val pathFile = File(path)
        if (pathFile.isFile || !pathFile.exists()) {
            startFuture.fail("$ENV_PATH is not found or is a file.")
            return
        }
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        router.route(HttpMethod.GET, PATH_IMAGE).handler {
            it.response()
                .putHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .setStatusCode(200)
                .sendFile(mIndex[Random.nextInt(mIndex.size)])
        }
        server.requestHandler(router)
        CompositeFuture.all(Future.future<Any> {
            logger.info("Indexing images")
            index(pathFile)
            logger.info("Done, size: ${mIndex.size}")
            it.complete()
        }, Future.future<HttpServer> { server.listen(PORT, it) })
            .setHandler {
            if (it.succeeded()) startFuture.complete()
            else startFuture.fail(it.cause())
        }
    }

    private fun index(rootFolder: File) {
        rootFolder.listFiles { dir, name ->
            if (!File(dir, name).isFile) {
                // Remain to the result so it will be entered later
                return@listFiles true
            }
            for (acceptableName in ACCEPTABLE_FILES) {
                if (name.toLowerCase().endsWith(acceptableName.toLowerCase())) {
                    return@listFiles true
                }
            }
            logger.warn("Ignoring unacceptable file $name")
            return@listFiles false
        }.toList()
            .stream()
            .forEach { file ->
                if (file.isFile) {
                    mIndex.add(file.absolutePath)
                } else {
                    logger.info("Entering folder ${file.absolutePath}")
                    index(file)
                }
            }
    }
}