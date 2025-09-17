package gen;

import java.util.List;
import java.util.Collections;
import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalTime;
import java.time.LocalDate;

import org.shredzone.*;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonTimes;
import org.shredzone.commons.suncalc.SunTimes;

public class App {

    static void moonPositions(LocalDate first_day, int snt, List<Satelite> satelites) {

        double lat = 52.3;
        double lng = 16.43;
        MoonTimes mtimes = MoonTimes.compute()
                .on(first_day)
                .at(lat, lng)
                .timezone("Europe/Warsaw")
                .execute();

        System.out.println("Moonrise: " + mtimes.getRise());
        System.out.println("Moonset: " + mtimes.getSet());

        SunTimes stimes = SunTimes.compute()
                .on(first_day)
                .at(lat, lng)
                .timezone("Europe/Warsaw")
                .execute();

        System.out.println("Sunrise: " + stimes.getRise());
        System.out.println("Sunset: " + stimes.getSet());

        MoonIllumination moon = MoonIllumination.compute()
                .on(first_day)
                .timezone("Europe/Warsaw")
                .execute();

        double percent = moon.getFraction() * 100;
        String moonphase = (int) Math.round(percent) + "%";
        System.out.println("Moon Phase: " + moonphase);

        LocalTime mSet = mtimes.getSet().toLocalTime();
        int mset = mSet.getHour() * 3_600_000 + mSet.getMinute() * 60_000 +
                mSet.getSecond() * 1_000 + mSet.getNano() / 1_000_000;

        LocalTime mRise = mtimes.getRise().toLocalTime();
        int mrise = mRise.getHour() * 3_600_000 + mRise.getMinute() * 60_000 +
                mRise.getSecond() * 1_000 + mRise.getNano() / 1_000_000;

        if ((mset > snt || mrise < snt) && percent > 0) {

            try {
                File moonFile = new File("demo\\moon_pmt.csv");
                Scanner inMoon = new Scanner(moonFile);
                String str_moon;
                String[] token_moon;
                while (inMoon.hasNext()) {
                    str_moon = inMoon.nextLine();
                    token_moon = str_moon.split(",");
                    String mid = token_moon[0];
                    LocalTime mcol = LocalTime.parse(token_moon[1]);
                    int mcolT = mcol.getHour() * 3_600_000 + mcol.getMinute() * 60_000 +
                            mcol.getSecond() * 1_000 + mRise.getNano() / 1_000_000;
                    ;

                    if (mcolT < snt) {
                        mcolT += 86400000 - snt;
                    } else {
                        mcolT -= snt;
                    }

                    // System.out.println(mcolT + "\n");
                    for (Satelite sat : satelites) {
                        if (sat.id.equals(mid)) {
                            long[] mstart = new long[sat.winStart.length + 1];
                            long[] mend = new long[sat.winEnd.length + 1];
                            int i = 0;
                            int k = 0;
                            for (; i < sat.winStart.length; i++) {
                                if (k < 5) {
                                    if (sat.winStart[i] < mcolT && sat.winEnd[i] > mcolT) {
                                        mstart[k] = sat.winStart[i];
                                        mend[k] = mcolT;
                                        mstart[k + 1] = mcolT + (int) (30000 * percent);
                                        mend[k + 1] = sat.winEnd[i];
                                        k += 2;
                                    } else {
                                        mstart[k] = sat.winStart[i];
                                        mend[k] = sat.winEnd[i];
                                    }
                                    k++;
                                }
                            }
                            sat.winEnd = mend;
                            sat.winStart = mstart;
                        }
                    }
                }
                inMoon.close();
            } catch (Exception e) {
                System.out.println("Don't find file moon_pmt.csv \n");
            }
        }
    }

    static void timePartition(List<Satelite> satelites, int time_discredit) {
        for (Satelite outSat : satelites) {
            outSat.minSeperation = outSat.minSeperation / time_discredit;
            outSat.singleSeries = outSat.singleSeries / time_discredit;
            for (int i = 0; i < outSat.winStart.length; i++) {
                outSat.winEnd[i] = outSat.winEnd[i] / time_discredit;
                outSat.winStart[i] = outSat.winStart[i] / time_discredit;
            }
        }
    }

    static void satStatistic(List<Satelite> bestsat, List<Satelite> satelites) {
        int p5 = 0, p4 = 0, p3 = 0, p2 = 0, p1 = 0;
        for (Satelite sat : bestsat) {
            switch (sat.priority) {
                case 5:
                    p5++;
                    break;
                case 4:
                    p4++;
                    break;
                case 3:
                    p3++;
                    break;
                case 2:
                    p2++;
                    break;
                case 1:
                    p1++;
                    break;
            }
        }
        int s5 = 0, s4 = 0, s3 = 0, s2 = 0, s1 = 0;
        for (Satelite sat : satelites) {
            switch (sat.priority) {
                case 5:
                    s5++;
                    break;
                case 4:
                    s4++;
                    break;
                case 3:
                    s3++;
                    break;
                case 2:
                    s2++;
                    break;
                case 1:
                    s1++;
                    break;
            }
        }
        System.out.println(
                "Number satelites with priority 5 - " + p5 + "/" + s5 + "\n" + "Number satelites with priority 4 - "
                        + p4 + "/" + s4 + "\n" + "Number satelites with priority 3 - " + p3 + "/" + s3 + "\n"
                        + "Number satelites with priority 2 - "
                        + p2 + "/" + s2 + "\n" + "Number satelites with priority 1 - " + p1 + "/" + s1 + "\n");
    }

    public static void main(String[] args) throws Exception {
        File confFile = new File("demo\\night_conf.csv");
        Scanner inConf = new Scanner(confFile);
        String str_conf;
        String[] token_conf;
        int time_discredit = 1;
        LocalTime start_night = LocalTime.now();
        int discredit = 36000;
        LocalTime end_night = LocalTime.now();
        LocalDate first_day = LocalDate.now();
        LocalDate second_day = LocalDate.now();
        try {
            while (inConf.hasNext()) {
                str_conf = inConf.nextLine();
                token_conf = str_conf.split(",");
                time_discredit = Integer.parseInt(token_conf[0]);
                start_night = LocalTime.parse(token_conf[1]);
                end_night = LocalTime.parse(token_conf[2]);
                first_day = LocalDate.parse(token_conf[3]);
                second_day = LocalDate.parse(token_conf[4]);
            }
        } catch (Exception e) {
            System.out.println("Don't find file night_conf.csv \n");
        }
        inConf.close();
        int snt = start_night.getHour() * 3_600_000 + start_night.getMinute() * 60_000 +
                start_night.getSecond() * 1_000 + start_night.getNano() / 1_000_000;
        String str_sat;
        String[] token_sat;
        String str_win;
        String[] token_win;
        int a = 0;
        ArrayList<Satelite> satelites = new ArrayList<>();

        try {
            File satFile = new File("demo//satelite_pmt.csv");
            Scanner inSat = new Scanner(satFile);
            File winFile = new File("demo//window_pmt.csv");
            Scanner inWin = new Scanner(winFile);
            while (inSat.hasNext() || inWin.hasNext()) {
                str_sat = inSat.nextLine();
                token_sat = str_sat.split(",");
                str_win = inWin.nextLine();
                token_win = str_win.split(",");
                String id = token_sat[0];
                int checkA = Integer.parseInt(token_sat[0]);
                int checkB = Integer.parseInt(token_sat[0]);
                if (checkA == checkB) {
                    double singleSeries = Integer.parseInt(token_sat[1]);
                    int numSeries = Integer.parseInt(token_sat[2]);
                    int priority = Integer.parseInt(token_sat[3]);
                    int minSeperation = Integer.parseInt(token_sat[4]);
                    int order = (10000 - priority * 1000) + (numSeries * (int) Math.round(singleSeries) / 1000);
                    long[] winStart = new long[5];
                    long[] winEnd = new long[5];
                    int[] series = new int[numSeries];
                    LocalTime time_start = LocalTime.parse(token_win[1]);
                    LocalTime time_stop = LocalTime.parse(token_win[2]);
                    winStart[a] = time_start.getHour() * 3_600_000 + time_start.getMinute() * 60_000 +
                            time_start.getSecond() * 1_000 + time_start.getNano() / 1_000_000;
                    winEnd[a] = time_stop.getHour() * 3_600_000 + time_stop.getMinute() * 60_000 +
                            time_stop.getSecond() * 1_000 + time_stop.getNano() / 1_000_000;
                    boolean betweenDay = false;

                    if (winStart[a] < snt && winEnd[a] < snt) {
                        winStart[a] += 86400000 - snt;
                        winEnd[a] += 86400000 - snt;
                        betweenDay = false;
                    } else {
                        if (winStart[a] > snt && winEnd[a] > snt) {
                            winStart[a] -= snt;
                            winEnd[a] -= snt;
                            betweenDay = false;
                        } else {
                            if (winStart[a] > winEnd[a] && winStart[a] > snt
                                    && winEnd[a] < snt) {
                                winStart[a] -= snt;
                                winEnd[a] += 86400000 - snt;
                                betweenDay = true;
                            }
                        }
                    }
                    Satelite satelite = new Satelite(id, singleSeries, numSeries, priority, minSeperation,
                            winStart, winEnd, a, order, series, betweenDay);
                    satelites.add(satelite);
                } else {
                    a++;
                }
            }
            inSat.close();
            inWin.close();
        } catch (Exception e) {
            System.out.println("Don't find file satelite_pmt.csv or window_pmt.csv \n");
        }

        moonPositions(first_day, snt, satelites);

        Collections.sort(satelites, new SortbyOrder());

        timePartition(satelites, time_discredit);

        // *************ALGORYTM GENETYCZNY******************************
        GenAl GA = new GenAl();
        List<GenAl.Individual> individuals = new ArrayList<>();
        List<GenAl.Individual> newInd = new ArrayList<>();
        InGen setGen = new InGen();
        int numPop = setGen.getNumStartPopulity();
        System.out.println(numPop + " \n");
        for (int i = 0; i < numPop; i++) {
            List<Satelite> genotypes = new ArrayList<>();
            for (Satelite sat : satelites) {
                Satelite gen = new Satelite(sat.id, sat.singleSeries, sat.numSeries,
                        sat.priority,
                        sat.minSeperation,
                        sat.winStart, sat.winEnd, sat.winNum, sat.order, sat.series, sat.betweenDay);
                genotypes.add(gen);
            }
            GA.checkInd(GA.new Individual(GA.createInd(genotypes), GA.getPhenotypes(genotypes)));
            GenAl.Individual firstInd = GA.new Individual(GA.createInd(genotypes), GA.getPhenotypes(genotypes));
            GA.checkInd(firstInd);
            newInd.add(firstInd);
        }

        individuals.clear();
        individuals.addAll(newInd);

        int alive = 5;
        long duration = end_night.getHour() * 3_600_000
                + end_night.getMinute() * 60_000
                + end_night.getSecond() * 1_000 + end_night.getNano()
                        / 1_000_000 / time_discredit;
        long startLoop = System.currentTimeMillis();

        long endLoop = 0;
        while (endLoop - startLoop < setGen.getTimeMili()) {
            if (duration > 0) {
                GA.selection(individuals, alive);
                GA.nextGeneration(individuals, duration, satelites);
            }
            endLoop = System.currentTimeMillis();
            if (GA.getBestInd(individuals, satelites, true).genotyp.size() >= setGen.getNumSatalites()) {
                break;
            }
        }

        GenAl.Individual best_ind = GA.getBestInd(individuals, satelites, true);
        Collections.sort(best_ind.genotyp, new SortbyTime());
        FileWriter outResults = new FileWriter("results.csv");
        a = 0;
        satStatistic(best_ind.genotyp, satelites);
        for (Satelite outSat : best_ind.genotyp) {
            for (int i = 0; i < outSat.numSeries; i++) {
                long tmp1 = 0;
                long tmp2 = 0;
                tmp1 = (outSat.series[i]) * time_discredit;
                tmp2 = (outSat.series[i] + (int) Math.round(outSat.singleSeries)) * time_discredit;
                Instant inst1 = Instant.ofEpochMilli(tmp1);
                LocalTime book_time1 = inst1.atOffset(ZoneOffset.UTC).toLocalTime();
                Instant inst2 = Instant.ofEpochMilli(tmp2);
                LocalTime book_time2 = inst2.atOffset(ZoneOffset.UTC).toLocalTime();
                if (tmp1 < 86400000 - snt && tmp2 < 86400000 - snt && outSat.betweenDay == false) {
                    tmp1 = (outSat.series[i] * time_discredit) + snt;
                    tmp2 = ((outSat.series[i] + (int) Math.round(outSat.singleSeries))
                            * time_discredit) + snt;
                    inst1 = Instant.ofEpochMilli(tmp1);
                    book_time1 = inst1.atOffset(ZoneOffset.UTC).toLocalTime();
                    inst2 = Instant.ofEpochMilli(tmp2);
                    book_time2 = inst2.atOffset(ZoneOffset.UTC).toLocalTime();
                    outResults.append(outSat.id + "," + outSat.priority + "," + first_day + "T" + book_time1 + ","
                            + first_day + "T" + book_time2);
                    outResults.append("\n");
                } else {
                    if (tmp1 > 86400000 - snt && tmp2 > 86400000 - snt) {
                        tmp1 = (outSat.series[i] * time_discredit) - (86400000 - snt);
                        tmp2 = ((outSat.series[i] + (int) Math.round(outSat.singleSeries))
                                * time_discredit) - (86400000 - snt);
                        inst1 = Instant.ofEpochMilli(tmp1);
                        book_time1 = inst1.atOffset(ZoneOffset.UTC).toLocalTime();
                        inst2 = Instant.ofEpochMilli(tmp2);
                        book_time2 = inst2.atOffset(ZoneOffset.UTC).toLocalTime();
                        outResults
                                .append(outSat.id + "," + outSat.priority + "," + second_day + "T" + book_time1 + ","
                                        + second_day + "T" + book_time2);
                        outResults.append("\n");
                    } else {
                        if (tmp1 < 86400000 - snt && tmp2 > 86400000 - snt && outSat.betweenDay == true) {
                            tmp1 = (outSat.series[i] * time_discredit) + snt;
                            tmp2 = ((outSat.series[i] + (int) Math.round(outSat.singleSeries))
                                    * time_discredit) - (86400000 - snt);
                            inst1 = Instant.ofEpochMilli(tmp1);
                            book_time1 = inst1.atOffset(ZoneOffset.UTC).toLocalTime();
                            inst2 = Instant.ofEpochMilli(tmp2);
                            book_time2 = inst2.atOffset(ZoneOffset.UTC).toLocalTime();
                            outResults.append(
                                    outSat.id + "," + outSat.priority + "," + first_day + "T" + book_time1 + ","
                                            + second_day + "T" + book_time2 + "\n");
                        }
                    }
                }
            }
        }
        outResults.flush();
        outResults.close();
    }
}

