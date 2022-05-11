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
@XmlType(name = "SvarType", propOrder = {
        "svarJa",
        "svarNei"
})
public class SvarType {

    @XmlElement(name = "SvarJa", required = true)
    protected VerdiBooleanType svarJa;
    @XmlElement(name = "SvarNei", required = true)
    protected VerdiBooleanType svarNei;

    /**
     * Gets the value of the svarJa property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getSvarJa() {
        return svarJa;
    }

    /**
     * Sets the value of the svarJa property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setSvarJa(VerdiBooleanType value) {
        this.svarJa = value;
    }

    /**
     * Gets the value of the svarNei property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getSvarNei() {
        return svarNei;
    }

    /**
     * Sets the value of the svarNei property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setSvarNei(VerdiBooleanType value) {
        this.svarNei = value;
    }
}
