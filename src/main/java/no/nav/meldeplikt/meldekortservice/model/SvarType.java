package no.nav.meldeplikt.meldekortservice.model;

public class SvarType {

    protected VerdiBooleanType svarJa;
    protected VerdiBooleanType svarNei;

    public VerdiBooleanType getSvarJa() {
        return svarJa;
    }

    public void setSvarJa(VerdiBooleanType svarJa) {
        this.svarJa = svarJa;
    }

    public VerdiBooleanType getSvarNei() {
        return svarNei;
    }

    public void setSvarNei(VerdiBooleanType svarNei) {
        this.svarNei = svarNei;
    }
}
