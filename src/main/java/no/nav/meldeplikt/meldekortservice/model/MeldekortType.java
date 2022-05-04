package no.nav.meldeplikt.meldekortservice.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeldekortType", propOrder = {
        "hode",
        "spm",
        "image",
        "kontrollresultat"
})
public class MeldekortType {

    @XmlElement(name = "Hode", required = true)
    protected HodeType hode;
    @XmlElement(name = "Spm", required = true)
    protected SpmType spm;
    @XmlElement(name = "Image")
    protected byte[] image;
    @XmlElement(name = "Kontrollresultat")
    protected KontrollresultatType kontrollresultat;
    @XmlAttribute(name = "Id")
    protected String id;

    /**
     * Gets the value of the hode property.
     *
     * @return
     *     possible object is
     *     {@link HodeType }
     *
     */
    public HodeType getHode() {
        return hode;
    }

    /**
     * Sets the value of the hode property.
     *
     * @param value
     *     allowed object is
     *     {@link HodeType }
     *
     */
    public void setHode(HodeType value) {
        this.hode = value;
    }

    /**
     * Gets the value of the spm property.
     *
     * @return
     *     possible object is
     *     {@link SpmType }
     *
     */
    public SpmType getSpm() {
        return spm;
    }

    /**
     * Sets the value of the spm property.
     *
     * @param value
     *     allowed object is
     *     {@link SpmType }
     *
     */
    public void setSpm(SpmType value) {
        this.spm = value;
    }

    /**
     * Gets the value of the image property.
     *
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the value of the image property.
     *
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setImage(byte[] value) {
        this.image = value;
    }

    /**
     * Gets the value of the kontrollresultat property.
     *
     * @return
     *     possible object is
     *     {@link KontrollresultatType }
     *
     */
    public KontrollresultatType getKontrollresultat() {
        return kontrollresultat;
    }

    /**
     * Sets the value of the kontrollresultat property.
     *
     * @param value
     *     allowed object is
     *     {@link KontrollresultatType }
     *
     */
    public void setKontrollresultat(KontrollresultatType value) {
        this.kontrollresultat = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setId(String value) {
        this.id = value;
    }

}

