package com.example.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.request.*
import java.io.File
import ch.qos.logback.classic.LoggerContext
import com.mongodb.MongoClient
import org.bson.Document
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import passForUser

fun Application.configureRouting() {

    routing {
        static {
            files("RoboPortal")
            default("RoboPortal/index.html")
        }
        post("/Login") { //обрабатываем форму Login, описанную в предыдущем коде на стороне html
            val receivedParams = call.receiveParameters() //получаем параметры, введенные на стороне браузера
            val login = receivedParams["login"] //берем логин
            val pass = receivedParams["password"] //берем пароль
            print("login=$login ") //выводим их в консоль
            println("pass=$pass")
            if (login == "Admin" && passForUser(login, pass!!)==true){
                call.respondFile(File("RoboPortal/admin.html"))
//создаем объект для регулирования вывода сообщений от MongoDB
            }
            else {
                call.respondFile(File("RoboPortal/userPage.html"))
            }
        }
        get("/demo") {
            call.respondFile(File("RoboPortal/socketBot.html")) // файл с интерфейсом чат-бота
        }
//        get("/") {
//            call.respondFile(File("RoboPortal/index.html"))
//        }
    }
}
