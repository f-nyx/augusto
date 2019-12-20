package be.rlab.augusto

import be.rlab.augusto.config.ApplicationBeans
import be.rlab.tehanu.SpringApplication
import be.rlab.tehanu.config.SlackBeans
import be.rlab.tehanu.config.TelegramBeans

class Main : SpringApplication() {

    override fun initialize() {
        applicationContext.apply {
            ApplicationBeans.beans().initialize(this)
            TelegramBeans.beans(resolveConfig()).initialize(this)
            SlackBeans.beans(resolveConfig()).initialize(this)
        }
    }
}

fun main() {
    Main().start()
}
