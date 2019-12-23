package be.rlab.augusto

import be.rlab.augusto.config.ApplicationBeans
import be.rlab.augusto.domain.NaturalCommand
import be.rlab.augusto.domain.NaturalService
import be.rlab.augusto.nlp.TextClassifier
import be.rlab.tehanu.SpringApplication
import be.rlab.tehanu.config.SlackBeans
import org.springframework.beans.factory.getBean

class Main : SpringApplication() {

    override fun initialize() {
        applicationContext.apply {
            ApplicationBeans.beans(resolveConfig()).initialize(this)
            SlackBeans.beans(resolveConfig()).initialize(this)
        }
    }

    override fun ready() {
        applicationContext.getBeansOfType(NaturalCommand::class.java).forEach { (_, instance) ->
            instance.textClassifier = TextClassifier(
                indexManager = applicationContext.getBean(),
                namespace = instance.name
            )
        }

        val naturalService: NaturalService = applicationContext.getBean()
        naturalService.loadTrainingData()
    }
}

fun main() {
    Main().start()
}
