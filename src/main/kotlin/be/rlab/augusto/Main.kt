package be.rlab.augusto

import be.rlab.augusto.config.ApplicationBeans
import be.rlab.augusto.domain.NaturalService
import be.rlab.tehanu.SpringApplication
import be.rlab.tehanu.config.TelegramBeans
import org.springframework.beans.factory.getBean

class Main : SpringApplication() {

    override fun initialize() {
        applicationContext.apply {
            ApplicationBeans.beans(resolveConfig()).initialize(this)
            //SlackBeans.beans(resolveConfig()).initialize(this)
            TelegramBeans.beans(resolveConfig()).initialize(this)
        }
    }

    override fun ready() {
        val naturalService: NaturalService = applicationContext.getBean()
        naturalService.loadTrainingData()
    }
}

fun main() {
    Main().start()
}
