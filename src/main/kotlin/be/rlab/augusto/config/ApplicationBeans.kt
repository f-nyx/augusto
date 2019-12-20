package be.rlab.augusto.config

import be.rlab.augusto.domain.CountNumbers
import org.springframework.context.support.beans

object ApplicationBeans {
    fun beans() = beans {
        // Listeners
        bean {
            CountNumbers(
                name = "augusto"
            )
        }
    }
}
