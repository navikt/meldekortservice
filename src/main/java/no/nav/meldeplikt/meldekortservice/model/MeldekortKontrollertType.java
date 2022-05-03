package no.nav.meldeplikt.meldekortservice.model;

        import java.util.ArrayList;
        import java.util.List;
        import javax.xml.bind.annotation.XmlAccessType;
        import javax.xml.bind.annotation.XmlAccessorType;
        import javax.xml.bind.annotation.XmlElement;
        import javax.xml.bind.annotation.XmlSeeAlso;
        import javax.xml.bind.annotation.XmlType;
/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeldekortKontrollertType", propOrder = {
        "meldekortId",
        "status",
        "arsakskoder",
        "meldekortDager"
})
//@XmlSeeAlso({
//        MeldekortKontrollert.class
//})
public class MeldekortKontrollertType {
    @XmlElement(name = "MeldekortId")
    protected long meldekortId;
    @XmlElement(name = "Status", required = true)
    protected String status;
    @XmlElement(name = "Arsakskoder", required = true, nillable = true)
    protected MeldekortKontrollertType.Arsakskoder arsakskoder;
    @XmlElement(name = "MeldekortDager", required = true)
    protected MeldekortKontrollertType.MeldekortDager meldekortDager;
    /**
     * Gets the value of the meldekortId property.
     *
     */
    public long getMeldekortId() {
        return meldekortId;
    }
    /**
     * Sets the value of the meldekortId property.
     *
     */
    public void setMeldekortId(long value) {
        this.meldekortId = value;
    }
    /**
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStatus() {
        return status;
    }
    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStatus(String value) {
        this.status = value;
    }
    /**
     * Gets the value of the arsakskoder property.
     *
     * @return
     *     possible object is
     *     {@link MeldekortKontrollertType.Arsakskoder }
     *
     */
    public MeldekortKontrollertType.Arsakskoder getArsakskoder() {
        return arsakskoder;
    }
    /**
     * Sets the value of the arsakskoder property.
     *
     * @param value
     *     allowed object is
     *     {@link MeldekortKontrollertType.Arsakskoder }
     *
     */
    public void setArsakskoder(MeldekortKontrollertType.Arsakskoder value) {
        this.arsakskoder = value;
    }
    /**
     * Gets the value of the meldekortDager property.
     *
     * @return
     *     possible object is
     *     {@link MeldekortKontrollertType.MeldekortDager }
     *
     */
    public MeldekortKontrollertType.MeldekortDager getMeldekortDager() {
        return meldekortDager;
    }
    /**
     * Sets the value of the meldekortDager property.
     *
     * @param value
     *     allowed object is
     *     {@link MeldekortKontrollertType.MeldekortDager }
     *
     */
    public void setMeldekortDager(MeldekortKontrollertType.MeldekortDager value) {
        this.meldekortDager = value;
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
     *         &lt;element name="Arsakskode" type="{http://www.aetat.no/arena/mk_meldekort_kontrollert.xsd}ArsakskodeType" maxOccurs="unbounded" minOccurs="0"/&gt;
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
            "arsakskode"
    })
    public static class Arsakskoder {
        @XmlElement(name = "Arsakskode")
        protected List<ArsakskodeType> arsakskode;
        /**
         * Gets the value of the arsakskode property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the arsakskode property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getArsakskode().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ArsakskodeType }
         *
         *
         */
        public List<ArsakskodeType> getArsakskode() {
            if (arsakskode == null) {
                arsakskode = new ArrayList<ArsakskodeType>();
            }
            return this.arsakskode;
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
     *         &lt;element name="MeldekortDag" type="{http://www.aetat.no/arena/mk_meldekort_kontrollert.xsd}MeldekortDagType" maxOccurs="14" minOccurs="14"/&gt;
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
         * {@link MeldekortDagType }
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

