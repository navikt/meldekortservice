package no.nav.meldeplikt.meldekortservice.model.enum

import com.fasterxml.jackson.annotation.JsonCreator

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

    companion object {
        private var values: MutableMap<String, KortType>? = null

        init {
            values = HashMap()
            for (kortType in values()) {
                values!![kortType.code] = kortType
            }
        }

        @JsonCreator
        fun getByCode(code: String): KortType {
            val kortType = values!![code]
            return kortType ?: throw IllegalArgumentException("Ugyldig kode [ $code ]")
        }
    }
}