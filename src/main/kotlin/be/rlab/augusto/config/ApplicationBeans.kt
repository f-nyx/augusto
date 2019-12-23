package be.rlab.augusto.config

import be.rlab.augusto.domain.ConvertNumbers
import be.rlab.augusto.domain.NaturalService
import be.rlab.augusto.nlp.IndexManager
import com.typesafe.config.Config
import org.springframework.context.support.beans

object ApplicationBeans {
    fun beans(config: Config) = beans {
        // Listeners
        bean {
            ConvertNumbers(
                name = "ConvertNumbers"
            )
        }

        bean<NaturalService>()

        bean {
            IndexManager(
                indexPath = config.getConfig("bot").getString("index-path")
            )
        }
    }
}
