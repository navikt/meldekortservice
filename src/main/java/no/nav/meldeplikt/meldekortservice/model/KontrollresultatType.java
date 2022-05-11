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
@XmlType(name = "KontrollresultatType", propOrder = {
        "arsakskoder",
        "status",
        "fritekst",
        "veiledning",
        "returbrev"
})
public class KontrollresultatType {

    @XmlElement(name = "Arsakskoder", required = true, nillable = true)
    protected KontrollresultatType.Arsakskoder arsakskoder;
    @XmlElement(name = "Status", required = true)
    protected String status;
    @XmlElement(name = "Fritekst")
    protected String fritekst;
    @XmlElement(name = "Veiledning")
    protected boolean veiledning;
    @XmlElement(name = "Returbrev", required = true)
    protected String returbrev;

    /**
     * Gets the value of the arsakskoder property.
     *
     * @return
     *     possible object is
     *     {@link KontrollresultatType.Arsakskoder }
     *
     */
    public KontrollresultatType.Arsakskoder getArsakskoder() {
        return arsakskoder;
    }

    /**
     * Sets the value of the arsakskoder property.
     *
     * @param value
     *     allowed object is
     *     {@link KontrollresultatType.Arsakskoder }
     *
     */
    public void setArsakskoder(KontrollresultatType.Arsakskoder value) {
        this.arsakskoder = value;
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
     * Gets the value of the fritekst property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFritekst() {
        return fritekst;
    }

    /**
     * Sets the value of the fritekst property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFritekst(String value) {
        this.fritekst = value;
    }

    /**
     * Gets the value of the veiledning property.
     *
     */
    public boolean isVeiledning() {
        return veiledning;
    }

    /**
     * Sets the value of the veiledning property.
     *
     */
    public void setVeiledning(boolean value) {
        this.veiledning = value;
    }

    /**
     * Gets the value of the returbrev property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReturbrev() {
        return returbrev;
    }

    /**
     * Sets the value of the returbrev property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReturbrev(String value) {
        this.returbrev = value;
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
     *         &lt;element name="Arsakskode" type="{http://www.aetat.no/arena/mk_meldekort.xsd}ArsakskodeType" maxOccurs="unbounded" minOccurs="0"/&gt;
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
}
