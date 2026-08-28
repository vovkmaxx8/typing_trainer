// TypingTrainer.kt
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.random.Random

class TypingTrainer {
    @Parameter(names = ["--length"])
    private var length: Int = 50

    @Parameter(names = ["--charset"])
    private var charset: String = "all"

    @Parameter(names = ["--mode"])
    private var mode: String = "char"

    @Parameter(names = ["--language"])
    private var language: String = "en"

    @Parameter(names = ["--export-stats"])
    private var exportFile: String? = null

    private lateinit var text: String
    private var errors = 0
    private var totalChars = 0
    private var startTime = 0L
    private var endTime = 0L

    private fun getCharset(): String {
        val letters = if (language == "ru") {
            "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        } else {
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        }
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=,.;:?/[]{}|"
        return when (charset) {
            "letters" -> letters
            "alnum" -> letters + digits
            else -> letters + digits + symbols
        }
    }

    private fun generateText(): String {
        val cs = getCharset()
        return when (mode) {
            "word" -> {
                val wordCount = maxOf(1, length / 5)
                (1..wordCount).map {
                    val wl = Random.nextInt(3, 8)
                    (1..wl).map { cs[Random.nextInt(cs.length)] }.joinToString("")
                }.joinToString(" ")
            }
            "sentence" -> {
                val sentCount = maxOf(1, length / 30)
                (1..sentCount).map {
                    val wc = Random.nextInt(5, 12)
                    val words = (1..wc).map {
                        val wl = Random.nextInt(3, 8)
                        (1..wl).map { cs[Random.nextInt(cs.length)] }.joinToString("")
                    }
                    var sent = words.joinToString(" ")
                    sent = sent.replaceFirstChar { it.uppercase() }
                    sent + "."
                }.joinToString(" ")
            }
            else -> (1..length).map { cs[Random.nextInt(cs.length)] }.joinToString("")
        }
    }

    fun run() {
        text = generateText()
        println("\u001B[36mТренажер печати (символы)\u001B[0m")
        println("\u001B[33mВведите следующий текст как можно точнее и быстрее:\u001B[0m")
        println("\u001B[37m$text\u001B[0m")
        println("\nНажмите Enter, чтобы начать...")
        readLine()
        print("\u001B[32m> \u001B[0m")
        startTime = System.currentTimeMillis()
        val input = readLine() ?: ""
        endTime = System.currentTimeMillis()
        totalChars = input.length
        val minLen = minOf(text.length, input.length)
        errors = 0
        for (i in 0 until minLen) {
            if (text[i] != input[i]) errors++
        }
        if (input.length > text.length) errors += input.length - text.length
        val elapsed = (endTime - startTime) / 1000.0
        val cpm = if (elapsed > 0) (totalChars / elapsed) * 60 else 0.0
        val wpm = cpm / 5
        val accuracy = if (totalChars > 0) ((totalChars - errors).toDouble() / totalChars * 100) else 0.0
        println("\n\u001B[36m--- Результаты ---\u001B[0m")
        println("\u001B[32mСкорость: ${"%.1f".format(cpm)} симв/мин (${"%.1f".format(wpm)} слов/мин)\u001B[0m")
        println("\u001B[33mТочность: ${"%.1f".format(accuracy)}%\u001B[0m")
        println("\u001B[35mВремя: ${"%.2f".format(elapsed)} сек\u001B[0m")
        println("\u001B[31mОшибок: $errors\u001B[0m")

        exportFile?.let {
            val stats = mapOf("cpm" to cpm, "wpm" to wpm, "accuracy" to accuracy, "time" to elapsed, "errors" to errors)
            val gson = GsonBuilder().setPrettyPrinting().create()
            File(it).writeText(gson.toJson(stats))
            println("Статистика сохранена в $it")
        }
    }
}

fun main(args: Array<String>) {
    val trainer = TypingTrainer()
    JCommander.newBuilder().addObject(trainer).build().parse(*args)
    trainer.run()
}
