import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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
            println("Saving: ${outPath}")
            outPath.writeBytes(body)
        }

    }

}

fun main() {

    for (i in 1..6) {

        val f = File( "src/main/resources/00${i}.html")

        val doc = Jsoup.parse(f, "utf-8", "https://www.sportograf.com")

        doc.select(".image").forEach {
            val url = it.attr("src").replace("thumbnail", "preview")
            downloadFile(url)
        }

    }

}