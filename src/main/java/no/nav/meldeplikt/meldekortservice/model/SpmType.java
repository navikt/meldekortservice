package no.nav.meldeplikt.meldekortservice.model;


import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpmType", propOrder = {
        "arbeidssoker",
        "arbeidet",
        "syk",
        "annetFravaer",
        "kurs",
        "forskudd",
        "meldekortDager",
        "signatur"
})
public class SpmType {

    @XmlElement(name = "Arbeidssoker", required = true)
    protected SvarType arbeidssoker;
    @XmlElement(name = "Arbeidet", required = true)
    protected SvarType arbeidet;
    @XmlElement(name = "Syk", required = true)
    protected SvarType syk;
    @XmlElement(name = "AnnetFravaer", required = true)
    protected SvarType annetFravaer;
    @XmlElement(name = "Kurs", required = true)
    protected SvarType kurs;
    @XmlElement(name = "Forskudd", required = true)
    protected VerdiBooleanType forskudd;
    @XmlElement(name = "MeldekortDager", required = true)
    protected SpmType.MeldekortDager meldekortDager;
    @XmlElement(name = "Signatur", required = true)
    protected VerdiBooleanType signatur;

    /**
     * Gets the value of the arbeidssoker property.
     *
     * @return
     *     possible object is
     *     {@link SvarType }
     *
     */
    public SvarType getArbeidssoker() {
        return arbeidssoker;
    }

    /**
     * Sets the value of the arbeidssoker property.
     *
     * @param value
     *     allowed object is
     *     {@link SvarType }
     *
     */
    public void setArbeidssoker(SvarType value) {
        this.arbeidssoker = value;
    }

    /**
     * Gets the value of the arbeidet property.
     *
     * @return
     *     possible object is
     *     {@link SvarType }
     *
     */
    public SvarType getArbeidet() {
        return arbeidet;
    }

    /**
     * Sets the value of the arbeidet property.
     *
     * @param value
     *     allowed object is
     *     {@link SvarType }
     *
     */
    public void setArbeidet(SvarType value) {
        this.arbeidet = value;
    }

    /**
     * Gets the value of the syk property.
     *
     * @return
     *     possible object is
     *     {@link SvarType }
     *
     */
    public SvarType getSyk() {
        return syk;
    }

    /**
     * Sets the value of the syk property.
     *
     * @param value
     *     allowed object is
     *     {@link SvarType }
     *
     */
    public void setSyk(SvarType value) {
        this.syk = value;
    }

    /**
     * Gets the value of the annetFravaer property.
     *
     * @return
     *     possible object is
     *     {@link SvarType }
     *
     */
    public SvarType getAnnetFravaer() {
        return annetFravaer;
    }

    /**
     * Sets the value of the annetFravaer property.
     *
     * @param value
     *     allowed object is
     *     {@link SvarType }
     *
     */
    public void setAnnetFravaer(SvarType value) {
        this.annetFravaer = value;
    }

    /**
     * Gets the value of the kurs property.
     *
     * @return
     *     possible object is
     *     {@link SvarType }
     *
     */
    public SvarType getKurs() {
        return kurs;
    }

    /**
     * Sets the value of the kurs property.
     *
     * @param value
     *     allowed object is
     *     {@link SvarType }
     *
     */
    public void setKurs(SvarType value) {
        this.kurs = value;
    }

    /**
     * Gets the value of the forskudd property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getForskudd() {
        return forskudd;
    }

    /**
     * Sets the value of the forskudd property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setForskudd(VerdiBooleanType value) {
        this.forskudd = value;
    }

    /**
     * Gets the value of the meldekortDager property.
     *
     * @return
     *     possible object is
     *     {@link SpmType.MeldekortDager }
     *
     */
    public SpmType.MeldekortDager getMeldekortDager() {
        return meldekortDager;
    }

    /**
     * Sets the value of the meldekortDager property.
     *
     * @param value
     *     allowed object is
     *     {@link SpmType.MeldekortDager }
     *
     */
    public void setMeldekortDager(SpmType.MeldekortDager value) {
        this.meldekortDager = value;
    }

    /**
     * Gets the value of the signatur property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getSignatur() {
        return signatur;
    }

    /**
     * Sets the value of the signatur property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setSignatur(VerdiBooleanType value) {
        this.signatur = value;
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
     *         &lt;element name="MeldekortDag" type="{http://www.aetat.no/arena/mk_meldekort.xsd}MeldekortDagType" maxOccurs="14" minOccurs="14"/&gt;
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
            "meldekortDag"
    })
    public static class MeldekortDager {

        @XmlElement(name = "MeldekortDag", required = true)
        protected List<MeldekortDagType> meldekortDag;

        /**
         * Gets the value of the meldekortDag property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the meldekortDag property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMeldekortDag().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MeldekortDagKontrollertType }
         *
         *
         */
        public List<MeldekortDagType> getMeldekortDag() {
            if (meldekortDag == null) {
                meldekortDag = new ArrayList<MeldekortDagType>();
            }
            return this.meldekortDag;
        }
    }
}
