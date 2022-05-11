package no.nav.meldeplikt.meldekortservice.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeldekortDagType", propOrder = {
        "dag",
        "meldegruppe"
})
public class MeldekortDagKontrollertType {

    @XmlElement(name = "Dag")
    protected int dag;
    @XmlElement(name = "Meldegruppe", required = true)
    protected String meldegruppe;

    /**
     * Gets the value of the dag property.
     */
    public int getDag() {
        return dag;
    }

    /**
     * Sets the value of the dag property.
     */
    public void setDag(int value) {
        this.dag = value;
    }

    /**
     * Gets the value of the meldegruppe property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMeldegruppe() {
        return meldegruppe;
    }

    /**
     * Sets the value of the meldegruppe property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMeldegruppe(String value) {
        this.meldegruppe = value;
    }
}
