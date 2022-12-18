package homework03

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSetter
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
data class AboutSnapshot(
    val dateCreated: Date,
    val online: Long,
    val description: String
) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun create(
            @JsonSetter("created") dateCreated: Double,
            @JsonSetter("accounts_active") online: Long,
            @JsonSetter("public_description") description: String
        ) = AboutSnapshot(Date(dateCreated.toLong() * 1000), online, description)
    }
}



