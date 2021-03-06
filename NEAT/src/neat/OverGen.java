package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This class contains populations of genomes and manipulates/evolves them
 * toward a higher fitness
 */
public class OverGen {

	/** MultiMap of all Genes indexed by output node */
	Multimap<Node, Gene> geneMap = HashMultimap.create();

	/** Map of all species atchetypes indexed by species id */
	Map<Integer, Genome> archetypeMap = new HashMap<Integer, Genome>();

	/** Species fitness map */
	List<Multimap<Integer, Float>> sfMap = new ArrayList<Multimap<Integer, Float>>();

	/** Species map. */
	List<Multimap<Integer, Genome>> speciesMap = new ArrayList<Multimap<Integer, Genome>>();

	/** List of input nodes */
	List<Node> inList = new ArrayList<Node>();

	/** List of output nodes */
	List<Node> outList = new ArrayList<Node>();

	/** The nod list. */
	// List<Node> nodList = new ArrayList<Node>();

	/** The genome list. */
	List<Genome> genomeList = new ArrayList<Genome>();

	/** List of all Genomes by Generation */
	List<List<Genome>> generation = new ArrayList<List<Genome>>();

	/** The top fit. */
	Set<Genome> topFit = new TreeSet<Genome>();

	/** The shared top fit. */
	Set<Genome> sharedTopFit = new TreeSet<Genome>();

	FitnessFunction fitFunc;

	Long lastTime = System.currentTimeMillis();

	/** Random Variable */
	Random rand;

	/** The seed. */
	long seed;

	/** The current generation */
	int gen = 0;

	int popSize = 0;

	int topSize = 0;

	/** The base genome */
	Genome base;

	/** The bias node */
	Node bias;

	/**
	 * Instantiates a new over gen.
	 *
	 * @param f
	 *            the fitness function
	 */
	public OverGen(FitnessFunction f) {
		seed = System.currentTimeMillis();
		rand = new Random(seed);
		fitFunc = f;
		bias = new Node(this);
		base = new Genome(this);
		for (int i = 0; i < f.numInputs(); ++i) {
			Node nod = new Node(0);
			inList.add(nod);
		}
		for (int i = 0; i < f.numOutputs(); ++i) {
			Node nod = new Node(1);
			outList.add(nod);

		}
		for (int i = 0; i < outList.size(); ++i) {
			Node nod = outList.get(i);
			for (int j = -1; j < inList.size(); ++j) {
				base.addGene((j < 0 ? bias : inList.get(j)), nod);
			}
		}

	}

	/**
	 * Sets the seed for the Random.
	 *
	 * @param seed
	 *            the new seed
	 */
	public void setSeed(long seed) {
		rand.setSeed(seed);
		this.seed = seed;
	}

	/**
	 * Gets the seed.
	 *
	 * @return the seed
	 */
	public long getSeed() {
		return seed;
	}

	/**
	 * Creates the base population.
	 *
	 * @param pSize
	 *            the population size for all generations
	 */
	public void createBasePop(int pSize) {
		popSize = pSize;
		topSize = popSize / 10;

		List<Genome> tmpPop = new ArrayList<Genome>();
		for (int i = 0; i < popSize; ++i) {
			Genome g = new Genome(base);
			if (i > 0) {
				for (int j = 0; j < 10; j++) {
					g.mutate();
				}
			}
			genomeList.add(g);
			tmpPop.add(g);
		}
		generation.add(tmpPop);
		speciate();
	}

	/**
	 * Calculates the fitness for each member of the population. Then finds the
	 * most fit Genomes and puts them in the topFit and sharedTopFit Sets
	 */
	public void popFitness() {
		for (Genome g : generation.get(gen)) {

			g.calculateFitness();
			g.calculateSharedFitness();
		}

		Set<Genome> sfs = new TreeSet<Genome>(sharedTopFit);
		sfs.addAll(generation.get(gen));
		Set<Genome> tfs = new TreeSet<Genome>(new GenomeFitnessComparatorDesc());
		tfs.addAll(topFit);
		tfs.addAll(sfs);
		sharedTopFit = new TreeSet<Genome>();
		for (Genome g : sfs) {
			if (sharedTopFit.size() < topSize) {
				sharedTopFit.add(g);
			} else {
				break;
			}
		}
		topFit = new TreeSet<Genome>(new GenomeFitnessComparatorDesc());
		for (Genome g : tfs) {
			if (topFit.size() < topSize) {
				topFit.add(g);
			} else {
				break;
			}
		}
		System.out.println("\nTop Fitness and Shared Fitness of Generation " + gen);
		int i = 0;
		for (Genome g : topFit) {
			System.out.print("#" + (i + 1) + " Fitness:\t" + g.fitness + "  (" + g.sharedFitness + ")");
			if (++i >= 1) {
				break;
			}
		}
		for (Genome g : sharedTopFit) {
			System.out.println("   \t #" + (i) + " Shared Fitness:\t" + g.sharedFitness + "  (" + g.fitness + ")");
			// g.printGenome();
			if (++i >= 2) {
				break;
			}
		}
		System.out.println("_______________________________\n");
	}

	/**
	 * Checks if two genomes are of the same species.
	 *
	 * @param g1
	 *            the g 1
	 * @param g2
	 *            the g 2
	 * @return true, if the genomes share species
	 */
	public boolean compatable(Genome g1, Genome g2) {
		float distThresh = 0.4f;
		float c1 = 0.3f; // Weights the excess (e)
		float c2 = 0.3f; // Weights the disjoint (d)
		float c3 = 0.3f; // Weights the weight difference (w)
		Set<Gene> s1 = new TreeSet<Gene>(g1.genome.values());
		Set<Gene> s2 = new TreeSet<Gene>(g2.genome.values());
		int e = 0;
		int d = 0;
		float w = 0f;
		int N = (s1.size() > s2.size() ? s1.size() : s2.size());
		Gene max1 = Collections.max(g1.genome.values());
		Gene max2 = Collections.max(g2.genome.values());

		if (max1 != max2) {
			boolean b = max1.getId() > max2.getId();
			int excess = (b ? max2 : max1).getId();
			for (Gene g : (b ? s1 : s2)) {
				if (g.id > excess) {
					e += 1;
				}
			}
		}

		Set<Gene> s = new TreeSet<Gene>(s1);
		s.retainAll(s2);

		d = s1.size() + s2.size() - 2 * s.size() - e;

		for (Gene g : s) {
			w += Math.abs(g.getWeight(g1) - g.getWeight(g2));
		}
		w /= s.size();

		float distance = (c1 * e + c2 * d) / N + c3 * w;

		return distance < distThresh;
	}

	/**
	 * Populate the next generation
	 */
	public void populateGeneration() {

		Map<Integer, Float> sumMap = new TreeMap<Integer, Float>();
		float totalSum = 0f;
		for (int i : sfMap.get(gen).keySet()) {
			float sum = 0f;
			for (float f : sfMap.get(gen).get(i)) {
				sum += f;
			}
			sumMap.put(i, sum);
			totalSum += sum;
		}

		List<List<Genome>> spcsList = new ArrayList<List<Genome>>();
		List<Integer> topList = new ArrayList<Integer>();
		for (int i : speciesMap.get(gen).keySet()) {
			List<Genome> spcs = new ArrayList<Genome>(speciesMap.get(gen).get(i));
			Collections.sort(spcs);
			int top = Math.round((popSize - topSize) * (sumMap.get(i) / totalSum));
			spcsList.add(spcs.subList(0, (spcs.size() == 1 ? 1 : spcs.size() / 2)));
			topList.add(top);
		}

		Set<Genome> tmpPop = new TreeSet<Genome>();
		for (List<Genome> spcs : spcsList) {
			tmpPop.add(spcs.get(0));
		}
		List<Genome> tmp = new ArrayList<Genome>();
		for (Genome g : tmpPop) {
			Genome gnm = new Genome(g);
			gnm.mutate();
			tmp.add(gnm);
		}
		tmpPop.addAll(tmp);
		for (int i = 0; i < topList.size(); ++i) {
			for (int j = 0; j < topList.get(i); ++j) {
				if (spcsList.get(i).size() == 0 || tmpPop.size() >= popSize) {
					break;
				} else if (spcsList.get(i).size() == 1) {
					if (j * 2 < topList.get(i)) {
						Genome g = mate(spcsList.get(i).get(0), spcsList.get(i).get(0));
						g.mutate();
						tmpPop.add(g);
					} else {
						int spc = rand.nextInt(spcsList.size());
						while (spcsList.get(spc).size() == 0) {
							spc = rand.nextInt(spcsList.size());

						}
						int spInd = rand.nextInt(spcsList.get(spc).size());
						Genome g = mate(spcsList.get(i).get(0), spcsList.get(spc).get(spInd));
						tmpPop.add(g);
					}
				} else {
					int ind = rand.nextInt(spcsList.get(i).size());
					int spc = rand.nextInt(spcsList.size());
					if (j * 2 < topList.get(i)) {
						spc = i;
					}
					while (spcsList.get(spc).size() == 0) {
						spc = rand.nextInt(spcsList.size());

					}
					int spInd = rand.nextInt(spcsList.get(spc).size());

					Genome g = mate(spcsList.get(i).get(ind), spcsList.get(spc).get(spInd));
					tmpPop.add(g);
				}
			}
		}
		tmp = new ArrayList<Genome>(tmpPop);
		while (tmpPop.size() < popSize) {
			int r1 = rand.nextInt(tmp.size());
			int r2 = rand.nextInt(tmp.size());

			Genome g = mate(tmp.get(r1), tmp.get(r2));
			g.mutate();
			tmpPop.add(g);
		}
		gen++;
		generation.add(new ArrayList<Genome>(tmpPop));
		speciate();

	}

	/**
	 * Define the different species.
	 */
	public void speciate() {
		Multimap<Integer, Genome> thisGen = HashMultimap.create();
		if (gen == 0) {
			archetypeMap.put(0, generation.get(gen).get(0));
			thisGen.put(0, generation.get(gen).get(0));
		}
		for (Genome g : generation.get(gen)) {
			if (!thisGen.containsValue(g)) {
				for (int i : archetypeMap.keySet()) {
					if (compatable(g, archetypeMap.get(i))) {
						thisGen.put(i, g);
						break;
					}
				}
			}
			if (!thisGen.containsValue(g)) {
				int spNum = Collections.max(archetypeMap.keySet()) + 1;
				archetypeMap.put(spNum, g);
				thisGen.put(spNum, g);
			}
		}

		speciesMap.add(thisGen);
		popFitness();

		/*
		 * for (int i : thisGen.keySet()) { List<Genome> gL = new
		 * ArrayList<Genome>(thisGen.get(i)); Collections.sort(gL);
		 * System.out.println("Species: " + i + "\tSize: " +
		 * thisGen.get(i).size() + " \tTop Shared Fitness: " +
		 * gL.get(0).sharedFitness);
		 * 
		 * }
		 */
		System.out.println("\nNodeNum: " + Node.count + "\tSpecies: " + thisGen.keySet().size() + "\tGenes/Node: "
				+ (float) Gene.count / Node.count + "\t Time Since Last: "
				+ (System.currentTimeMillis() - lastTime) / 1000f);
		lastTime = System.currentTimeMillis();
	}

	/**
	 * Returns a Genome that is the 'Child' of the two input Genomes
	 *
	 * @param g1
	 *            parent 1
	 * @param g2
	 *            parent 2
	 * @return the child
	 */
	private Genome mate(Genome g1, Genome g2) {
		if (g1 == g2) {
			return new Genome(g1);
		}
		return new Genome(g1, g2);
	}

	/**
	 * Adds the gene g to this Genome.
	 *
	 * @param g
	 *            the g
	 */
	public void addGene(Gene g) {
		geneMap.put(g.out, g);
	}
}

class GenomeFitnessComparatorDesc implements Comparator<Genome> {

	@Override
	public int compare(Genome o1, Genome o2) {
		// Compare Lexographically by fitness(desc) then id(asc)
		Float f1 = o1.fitness;
		Float f2 = o2.fitness;
		Integer id1 = o1.id;
		Integer id2 = o2.id;
		return (f2.compareTo(f1) == 0 ? (id1.compareTo(id2)) : f2.compareTo(f1));
	}
}
