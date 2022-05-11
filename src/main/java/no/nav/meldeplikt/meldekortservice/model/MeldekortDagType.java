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
@XmlType(name = "MeldekortDagType", propOrder = {
        "dag",
        "arbeidetTimerSum",
        "arbeidetTimerListe",
        "syk",
        "annetFravaer",
        "kurs",
        "meldegruppe"
})
public class MeldekortDagType {

    @XmlElement(name = "Dag")
    protected int dag;
    @XmlElement(name = "ArbeidetTimerSum")
    protected MeldekortDagType.ArbeidetTimerSum arbeidetTimerSum;
    @XmlElement(name = "ArbeidetTimerListe")
    protected MeldekortDagType.ArbeidetTimerListe arbeidetTimerListe;
    @XmlElement(name = "Syk", required = true)
    protected VerdiBooleanType syk;
    @XmlElement(name = "AnnetFravaer", required = true)
    protected VerdiBooleanType annetFravaer;
    @XmlElement(name = "Kurs", required = true)
    protected VerdiBooleanType kurs;
    @XmlElement(name = "Meldegruppe")
    protected String meldegruppe;

    /**
     * Gets the value of the dag property.
     *
     */
    public int getDag() {
        return dag;
    }

    /**
     * Sets the value of the dag property.
     *
     */
    public void setDag(int value) {
        this.dag = value;
    }

    /**
     * Gets the value of the arbeidetTimerSum property.
     *
     * @return
     *     possible object is
     *     {@link MeldekortDagType.ArbeidetTimerSum }
     *
     */
    public MeldekortDagType.ArbeidetTimerSum getArbeidetTimerSum() {
        return arbeidetTimerSum;
    }

    /**
     * Sets the value of the arbeidetTimerSum property.
     *
     * @param value
     *     allowed object is
     *     {@link MeldekortDagType.ArbeidetTimerSum }
     *
     */
    public void setArbeidetTimerSum(MeldekortDagType.ArbeidetTimerSum value) {
        this.arbeidetTimerSum = value;
    }

    /**
     * Gets the value of the arbeidetTimerListe property.
     *
     * @return
     *     possible object is
     *     {@link MeldekortDagType.ArbeidetTimerListe }
     *
     */
    public MeldekortDagType.ArbeidetTimerListe getArbeidetTimerListe() {
        return arbeidetTimerListe;
    }

    /**
     * Sets the value of the arbeidetTimerListe property.
     *
     * @param value
     *     allowed object is
     *     {@link MeldekortDagType.ArbeidetTimerListe }
     *
     */
    public void setArbeidetTimerListe(MeldekortDagType.ArbeidetTimerListe value) {
        this.arbeidetTimerListe = value;
    }

    /**
     * Gets the value of the syk property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getSyk() {
        return syk;
    }

    /**
     * Sets the value of the syk property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setSyk(VerdiBooleanType value) {
        this.syk = value;
    }

    /**
     * Gets the value of the annetFravaer property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getAnnetFravaer() {
        return annetFravaer;
    }

    /**
     * Sets the value of the annetFravaer property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setAnnetFravaer(VerdiBooleanType value) {
        this.annetFravaer = value;
    }

    /**
     * Gets the value of the kurs property.
     *
     * @return
     *     possible object is
     *     {@link VerdiBooleanType }
     *
     */
    public VerdiBooleanType getKurs() {
        return kurs;
    }

    /**
     * Sets the value of the kurs property.
     *
     * @param value
     *     allowed object is
     *     {@link VerdiBooleanType }
     *
     */
    public void setKurs(VerdiBooleanType value) {
        this.kurs = value;
    }

    /**
     * Gets the value of the meldegruppe property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMeldegruppe() {
        return meldegruppe;
    }

    /**
     * Sets the value of the meldegruppe property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMeldegruppe(String value) {
        this.meldegruppe = value;
    }


    /**
     *
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "arbeidetTimer"
    })
    public static class ArbeidetTimerListe {

        @XmlElement(name = "ArbeidetTimer")
        protected List<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer> arbeidetTimer;

        /**
         * Gets the value of the arbeidetTimer property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the arbeidetTimer property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getArbeidetTimer().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer }
         *
         *
         */
        public List<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer> getArbeidetTimer() {
            if (arbeidetTimer == null) {
                arbeidetTimer = new ArrayList<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer>();
            }
            return this.arbeidetTimer;
        }


        /**
         *
         *
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "antallTimer",
                "usikkerVerdi"
        })
        public static class ArbeidetTimer {

            @XmlElement(name = "AntallTimer")
            protected float antallTimer;
            @XmlElement(name = "UsikkerVerdi")
            protected Boolean usikkerVerdi;

            /**
             * Gets the value of the antallTimer property.
             *
             */
            public float getAntallTimer() {
                return antallTimer;
            }

            /**
             * Sets the value of the antallTimer property.
             *
             */
            public void setAntallTimer(float value) {
                this.antallTimer = value;
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


    /**
     *
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "verdi",
            "usikkerVerdi"
    })
    public static class ArbeidetTimerSum {

        @XmlElement(name = "Verdi")
        protected float verdi;
        @XmlElement(name = "UsikkerVerdi")
        protected Boolean usikkerVerdi;

        /**
         * Gets the value of the verdi property.
         *
         */
        public float getVerdi() {
            return verdi;
        }

        /**
         * Sets the value of the verdi property.
         *
         */
        public void setVerdi(float value) {
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
