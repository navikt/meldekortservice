package no.nav.meldeplikt.meldekortservice.model;

public class MeldekortDagType {

    protected int dag;
    protected MeldekortDagType.ArbeidetTimerSum arbeidetTimerSum;
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

    public static class ArbeidetTimerSum {

        protected float verdi;

        public float getVerdi() {
            return verdi;
        }

        public void setVerdi(float value) {
            this.verdi = value;
        }
    }
}
