import org.bson.Document
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.util.JSON.serialize
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.security.MessageDigest

fun main(){
    print("Input string: ")
    val str = readLine()
    val strMD5 = str?.md5()
    println("md5 = $strMD5")
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun getUsers(): String {
    val loggerContext: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF
    val mongoUrl = "localhost"
    val mongoClient = MongoClient(mongoUrl, 27017)
    val mongoDatabase = mongoClient.getDatabase("Portal")
    var userCollection = mongoDatabase.getCollection("User")
    println("From Mongo = $userCollection")
    val usersCount = userCollection.countDocuments()
    println("usersCount = $usersCount")
    var users = ArrayList<Document>()
    val iter = userCollection.find()
    users.clear()
    iter.into(users)
    val usersString = usersToString(users) // функция описана ниже
    return usersString
}

fun usersToString(users: ArrayList<Document>): String{
    var usersString = """[""" //начало итоговой строки
    users.forEach { // для всех пользователей
        val json = serialize(it) //преобразуем объект в JSON-строку и добавляем к итоговой строке
        usersString = usersString + json + ""","""
    }
    usersString=usersString.dropLast(1) //удаляем последний символ - запятую, вместо нее вставляем
    usersString = usersString + """]""" // квадратную скобку
    println("usersString = $usersString ")
    return usersString
}

fun getCollectionFromDB(col: String): MongoCollection<Document> {
    val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = loggerContext.getLogger("org.mongodb.driver")
    rootLogger.level = Level.OFF //выключаем лишние сообщения от MongoDB
    val mongoUrl = "localhost" //адрес локального сервера MongoDB
    val mongoClient = MongoClient(mongoUrl, 27017) //клиент MongoDB
    val mongoDatabase = mongoClient.getDatabase("Portal") //выбираем БД
    var collection = mongoDatabase.getCollection(col) //выбираем коллекцию
    return collection //возвращаем коллекцию
}

fun passForUser(user: String, pass: String): Boolean{
    var passValid = false //по умолчанию результат равен false
    val userCollection = getCollectionFromDB("User") //получаем коллекцию из БД
    val docs = ArrayList<Document>() //массив документов
    val iter = userCollection.find() //итератор по коллекции
    docs.clear() //очищаем массив
    iter.into(docs) //вставляем данные из итератора в массив
    val findLogin = docs.filter { it["login"] == user } //ищем пользователя
    if (findLogin.isNotEmpty()) { //если юзер найден
        println("!Match! Login = ${findLogin.first()["login"]}")
        val user = findLogin.first() //получаем его данные
// и сравниваем хэши пароля из БД и с формы
        if (user["pass"] == pass?.md5()) { //если они равны
            passValid = true //то результат функции равен true
        }
    }
    return passValid // возвращаем результат проверки пароля
}

