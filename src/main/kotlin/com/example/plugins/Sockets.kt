package com.example.plugins

import getUsers
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/") { // websocketSession
            for (frame in incoming) { // цикл по всем данным, пришедшим из сокета
                when (frame) { // переключатель по видам пришедших данных
                    is Frame.Text -> { //если это обычный текст
                        val text = frame.readText() // берем текст
                        println("From site: $text") // выводим его в консоль
                        if (text.lowercase() in arrayListOf("hello", "hi") ) { // и в зависимости от содержимого
                            outgoing.send(Frame.Text("Hello. What can I do?")) // отправляем ответный текст
                        }
                        else if (text.lowercase() in arrayListOf("добрый день", "здравствуйте", "привет")){
                            outgoing.send(Frame.Text("Добрый день. Чем помочь?"))
                        }
                        else if (text!="StartBot"){ // если текст не подходит под предыдущие варианты
                            outgoing.send(Frame.Text("Не понимаю. I don't understand.")) // и неравен StartBot,
                        } // то выводим соответствующий текст
                    }
                }
            }
        }
        webSocket("Users"){
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        println("From site2: $text")
                        if (text=="GetUsers"){
                            val users = getUsers()
                            outgoing.send(Frame.Text(users))
                        }
                    }
                }
            }
        }
    }
}
