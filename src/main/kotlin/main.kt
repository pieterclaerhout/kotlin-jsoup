import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object Main {

    init {
        val rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        rootLogger.level = Level.INFO
    }

    val log = LoggerFactory.getLogger(Main::class.java)

    val client = OkHttpClient.Builder().build()

    fun downloadFile(url: String) {

        val request = Request.Builder().url(URL(url)).get().build()

        val response = client.newCall(request).execute()
        if (response.code == HttpURLConnection.HTTP_OK) {

            val body = response.body?.bytes()

            val outDir = File("/Users/pclaerhout/Desktop/fre")
            outDir.mkdirs()

            val outPath = File(outDir, File(URL(url).path).name)

            if (body != null) {
                log.info("Saving: ${outPath}")
                outPath.writeBytes(body)
            }

        }

    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val urls = mutableSetOf<String>()

        for (i in 1..6) {

            val f = File("src/main/resources/00${i}.html")

            val doc = Jsoup.parse(f, "utf-8", "https://www.sportograf.com")

            doc.select(".image").forEach {
                val url = it.attr("src").replace("thumbnail", "preview")
                urls.add(url)
            }

        }

        log.info("Downloading ${urls.size} urls")
        val requestSemaphore = Semaphore(32)

        val futures = urls.map {
            launch {
                // Will limit number of concurrent requests to 5
                requestSemaphore.withPermit {
                    downloadFile(it)
                }
            }
        }

        futures.joinAll()

    }

}