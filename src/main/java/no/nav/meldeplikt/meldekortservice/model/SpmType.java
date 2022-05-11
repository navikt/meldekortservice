package no.nav.meldeplikt.meldekortservice.model;

import java.util.ArrayList;
import java.util.List;

public class SpmType {

    protected SvarType arbeidssoker;
    protected SvarType arbeidet;
    protected SvarType syk;
    protected SvarType annetFravaer;
    protected SvarType kurs;
    protected VerdiBooleanType forskudd;
    protected SpmType.MeldekortDager meldekortDager;
    protected VerdiBooleanType signatur;

    public SvarType getArbeidssoker() {
        return arbeidssoker;
    }

    public void setArbeidssoker(SvarType arbeidssoker) {
        this.arbeidssoker = arbeidssoker;
    }

    public SvarType getArbeidet() {
        return arbeidet;
    }

    public void setArbeidet(SvarType arbeidet) {
        this.arbeidet = arbeidet;
    }

    public SvarType getSyk() {
        return syk;
    }

    public void setSyk(SvarType syk) {
        this.syk = syk;
    }

    public SvarType getAnnetFravaer() {
        return annetFravaer;
    }

    public void setAnnetFravaer(SvarType annetFravaer) {
        this.annetFravaer = annetFravaer;
    }

    public SvarType getKurs() {
        return kurs;
    }

    public void setKurs(SvarType kurs) {
        this.kurs = kurs;
    }

    public VerdiBooleanType getForskudd() {
        return forskudd;
    }

    public void setForskudd(VerdiBooleanType forskudd) {
        this.forskudd = forskudd;
    }

    public SpmType.MeldekortDager getMeldekortDager() {
        return meldekortDager;
    }

    public void setMeldekortDager(SpmType.MeldekortDager value) {
        this.meldekortDager = value;
    }
    public VerdiBooleanType getSignatur() {
        return signatur;
    }

    public void setSignatur(VerdiBooleanType signatur) {
        this.signatur = signatur;
    }

    public static class MeldekortDager {

        protected List<MeldekortDagType> meldekortDag;

        public List<MeldekortDagType> getMeldekortDag() {
            if (meldekortDag == null) {
                meldekortDag = new ArrayList<>();
            }
            return this.meldekortDag;
        }
    }
}
