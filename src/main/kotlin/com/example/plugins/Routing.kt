package com.example.plugins

import Account
import addUser
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
import getUsersList
import md5
import passForUser
import toExcel

fun Application.configureRouting() {
    var curUser: String = ""
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
                curUser = "Admin"
//создаем объект для регулирования вывода сообщений от MongoDB
            }
            else {
                call.respondFile(File("RoboPortal/userPage.html"))
            }
        }

        post("AddUser"){
            val receivedParams = call.receiveParameters() //получаем параметры запроса
            val fio = receivedParams["FIO"] //берем нужные поля
            val status = receivedParams["status"]
            val login = receivedParams["login"]
            val pass = receivedParams["password"]
            val newAcc = Account(login, fio!!, status!!, pass!!.md5()) //создаем объект Account
            println("newAcc = $newAcc") //вывод для проверки
            addUser(newAcc) //вызываем функцию для добавления нового пользователя в MongoDB
            call.respondFile(File("RoboPortal/admin.html")) //перегружаем html-файл
        }
        get("/admin.html") {
            if (curUser=="Admin") call.respondFile(File("RoboPortal/admin.html"))
            else call.respondFile(File("RoboPortal/index.html"))
        }

        get("Logout"){
            val receivedParams = call.request.queryParameters["login"]
            println("logout from ${receivedParams}")
            curUser = ""
            call.respondFile(File("RoboPortal/index.html"))
        }
        get("/demo") {
            call.respondFile(File("RoboPortal/socketBot.html")) // файл с интерфейсом чат-бота
        }

        post ("ToMSOffice") {
            val receivedParams = call.receiveParameters() //получаем параметры запроса
            println("To office = ")
            when (receivedParams["toOffice"]){ //в зависимости от параметра
                "Excel" -> { //действия для выдачи Excel-файла
                    println("Excel")
                    val file = toExcel(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
                    if(file!!.exists()) { //если файл существует
                        call.respondFile(file) //отправляем в браузер
                    } else call.respond(HttpStatusCode.NotFound)
                }
//                "Word" -> { //действия для выдачи Word-файла
//                    println("Word")
//                    val file = toWordDocx(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
//                    if(file!!.exists()) { //если файл существует
//                        call.respondFile(file) //отправляем в браузер
//                    } else call.respond(HttpStatusCode.NotFound)
//                }
            }
        }
//        get("/") {
//            call.respondFile(File("RoboPortal/index.html"))
//        }
    }
}
