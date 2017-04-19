
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

class Genome implements Comparable<Genome> {

	static OverGen og;

	HashMultimap<Node, Gene> genIn = HashMultimap.create();
	HashMultimap<Node, Gene> genome = HashMultimap.create();

	float fitness;
	float sharedFitness;
	int speciesSize;

	int id;
	private static int count = 0;

	public Genome(OverGen o) {
		og = o;
		id = count++;
	}

	public Genome(Genome g) {
		id = count++;
		for (Gene gen : g.genome.values()) {
			gen.copyToGenome(g, this);
			genome.put(gen.out, gen);
			genIn.put(gen.in, gen);
		}
	}

	public Genome(Genome g1, Genome g2) {
		id = count++;
		Genome max = (g1.genome.values().size() > g2.genome.values().size() ? g1 : g2);
		Genome min = (g1.genome.values().size() > g2.genome.values().size() ? g2 : g1);
		for (Gene gen : max.genome.values()) {
			gen.copyToGenome(max, this);
			genome.put(gen.out, gen);
			genIn.put(gen.in, gen);
		}
		for (Gene gen : min.genome.values()) {
			if (!genome.containsValue(gen)) {
				if (loopCheck(gen.in, gen.out)) {
					gen.copyToGenome(min, this);
					genome.put(gen.out, gen);
					genIn.put(gen.in, gen);
				}
			} else {
				Random rand = og.rand;
				if (rand.nextBoolean()) {
					gen.copyToGenome(min, this);
				}
			}
		}
	}

	public void mutate() {
		Random rand = og.rand;
		int r = rand.nextInt(1000);
		if (r < 5) {
			addNode();
			// System.out.println("Adding Node");
		} else if (r < 400) {
			// System.out.println("Adding Connection");
			if (!addConnection()) {
				changeWeight();
			}
		} else if (r < 950) {
			changeWeight();

		} else {
			changeAllWeights();
			// System.out.println("Changing all weights");
		}
	}

	public void addNode() {
		// Inserts a Node in the middle of a Gene

		// Node cannot be inserted into a disabled gene or a bias gene
		List<Gene> genList = new ArrayList<Gene>();
		for (Gene g : genome.values()) {
			if (!g.in.bias && g.getEnabled(this)) {
				// Create a list of viable genes to be disabled
				genList.add(g);
			}
		}

		// Choose a gene for the list at random
		Random rand = og.rand;
		int r = rand.nextInt(genList.size());
		Gene gen = genList.get(r);

		// If this is the first time the gene is being disabled, create new node
		// Otehrwise use the disabler node
		if (gen.disabler == null) {
			Node n = new Node();
			gen.disable(this, n);

			// Each hidden node is connected to 3 nodes (bias, input : output)
			this.addGene(og.bias, n);
			this.addGene(gen.in, n);
			this.addGene(n, gen.out);
		} else {
			Node n = gen.disabler;
			gen.disable(this);

			// Each hidden node is connected to 3 nodes (bias, input : output)
			this.addGene(og.bias, n);
			this.addGene(gen.in, n);
			this.addGene(n, gen.out);
		}

	}

	public void changeAllWeights() {
		for (Gene g : genome.values()) {
			g.changeWeight(this);
		}
	}

	public void changeWeight() {
		List<Gene> genList = new ArrayList<Gene>(genome.values());
		Random rand = og.rand;
		int r = rand.nextInt(genList.size());
		while (!genList.get(r).getEnabled(this)) {
			r = rand.nextInt(genList.size());
		}
		genList.get(r).changeWeight(this);
	}

	public void addGene(Node in, Node out) {
		if (og.geneMap.values().size() > 0) {
			if (!genome.values().isEmpty()) {
				for (Gene g : genome.get(out)) {
					if (g.in == in) {
						if (g.getEnabled(this)) {
							return;
						} else {
							g.enable(this);
							return;
						}
					}
				}
			}
			for (Gene g : og.geneMap.get(out)) {
				if (g.in == in) {
					g.addGenome(this);
					genome.put(out, g);
					genIn.put(in, g);
					return;
				}
			}
		}
		Gene gen = new Gene(in, out, og);
		gen.addGenome(this);
		genome.put(out, gen);
		genIn.put(in, gen);
	}

	public boolean addConnection() {
		List<Node> inList = new ArrayList<Node>();
		List<Node> outList = new ArrayList<Node>();
		Set<Node> inSet = new TreeSet<Node>(genIn.keySet());
		Set<Node> outSet = new TreeSet<Node>(genome.keySet());

		for (Node in : inSet) {
			for (Node out : outSet) {
				Set<Gene> s = new HashSet<Gene>(genIn.get(in));
				s.retainAll(genome.get(out));
				if (s.isEmpty() && loopCheck(in, out)) {
					inList.add(in);
					outList.add(out);
				} else if (s.size() > 1) {
					throw new IllegalStateException();
				} else {
					for (Gene g : s) {
						if (!g.getEnabled(this) && loopCheck(in, out)) {
							inList.add(in);
							outList.add(out);
						}
					}
				}
			}
		}

		if (inList.isEmpty()) {
			// System.out.println("No possible connections");
			return false;
		} else {
			Random rand = og.rand;
			int r = rand.nextInt(inList.size());
			addGene(inList.get(r), outList.get(r));
			return true;
		}

	}

	public boolean loopCheck(Node in, Node out) {
		// This function checks whether the proposed connection loops
		// In other words, checks if the output would affect the input
		if (in == out) {
			return false;
		}
		if (genome.get(in).contains(out)) {
			return false;
		}
		if (in.bias || in.input) {
			return true;
		}
		boolean b = true;
		for (Gene g : genome.get(in)) {
			b = (b ? loopCheck(g.in, out) : false);
		}
		return b;
	}

	public void printGenome() {
		Set<Node> out = new TreeSet<Node>(genome.keySet());
		Set<Node> in = new TreeSet<Node>(genIn.keySet());
		Set<Node> all = new TreeSet<Node>(in);
		System.out.println();
		all.addAll(out);
		for (Node o : out) {
			System.out.print("\t" + o.id);
		}
		System.out.println("\n");
		GeneOutComparator goc = new GeneOutComparator();
		for (Node i : in) {
			List<Gene> genList = new ArrayList<Gene>(genIn.get(i));
			Collections.sort(genList, goc);
			System.out.print(i.id + "\t");
			List<Node> nodList = new ArrayList<Node>();
			for (Gene g : genList) {
				if (g.getEnabled(this)) {
					nodList.add(g.out);
				}
			}
			for (Node o : out) {
				System.out.print((nodList.contains(o) ? 1 : 0) + "\t");
			}
			System.out.println();
		}
	}

	public void printFloat() {
		Set<Node> out = new TreeSet<Node>(genome.keySet());
		Set<Node> in = new TreeSet<Node>(genIn.keySet());
		Set<Node> all = new TreeSet<Node>(in);
		System.out.println();
		all.addAll(out);
		for (Node o : out) {
			System.out.print("\t" + o.id);
		}
		System.out.println("\n");
		new GeneOutComparator();
		for (Node i : in) {
			new ArrayList<Gene>(genIn.get(i));
			System.out.print(i.id + "\t");
			for (Node o : out) {
				Set<Gene> s = new HashSet<Gene>(genIn.get(i));
				s.retainAll(genome.get(o));
				if (!s.isEmpty()) {
					for (Gene g : s) {
						if (g.getEnabled(this)) {
							System.out.printf("%+.2f\t", g.weightMap.get(this));
						} else {
							System.out.print("+O.OO\t");
						}
					}
				} else {
					System.out.printf("%+.2f\t", 0f);
				}

			}
			System.out.println();
		}
	}

	public Gene getGene(Node in, Node out) {
		// Returns the gene connecting the two nodes if it exists
		// Otehrwise returns null
		Set<Gene> s = new HashSet<Gene>(genIn.get(in));
		s.retainAll(genome.get(out));
		for (Gene g : s) {
			return g;
		}
		return null;
	}

	public List<Float> calculate(List<Float> inList) {
		if (inList.size() != og.inList.size()) {
			throw new IllegalArgumentException();
		}
		// Set inputs
		for (int i = 0; i < og.inList.size(); ++i) {
			Node n = og.inList.get(i);
			n.value = inList.get(i);
		}

		for (Node n : genome.keySet()) {
			n.value = 0f;
			n.calculated = false;
		}
		List<Float> retList = new ArrayList<Float>();
		for (Node n : og.outList) {
			retList.add(n.calculateNode(this, genome.keySet().size() * 2));
		}
		return retList;
	}

	public void calculateFitness() {
		og.fitFunc.calculateFitness(this);
		/*
		 * ArrayList<Float> inList = new ArrayList<Float>(); inList.add(1f);
		 * inList.add(1f); float d1 = calculate(inList).get(0); inList.set(0,
		 * 0f); inList.set(1, 0f); float d2 = calculate(inList).get(0);
		 * inList.set(0, 1f); inList.set(1, 0f); float d3 = 1f -
		 * calculate(inList).get(0); inList.set(0, 0f); inList.set(1, 1f); float
		 * d4 = 1f - calculate(inList).get(0); float d = d1 + d2 + d3 + d4;
		 * fitness = (4f - d) * (4f - d);
		 */


	}

	public void calculateSharedFitness() {

		while (og.sfMap.size() <= og.gen) {
			Multimap<Integer, Float> hmm = HashMultimap.create();
			og.sfMap.add(hmm);
		}
		Multimap<Integer, Genome> spm = og.speciesMap.get(og.gen);
		for (int i : spm.keySet()) {
			if (spm.get(i).contains(this)) {
				int spSize = og.speciesMap.get(og.gen).get(i).size();

				sharedFitness = fitness / spSize;
				og.sfMap.get(og.gen).put(i, sharedFitness);
			}
		}
	}

	@Override
	public int compareTo(Genome arg0) {
		// Compare Genomes by shared fitness descending;
		// Then id ascending;
		Float af = arg0.sharedFitness;
		Float tf = this.sharedFitness;
		Integer aid = arg0.id;
		Integer iid = this.id;
		return (af.compareTo(tf) == 0 ? (iid.compareTo(aid)) : af.compareTo(tf));
	}
}
