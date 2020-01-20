package be.rlab.augusto.config

import be.rlab.augusto.command.Cancel
import be.rlab.augusto.command.ConvertNumbers
import be.rlab.augusto.domain.NaturalService
import be.rlab.search.IndexManager
import com.typesafe.config.Config
import org.springframework.context.support.beans

object ApplicationBeans {

    fun beans(config: Config) = beans {
        bean(isPrimary = true) {
            CustomHandlerProvider(config, ref())
        }

        // Listeners
        bean<ConvertNumbers>()
        bean<Cancel>()
        bean<NaturalService>()

        bean {
            IndexManager(
                indexPath = config.getConfig("bot").getString("index-path")
            )
        }
    }
}
