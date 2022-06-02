package no.nav.meldeplikt.meldekortservice.model;

public class MeldekortDagKontrollertType {

    protected int dag;
    protected String meldegruppe;

    public int getDag() {
        return dag;
    }

    public void setDag(int dag) {
        this.dag = dag;
    }

    public String getMeldegruppe() {
        return meldegruppe;
    }

    public void setMeldegruppe(String meldegruppe) {
        this.meldegruppe = meldegruppe;
    }
}
