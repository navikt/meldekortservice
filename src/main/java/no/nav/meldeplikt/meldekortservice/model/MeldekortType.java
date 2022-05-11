package no.nav.meldeplikt.meldekortservice.model;

public class MeldekortType {

    protected HodeType hode;
    protected SpmType spm;
    protected String id;

    public HodeType getHode() {
        return hode;
    }

    public void setHode(HodeType hode) {
        this.hode = hode;
    }

    public SpmType getSpm() {
        return spm;
    }

    public void setSpm(SpmType spm) {
        this.spm = spm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
