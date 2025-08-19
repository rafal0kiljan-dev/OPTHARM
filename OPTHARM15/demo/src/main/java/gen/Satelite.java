package gen;

import java.util.Comparator;

public class Satelite {
    String id; // kod identyfikujący
    double singleSeries;// długość obserwacji
    int numSeries; // ilość obserwacji
    int priority; // priorytet
    int minSeperation; // czas minimalnej separacji
    long[] winStart; // początek okna obserwacji
    long[] winEnd; // koniec okna obserwacji
    int winNum; // ilość okien obserwacyjnych
    int order;
    int[] series; // seria planowanych obserwacji
    boolean betweenDay;

    public Satelite(String ID, double s_series, int n_series,
            int p, int m_sep, long win_start[], long win_stop[],
            int n, int o, int[] s, boolean b_d) {
        id = ID;
        singleSeries = s_series;
        numSeries = n_series;
        priority = p;
        minSeperation = m_sep;
        winStart = win_start;
        winEnd = win_stop;
        winNum = n;
        order = o;
        series = s;
        betweenDay = b_d;
    }
}

class SortbyOrder implements Comparator<Satelite> {
    public int compare(Satelite a, Satelite b) {
        return a.order - b.order;
    }
}

class SortbyTime implements Comparator<Satelite> {
    public int compare(Satelite b, Satelite a) {
        return b.series[0] - a.series[0];
    }
}
