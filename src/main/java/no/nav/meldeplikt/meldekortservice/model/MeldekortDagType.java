package no.nav.meldeplikt.meldekortservice.model;

public class MeldekortDagType {

    protected int dag;
    protected MeldekortDagType.ArbeidetTimerSum arbeidetTimerSum;
//    protected MeldekortDagType.ArbeidetTimerListe arbeidetTimerListe;
    protected VerdiBooleanType syk;
    protected VerdiBooleanType annetFravaer;
    protected VerdiBooleanType kurs;
    protected String meldegruppe;

    public int getDag() {
        return dag;
    }

    public void setDag(int value) {
        this.dag = value;
    }

    public MeldekortDagType.ArbeidetTimerSum getArbeidetTimerSum() {
        return arbeidetTimerSum;
    }

    public void setArbeidetTimerSum(MeldekortDagType.ArbeidetTimerSum value) {
        this.arbeidetTimerSum = value;
    }

//    public MeldekortDagType.ArbeidetTimerListe getArbeidetTimerListe() {
//        return arbeidetTimerListe;
//    }
//
//    public void setArbeidetTimerListe(MeldekortDagType.ArbeidetTimerListe value) {
//        this.arbeidetTimerListe = value;
//    }

    public VerdiBooleanType getSyk() {
        return syk;
    }

    public void setSyk(VerdiBooleanType value) {
        this.syk = value;
    }

    public VerdiBooleanType getAnnetFravaer() {
        return annetFravaer;
    }

    public void setAnnetFravaer(VerdiBooleanType value) {
        this.annetFravaer = value;
    }

    public VerdiBooleanType getKurs() {
        return kurs;
    }

    public void setKurs(VerdiBooleanType value) {
        this.kurs = value;
    }

    public String getMeldegruppe() {
        return meldegruppe;
    }

    public void setMeldegruppe(String value) {
        this.meldegruppe = value;
    }

//    public static class ArbeidetTimerListe {
//
//        protected List<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer> arbeidetTimer;
//
//        /**
//         * Gets the value of the arbeidetTimer property.
//         *
//         * <p>
//         * This accessor method returns a reference to the live list,
//         * not a snapshot. Therefore any modification you make to the
//         * returned list will be present inside the JAXB object.
//         * This is why there is not a <CODE>set</CODE> method for the arbeidetTimer property.
//         *
//         * <p>
//         * For example, to add a new item, do as follows:
//         * <pre>
//         *    getArbeidetTimer().add(newItem);
//         * </pre>
//         *
//         *
//         * <p>
//         * Objects of the following type(s) are allowed in the list
//         * {@link MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer }
//         *
//         *
//         */
//        public List<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer> getArbeidetTimer() {
//            if (arbeidetTimer == null) {
//                arbeidetTimer = new ArrayList<MeldekortDagType.ArbeidetTimerListe.ArbeidetTimer>();
//            }
//            return this.arbeidetTimer;
//        }
//
//        public static class ArbeidetTimer {
//
//            protected float antallTimer;
////            protected Boolean usikkerVerdi;
//
//            /**
//             * Gets the value of the antallTimer property.
//             *
//             */
//            public float getAntallTimer() {
//                return antallTimer;
//            }
//
//            /**
//             * Sets the value of the antallTimer property.
//             *
//             */
//            public void setAntallTimer(float value) {
//                this.antallTimer = value;
//            }
//
//            /**
////             * Gets the value of the usikkerVerdi property.
////             *
////             * @return
////             *     possible object is
////             *     {@link Boolean }
////             *
////             */
////            public Boolean isUsikkerVerdi() {
////                return usikkerVerdi;
////            }
////
////            /**
////             * Sets the value of the usikkerVerdi property.
////             *
////             * @param value
////             *     allowed object is
////             *     {@link Boolean }
////             *
////             */
////            public void setUsikkerVerdi(Boolean value) {
////                this.usikkerVerdi = value;
////            }
//
//        }
//
//    }

    public static class ArbeidetTimerSum {

        protected float verdi;
//        protected Boolean usikkerVerdi;

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
//
//        /**
//         * Gets the value of the usikkerVerdi property.
//         *
//         * @return
//         *     possible object is
//         *     {@link Boolean }
//         *
//         */
//        public Boolean isUsikkerVerdi() {
//            return usikkerVerdi;
//        }
//
//        /**
//         * Sets the value of the usikkerVerdi property.
//         *
//         * @param value
//         *     allowed object is
//         *     {@link Boolean }
//         *
//         */
//        public void setUsikkerVerdi(Boolean value) {
//            this.usikkerVerdi = value;
//        }
    }
}
