package no.nav.meldeplikt.meldekortservice.model;

import java.util.ArrayList;
import java.util.List;

public class MeldekortKontrollertType {

    protected long meldekortId;
    protected String status;
    protected MeldekortKontrollertType.Arsakskoder arsakskoder;
    protected MeldekortKontrollertType.MeldekortDager meldekortDager;

    public long getMeldekortId() {
        return meldekortId;
    }

    public void setMeldekortId(long value) {
        this.meldekortId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public MeldekortKontrollertType.Arsakskoder getArsakskoder() {
        return arsakskoder;
    }

    public void setArsakskoder(MeldekortKontrollertType.Arsakskoder value) {
        this.arsakskoder = value;
    }

    public MeldekortKontrollertType.MeldekortDager getMeldekortDager() {
        return meldekortDager;
    }

    public void setMeldekortDager(MeldekortKontrollertType.MeldekortDager value) {
        this.meldekortDager = value;
    }

    public static class Arsakskoder {
        protected List<ArsakskodeType> arsakskode;

        public List<ArsakskodeType> getArsakskode() {
            if (arsakskode == null) {
                arsakskode = new ArrayList<>();
            }
            return this.arsakskode;
        }
    }

    public static class MeldekortDager {
        protected List<MeldekortDagKontrollertType> meldekortDag;

        public List<MeldekortDagKontrollertType> getMeldekortDag() {
            if (meldekortDag == null) {
                meldekortDag = new ArrayList<>();
            }
            return this.meldekortDag;
        }
    }
}