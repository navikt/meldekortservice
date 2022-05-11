package no.nav.meldeplikt.meldekortservice.model;

import java.util.ArrayList;
import java.util.List;

public class KontrollresultatType {

    protected KontrollresultatType.Arsakskoder arsakskoder;
    protected String status;
    protected String fritekst;
    protected boolean veiledning;
//    protected String returbrev;

//    public KontrollresultatType.Arsakskoder getArsakskoder() {
//        return arsakskoder;
//    }
//
//    public void setArsakskoder(KontrollresultatType.Arsakskoder value) {
//        this.arsakskoder = value;
//    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getFritekst() {
        return fritekst;
    }

    public void setFritekst(String value) {
        this.fritekst = value;
    }

    public boolean isVeiledning() {
        return veiledning;
    }

    public void setVeiledning(boolean value) {
        this.veiledning = value;
    }

//    public String getReturbrev() {
//        return returbrev;
//    }
//
//    public void setReturbrev(String value) {
//        this.returbrev = value;
//    }

    public static class Arsakskoder {

        protected List<ArsakskodeType> arsakskode;

        public List<ArsakskodeType> getArsakskode() {
            if (arsakskode == null) {
                arsakskode = new ArrayList<>();
            }
            return this.arsakskode;
        }
    }
}
