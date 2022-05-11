package no.nav.meldeplikt.meldekortservice.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HodeType", propOrder = {
        "personId",
        "fodselsnr",
        "meldekortId",
        "meldeperiode",
        "arkivnokkel",
        "kortType",
        "meldeDato",
        "lestDato",
        "kommentar"
})
public class HodeType {

    @XmlElement(name = "PersonId", required = true)
    protected HodeType.PersonId personId;
    @XmlElement(name = "Fodselsnr", required = true)
    protected HodeType.Fodselsnr fodselsnr;
    @XmlElement(name = "MeldekortId", required = true)
    protected HodeType.MeldekortId meldekortId;
    @XmlElement(name = "Meldeperiode", required = true)
    protected String meldeperiode;
    @XmlElement(name = "Arkivnokkel", required = true)
    protected String arkivnokkel;
    @XmlElement(name = "KortType", required = true)
    protected String kortType;
    @XmlElement(name = "MeldeDato", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar meldeDato;
    @XmlElement(name = "LestDato", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar lestDato;
    @XmlElement(name = "Kommentar")
    protected String kommentar;

    /**
     * Gets the value of the personId property.
     *
     * @return
     *     possible object is
     *     {@link HodeType.PersonId }
     *
     */
    public HodeType.PersonId getPersonId() {
        return personId;
    }

    /**
     * Sets the value of the personId property.
     *
     * @param value
     *     allowed object is
     *     {@link HodeType.PersonId }
     *
     */
    public void setPersonId(HodeType.PersonId value) {
        this.personId = value;
    }

    /**
     * Gets the value of the fodselsnr property.
     *
     * @return
     *     possible object is
     *     {@link HodeType.Fodselsnr }
     *
     */
    public HodeType.Fodselsnr getFodselsnr() {
        return fodselsnr;
    }

    /**
     * Sets the value of the fodselsnr property.
     *
     * @param value
     *     allowed object is
     *     {@link HodeType.Fodselsnr }
     *
     */
    public void setFodselsnr(HodeType.Fodselsnr value) {
        this.fodselsnr = value;
    }

    /**
     * Gets the value of the meldekortId property.
     *
     * @return
     *     possible object is
     *     {@link HodeType.MeldekortId }
     *
     */
    public HodeType.MeldekortId getMeldekortId() {
        return meldekortId;
    }

    /**
     * Sets the value of the meldekortId property.
     *
     * @param value
     *     allowed object is
     *     {@link HodeType.MeldekortId }
     *
     */
    public void setMeldekortId(HodeType.MeldekortId value) {
        this.meldekortId = value;
    }

    /**
     * Gets the value of the meldeperiode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMeldeperiode() {
        return meldeperiode;
    }

    /**
     * Sets the value of the meldeperiode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMeldeperiode(String value) {
        this.meldeperiode = value;
    }

    /**
     * Gets the value of the arkivnokkel property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getArkivnokkel() {
        return arkivnokkel;
    }

    /**
     * Sets the value of the arkivnokkel property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setArkivnokkel(String value) {
        this.arkivnokkel = value;
    }

    /**
     * Gets the value of the kortType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getKortType() {
        return kortType;
    }

    /**
     * Sets the value of the kortType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setKortType(String value) {
        this.kortType = value;
    }

    /**
     * Gets the value of the meldeDato property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getMeldeDato() {
        return meldeDato;
    }

    /**
     * Sets the value of the meldeDato property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setMeldeDato(XMLGregorianCalendar value) {
        this.meldeDato = value;
    }

    /**
     * Gets the value of the lestDato property.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getLestDato() {
        return lestDato;
    }

    /**
     * Sets the value of the lestDato property.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setLestDato(XMLGregorianCalendar value) {
        this.lestDato = value;
    }

    /**
     * Gets the value of the kommentar property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getKommentar() {
        return kommentar;
    }

    /**
     * Sets the value of the kommentar property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setKommentar(String value) {
        this.kommentar = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Verdi"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *               &lt;maxLength value="11"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="UsikkerVerdi" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "verdi",
            "usikkerVerdi"
    })
    public static class Fodselsnr {

        @XmlElement(name = "Verdi", required = true)
        protected String verdi;
        @XmlElement(name = "UsikkerVerdi")
        protected Boolean usikkerVerdi;

        /**
         * Gets the value of the verdi property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getVerdi() {
            return verdi;
        }

        /**
         * Sets the value of the verdi property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setVerdi(String value) {
            this.verdi = value;
        }

        /**
         * Gets the value of the usikkerVerdi property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isUsikkerVerdi() {
            return usikkerVerdi;
        }

        /**
         * Sets the value of the usikkerVerdi property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setUsikkerVerdi(Boolean value) {
            this.usikkerVerdi = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Verdi"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long"&gt;
     *               &lt;minInclusive value="0"/&gt;
     *               &lt;totalDigits value="10"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="UsikkerVerdi" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "verdi",
            "usikkerVerdi"
    })
    public static class MeldekortId {

        @XmlElement(name = "Verdi")
        protected long verdi;
        @XmlElement(name = "UsikkerVerdi")
        protected Boolean usikkerVerdi;

        /**
         * Gets the value of the verdi property.
         *
         */
        public long getVerdi() {
            return verdi;
        }

        /**
         * Sets the value of the verdi property.
         *
         */
        public void setVerdi(long value) {
            this.verdi = value;
        }

        /**
         * Gets the value of the usikkerVerdi property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isUsikkerVerdi() {
            return usikkerVerdi;
        }

        /**
         * Sets the value of the usikkerVerdi property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setUsikkerVerdi(Boolean value) {
            this.usikkerVerdi = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Verdi"&gt;
     *           &lt;simpleType&gt;
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long"&gt;
     *               &lt;totalDigits value="9"/&gt;
     *               &lt;minInclusive value="0"/&gt;
     *             &lt;/restriction&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="UsikkerVerdi" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "verdi",
            "usikkerVerdi"
    })
    public static class PersonId {

        @XmlElement(name = "Verdi")
        protected long verdi;
        @XmlElement(name = "UsikkerVerdi")
        protected Boolean usikkerVerdi;

        /**
         * Gets the value of the verdi property.
         *
         */
        public long getVerdi() {
            return verdi;
        }

        /**
         * Sets the value of the verdi property.
         *
         */
        public void setVerdi(long value) {
            this.verdi = value;
        }

        /**
         * Gets the value of the usikkerVerdi property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isUsikkerVerdi() {
            return usikkerVerdi;
        }

        /**
         * Sets the value of the usikkerVerdi property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setUsikkerVerdi(Boolean value) {
            this.usikkerVerdi = value;
        }
    }
}
