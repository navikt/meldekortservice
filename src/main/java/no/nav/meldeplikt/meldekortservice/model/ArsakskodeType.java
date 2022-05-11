package no.nav.meldeplikt.meldekortservice.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
/**
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArsakskodeType", propOrder = {
        "kode",
        "tekst"
})
public class ArsakskodeType {
    @XmlElement(name = "Kode", required = true)
    protected String kode;
    @XmlElement(name = "Tekst", required = true)
    protected String tekst;
    /**
     * Gets the value of the kode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getKode() {
        return kode;
    }
    /**
     * Sets the value of the kode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setKode(String value) {
        this.kode = value;
    }
    /**
     * Gets the value of the tekst property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTekst() {
        return tekst;
    }
    /**
     * Sets the value of the tekst property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTekst(String value) {
        this.tekst = value;
    }
}
