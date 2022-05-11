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
@XmlType(name = "VerdiBooleanType", propOrder = {
        "verdi",
        "usikkerVerdi"
})
public class VerdiBooleanType {

    @XmlElement(name = "Verdi")
    protected boolean verdi;
    @XmlElement(name = "UsikkerVerdi")
    protected Boolean usikkerVerdi;

    /**
     * Gets the value of the verdi property.
     *
     */
    public boolean isVerdi() {
        return verdi;
    }

    /**
     * Sets the value of the verdi property.
     *
     */
    public void setVerdi(boolean value) {
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
