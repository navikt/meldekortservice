package no.nav.meldeplikt.meldekortservice.model.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class KortType constructor(val code: String) {
    ORDINAER("01"),
    ERSTATNING("03"),
    RETUR("04"),
    ELEKTRONISK("05"),
    AAP("06"),
    ORDINAER_MANUELL("07"),
    MASKINELT_OPPDATERT("08"),
    MANUELL_ARENA("09"),
    KORRIGERT_ELEKTRONISK("10");

    override fun toString(): String {
        return code
    }

    @JsonValue
    fun getName(): String {
        return this.name
    }

    companion object {
        private var values: MutableMap<String, KortType>? = null

        init {
            values = HashMap()
            for (kortType in entries) {
                values!![kortType.code] = kortType
            }
        }

        fun getByCode(code: String): KortType {
            val kortType = values!![code]
            return kortType ?: throw IllegalArgumentException("Ugyldig kode [ $code ]")
        }

        @JsonCreator
        fun getByName(name: String): KortType {
            return KortType.valueOf(name)
        }
    }
}
