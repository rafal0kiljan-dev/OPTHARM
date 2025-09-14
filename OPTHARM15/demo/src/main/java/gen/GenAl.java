package gen;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.*;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import gen.GenAl.Individual;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;

public class GenAl {

    @PlanningSolution
    public class Individual {
        public List<Satelite> genotyp;
        public int phenotyp;

        public Individual(List<Satelite> genotyp, int phenotyp) {
            this.genotyp = genotyp;
            this.phenotyp = phenotyp;
        }
    }

    public class Mut implements SelectionSorterWeightFactory<Individual, Individual> {

        public void mutationes(List<Satelite> genotyp, List<Satelite> sat) {
            for (int h = 0; h < 180; h++) {
                Random rand = new Random();
                int modification = rand.nextInt(100);
                if (modification < 42) {
                    mutRem(genotyp);
                }
                if (modification < 95) {
                    mutAdd(genotyp, sat);
                }

            }
        }

        public void mutAdd(List<Satelite> genotyp, List<Satelite> sat) {
            Random r_gen = new Random();
            int rand_gen = r_gen.nextInt(sat.size());
            Satelite randGen = sat.get(rand_gen);
            List<Satelite> newGen = new ArrayList<>();
            Satelite mutGen = createGen(randGen, sat);
            newGen.add(mutGen);
            genotyp.addAll(newGen);
        }

        public void mutRem(List<Satelite> genotyp) {
            Random r_gen = new Random();
            int rand_gen = r_gen.nextInt(genotyp.size());
            Satelite randGen = genotyp.get(rand_gen);
            genotyp.remove(randGen);
        }

        public Comparable<Integer> createSorterWeight(Individual individual, Individual individual2) {
            int tmp1 = getPhenotypes(individual.genotyp);
            int tmp2 = getPhenotypes(individual.genotyp);
            int weight;
            if (tmp1 > tmp2) {
                weight = tmp1;
            } else {
                weight = tmp2;
            }
            return weight;
        }
    }

    void fillingGenotyp(Individual individual, List<Satelite> satelites) {
        int sizeGenotyp = individual.genotyp.size();
        List<Satelite> addArr = new ArrayList<>();
        List<Satelite> remArr = new ArrayList<>();
        for (int i = 0; i < satelites.size(); i++) {
            Satelite sat = satelites.get(i);
            addArr.add(sat);
            for (int j = 0; j < individual.genotyp.size(); j++) {
                if (individual.genotyp.get(j).id.equals(satelites.get(i).id)) {
                    remArr.add(sat);
                    break;
                }
            }
        }
        addArr.removeAll(remArr);
        List<Satelite> newGen = new ArrayList<>();
        for (Satelite ar : addArr) {
            if (individual.genotyp.size() == sizeGenotyp + 1) {
                break;
            }
            Satelite mutGen = createGen(ar, individual.genotyp);
            newGen.add(mutGen);
        }
        individual.genotyp.addAll(newGen);
        checkInd(individual);
    }
    void checkInd(Individual ind) {
        List<Satelite> arRem = new ArrayList<>();
        for (int i = 0; i < ind.genotyp.size(); i++) {
            Satelite gen = ind.genotyp.get(i);
            for (int j = 0; j < gen.numSeries; j++) {
                for (int k = i + 1; k < ind.genotyp.size(); k++) {
                    Satelite rem = ind.genotyp.get(k);
                    for (int l = 0; l < ind.genotyp.get(k).series.length; l++) {
                        if (l > j && l < gen.numSeries) {
                            if ((rem.series[l] + rem.singleSeries + 180 >= rem.series[j] - rem.minSeperation
                                    && rem.series[l] + rem.singleSeries + 180 <= rem.series[j])
                                    || ((rem.series[l] - rem.minSeperation - 180 <= rem.series[j] + rem.singleSeries
                                            || rem.series[l] - rem.minSeperation - 180 <= rem.series[j])
                                            && rem.series[l] + rem.singleSeries + 180 >= rem.series[j]
                                                    + rem.singleSeries)) {
                                arRem.add(rem);
                            }
                        }
                        int s = rem.series[l] + 180;
                        double e = rem.series[l] + rem.singleSeries - 180;
                        if (l > j && l < gen.numSeries
                                && (s <= gen.series[j] && e >= gen.series[j] + gen.singleSeries)
                                || (s >= gen.series[j] && e <= gen.series[j] + gen.singleSeries)
                                || (e >= gen.series[j] && e <= gen.series[j] + gen.singleSeries)
                                || (s >= gen.series[j] && s <= gen.series[j] + gen.singleSeries)) {
                            arRem.add(rem);
                        } else {
                            s = rem.series[l];
                            e = rem.series[l] + rem.singleSeries;
                            if (l > j && l < gen.numSeries
                                    && (s <= gen.series[j] && e >= gen.series[j] + gen.singleSeries)
                                    || (s >= gen.series[j] && e <= gen.series[j] + gen.singleSeries)
                                    || (e >= gen.series[j] && e <= gen.series[j] + gen.singleSeries)
                                    || (s >= gen.series[j] && s <= gen.series[j] + gen.singleSeries)) {
                                arRem.add(rem);
                            }
                        }
                    }
                }
            }
        }

        ind.genotyp.removeAll(arRem);
        ind.phenotyp = getPhenotypes(ind.genotyp);
    }
    static Satelite createGen(Satelite gen, List<Satelite> genotyp) {
        int k = 0;
        int critic = 0;
        while ((gen.series[0] == 0 || gen.series[gen.series.length - 1] == 0) && critic < 100)
            for (int i = 0; i < gen.numSeries; i++) {
                for (Satelite prevGen : genotyp) {
                    for (int j = 0; j < prevGen.numSeries; j++) {
                        Random rand2 = new Random();
                        int winRand = rand2.nextInt(gen.winNum + 1);
                        int valRand = (int) Math.floor(
                                Math.random() * (gen.winEnd[winRand] - gen.winStart[winRand] + 1)
                                        + gen.winStart[winRand]);
                        if (k < gen.numSeries
                                && (valRand + gen.singleSeries < prevGen.series[j]
                                        || valRand > prevGen.series[j] + prevGen.singleSeries)
                                && (i == 0 || (i > 0
                                        && valRand + gen.singleSeries < gen.series[i - 1] - gen.singleSeries
                                                - gen.minSeperation
                                        || valRand > gen.series[i - 1] + gen.singleSeries
                                                + gen.minSeperation))) {
                            gen.series[k] = valRand;
                            k++;
                        } else {
                            critic++;
                        }
                    }
                }
            }
        return (new Satelite(gen.id, gen.singleSeries, gen.numSeries,
                gen.priority, gen.minSeperation,
                gen.winStart, gen.winEnd, gen.winNum, gen.order,
                gen.series, gen.betweenDay));
    }
    public List<Satelite> createInd(List<Satelite> genotyp) {
        List<Satelite> newGen = new ArrayList<>();
        for (Satelite gen : genotyp) {
            newGen.add(createGen(gen, genotyp));
        }
        genotyp.clear();
        genotyp.addAll(newGen);
        return genotyp;
    }
    int getPhenotypes(List<Satelite> genotyp) {
        int phenotyp = 0;
        for (Satelite gen : genotyp) {
            phenotyp += gen.priority * gen.singleSeries * gen.numSeries;
        }
        return phenotyp;
    }
    class SortChromosom implements Comparator<Individual> {
        public int compare(Individual a, Individual b) {
            return b.phenotyp - a.phenotyp;
        }
    }
    public List<Individual> individuals;
    public GenAl() {
        this.individuals = individuals;
    }
    public Individual getBestInd(List<Individual> individuals, List<Satelite> sat, boolean showT) {
        Individual best = individuals.get(0);
        for (int i = 0; i < individuals.size(); i++) {
            Individual next = individuals.get(i);
            checkInd(next);
            SimpleScore scoreB = SimpleScore.of(best.phenotyp);
            SimpleScore scoreN = SimpleScore.of(next.phenotyp);
            if (scoreB.compareTo(scoreN) < 0) {
                best = individuals.get(i);
            }
        }
        checkInd(best);
        if (showT == true) {
            System.out.println("BEST - phenotyp " + best.phenotyp + ", satelites - " +
                    best.genotyp.size() + "\n");
        }
        return best;
    }
    void createFirstPopulation(List<Satelite> genotyp, List<Individual> individuals) {
        List<Individual> newInds = new ArrayList<>();
        for (int i = 0; i < individuals.size(); i++) {
            newInds.add(new Individual(createInd(genotyp), getPhenotypes(genotyp)));
            if (i > 1 && newInds.get(i).genotyp.size() < 19) {
                newInds.remove(i);
                i--;
            }
        }
        individuals.clear();
        individuals.addAll(newInds);
    }
    void selection(List<Individual> individuals, int alive) {
        for (int i = 0; i < individuals.size(); i++) {
            if (i > alive) {
                individuals.remove(i);
                i--;
            }
        }
        Collections.sort(individuals, new SortChromosom());
    }
    void nextGeneration(List<Individual> individuals, long halftime, List<Satelite> sat) {
        List<Individual> childs = new ArrayList<>();
        int k = 0;
        for (int g = 0; g < 3; g++) {
            Random r = new Random();
            long new_half = r.nextLong(halftime);
            for (int h = 0; h < individuals.size(); h++) {
                for (int i = 0; i < individuals.size() - 1; i++) {
                    Individual parent1 = individuals.get(h);
                    Individual parent2 = individuals.get(i + 1);
                    List<Satelite> childGen = new ArrayList<>();
                    for (Satelite gen1 : parent1.genotyp) {
                        int mmax = 0;
                        for (int j = 0; j < gen1.numSeries; j++) {
                            if (gen1.series[mmax] < gen1.series[j]) {
                                mmax = j;
                            }
                        }
                        if (gen1.series[mmax] + gen1.singleSeries < new_half + 180) {
                            childGen.add(k, gen1);
                            k++;
                        }
                    }
                    for (Satelite gen2 : parent2.genotyp) {
                        int mmin = 0;
                        for (int j = 0; j < gen2.numSeries; j++) {
                            if (gen2.series[mmin] > gen2.series[j]) {
                                mmin = j;
                            }
                        }
                        if (gen2.series[mmin] + gen2.singleSeries > new_half - 180) {
                            childGen.add(k, gen2);
                            k++;
                        }
                    }
                    k = 0;
                    Individual child = new Individual(childGen, getPhenotypes(childGen));
                    Mut mutator = new Mut();
                    Individual mutant = new Individual(childGen, getPhenotypes(childGen));
                    mutator.mutationes(mutant.genotyp, sat);
                    if (mutator.createSorterWeight(mutant, child) != child
                            && getPhenotypes(mutant.genotyp) >= getPhenotypes(child.genotyp)) {
                        childs.add(mutant);
                    } else {
                        childs.add(child);
                    }
                }
            }
        }
        for (int i = 0; i < 1000; i++) {
            List<Satelite> genotypes = new ArrayList<>();
            for (Satelite sattle : sat) {
                Satelite gen = new Satelite(sattle.id, sattle.singleSeries, sattle.numSeries,
                        sattle.priority,
                        sattle.minSeperation,
                        sattle.winStart, sattle.winEnd, sattle.winNum, sattle.order, sattle.series, sattle.betweenDay);
                genotypes.add(gen);
            }
            checkInd(new Individual(createInd(genotypes), getPhenotypes(genotypes)));
            GenAl.Individual firstInd = new Individual(createInd(genotypes), getPhenotypes(genotypes));
            checkInd(firstInd);
            childs.add(firstInd);
        }
        individuals.addAll(childs);
    }
}

