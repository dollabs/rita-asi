package utils;

public class PairLISP<A,B> {
  A cardata;
  B cdrdata;

  public PairLISP(A car, B cdr) {
    cardata = car;
    cdrdata = cdr;
  }

  public A car() {
    return cardata;
  }

  public B cdr() {
    return cdrdata;
  }

  public String toString() {
    String carstr,cdrstr;
    if(cardata != null) carstr = cardata.toString(); else carstr = "#f";
    if(cdrdata != null) cdrstr = cdrdata.toString(); else cdrstr = "#f";
    return "("+carstr+" . "+cdrstr+")";
  }

  public boolean equals(Object p) {
    return ((p instanceof PairLISP) && cardata.equals(((PairLISP)p).cardata)
            && cdrdata.equals(((PairLISP)p).cdrdata));
  }

  public int hashCode() {
    return cardata.hashCode() + 2*cdrdata.hashCode();
  }

  public Object clone() {
    return new PairLISP<A,B>(cardata,cdrdata);
  }
}
