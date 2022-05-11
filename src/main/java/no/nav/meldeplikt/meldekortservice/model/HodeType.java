package no.nav.meldeplikt.meldekortservice.model;

import javax.xml.datatype.XMLGregorianCalendar;

public class HodeType {

    protected HodeType.PersonId personId;
    protected HodeType.Fodselsnr fodselsnr;
    protected HodeType.MeldekortId meldekortId;
    protected String meldeperiode;
    protected String arkivnokkel;
    protected String kortType;
    protected XMLGregorianCalendar meldeDato;
    protected XMLGregorianCalendar lestDato;
    protected String kommentar;

    public HodeType.PersonId getPersonId() {
        return personId;
    }

    public void setPersonId(HodeType.PersonId value) {
        this.personId = value;
    }

    public HodeType.Fodselsnr getFodselsnr() {
        return fodselsnr;
    }

    public void setFodselsnr(HodeType.Fodselsnr value) {
        this.fodselsnr = value;
    }

    public HodeType.MeldekortId getMeldekortId() {
        return meldekortId;
    }

    public void setMeldekortId(HodeType.MeldekortId value) {
        this.meldekortId = value;
    }

    public String getMeldeperiode() {
        return meldeperiode;
    }

    public void setMeldeperiode(String value) {
        this.meldeperiode = value;
    }

    public String getArkivnokkel() {
        return arkivnokkel;
    }

    public void setArkivnokkel(String value) {
        this.arkivnokkel = value;
    }

    public String getKortType() {
        return kortType;
    }

    public void setKortType(String value) {
        this.kortType = value;
    }

    public XMLGregorianCalendar getMeldeDato() {
        return meldeDato;
    }

    public void setMeldeDato(XMLGregorianCalendar value) {
        this.meldeDato = value;
    }

    public XMLGregorianCalendar getLestDato() {
        return lestDato;
    }

    public void setLestDato(XMLGregorianCalendar value) {
        this.lestDato = value;
    }

    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String value) {
        this.kommentar = value;
    }

    public static class Fodselsnr {

        protected String verdi;

        public String getVerdi() {
            return verdi;
        }

        public void setVerdi(String value) {
            this.verdi = value;
        }
    }

    public static class MeldekortId {

        protected long verdi;

        public long getVerdi() {
            return verdi;
        }

        public void setVerdi(long value) {
            this.verdi = value;
        }
    }

    public static class PersonId {

        protected long verdi;

        public long getVerdi() {
            return verdi;
        }

        public void setVerdi(long value) {
            this.verdi = value;
        }

    }
}
