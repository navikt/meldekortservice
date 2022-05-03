package no.nav.meldeplikt.meldekortservice.mapper

import no.nav.meldeplikt.meldekortservice.model.MeldekortDagType
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Meldekortdetaljer
import no.nav.meldeplikt.meldekortservice.model.meldekortdetaljer.Sporsmal
import java.time.LocalDate
import java.util.*
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

object MeldekortTypeMapper {

    fun mapMeldekortType(meldekortdetaljer: Meldekortdetaljer): MeldekortType {
        val meldekortTypeSoap = MeldekortType()
        meldekortTypeSoap.id = meldekortdetaljer.id
        meldekortTypeSoap.hode = mapHodeType(meldekortdetaljer)
        meldekortTypeSoap.spm = mapSpmType(meldekortdetaljer.sporsmal)
        return meldekortTypeSoap
    }

    private fun mapHodeType(meldekortdetaljer: Meldekortdetaljer): HodeType {
        val hodeTypeSoap = HodeType()

        val personIdSoap = HodeType.PersonId()
        personIdSoap.verdi = meldekortdetaljer.personId
        hodeTypeSoap.personId = personIdSoap

        val fodselsnrSoap = HodeType.Fodselsnr()
        fodselsnrSoap.verdi = meldekortdetaljer.fodselsnr
        hodeTypeSoap.fodselsnr = fodselsnrSoap

        val meldekortIdSoap = HodeType.MeldekortId()
        meldekortIdSoap.verdi = meldekortdetaljer.meldekortId
        hodeTypeSoap.meldekortId = meldekortIdSoap

        hodeTypeSoap.meldeperiode = meldekortdetaljer.meldeperiode
        hodeTypeSoap.arkivnokkel = meldekortdetaljer.arkivnokkel
        hodeTypeSoap.kortType = meldekortdetaljer.kortType.code
        hodeTypeSoap.meldeDato = konverterDato(meldekortdetaljer.meldeDato)
        hodeTypeSoap.lestDato = konverterDato(meldekortdetaljer.lestDato)
        if(meldekortdetaljer.begrunnelse != null) {
            hodeTypeSoap.kommentar = meldekortdetaljer.begrunnelse
        }

        return hodeTypeSoap
    }

    private fun mapSpmType(sporsmal: Sporsmal?): SpmType {
        val spmTypeSoap = SpmType()
        spmTypeSoap.arbeidssoker = getSvarType(sporsmal?.arbeidssoker?: false)
        spmTypeSoap.arbeidet = getSvarType(sporsmal?.arbeidet?: false)
        spmTypeSoap.syk = getSvarType(sporsmal?.syk?: false)
        spmTypeSoap.annetFravaer = getSvarType(sporsmal?.annetFravaer?: false)
        spmTypeSoap.kurs = getSvarType(sporsmal?.kurs?: false)

        val forskuddSoap = VerdiBooleanType()
        forskuddSoap.isVerdi = sporsmal?.forskudd?: false
        spmTypeSoap.forskudd = forskuddSoap

        val signaturSoap = VerdiBooleanType()
        signaturSoap.isVerdi = true
        spmTypeSoap.signatur = signaturSoap

        val meldedagerSoap = SpmType.MeldekortDager()
        meldedagerSoap.meldekortDag.addAll(mapMeldekortDager(sporsmal))
        spmTypeSoap.meldekortDager = meldedagerSoap

        return spmTypeSoap
    }

    private fun mapMeldekortDager(sporsmal: Sporsmal?): List<MeldekortDagType> {
        val meldekortDagerSoap: MutableList<MeldekortDagType> = ArrayList(sporsmal?.meldekortDager?.size?: 0)
        if (sporsmal?.meldekortDager != null) {
            for (meldekortDag in sporsmal.meldekortDager) {
                val arbeidetTimerSumSoap = MeldekortDagType.ArbeidetTimerSum()
                arbeidetTimerSumSoap.verdi = meldekortDag.arbeidetTimerSum?: 0F

                val sykSoap = VerdiBooleanType()
                sykSoap.isVerdi = meldekortDag.syk?: false

                val annetFravaerSoap = VerdiBooleanType()
                annetFravaerSoap.isVerdi = meldekortDag.annetFravaer?: false

                val kursSoap = VerdiBooleanType()
                kursSoap.isVerdi = meldekortDag.kurs?: false

                val meldekortDagTypeSoap = MeldekortDagType()
                meldekortDagTypeSoap.dag = meldekortDag.dag
                meldekortDagTypeSoap.arbeidetTimerSum = arbeidetTimerSumSoap
                meldekortDagTypeSoap.syk = sykSoap
                meldekortDagTypeSoap.annetFravaer = annetFravaerSoap
                meldekortDagTypeSoap.kurs = kursSoap
                meldekortDagerSoap.add(meldekortDagTypeSoap)
            }
        }
        return meldekortDagerSoap
    }

    private fun getSvarType(verdi: Boolean): SvarType {
        val verdiBooleanTypeJa = VerdiBooleanType()
        val verdiBooleanTypeNei = VerdiBooleanType()
        val svarTypeSoap = SvarType()

        verdiBooleanTypeJa.isVerdi = verdi
        verdiBooleanTypeNei.isVerdi = !verdi

        svarTypeSoap.svarJa = verdiBooleanTypeJa
        svarTypeSoap.svarNei = verdiBooleanTypeNei

        return svarTypeSoap
    }

    private fun fromLocalDateToXMLGregorianCalendar(localDate: LocalDate): XMLGregorianCalendar {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.toString())
    }

    private fun konverterDato(dato: LocalDate?): XMLGregorianCalendar {
        return if(dato is LocalDate) {
            fromLocalDateToXMLGregorianCalendar(dato)
        } else {
            fromLocalDateToXMLGregorianCalendar(LocalDate.now())
        }
    }
}