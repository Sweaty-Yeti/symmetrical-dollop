package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An XOR with 2 inputs and 1 output
 */
public class XOR implements FitnessFunction {

	OverGen og;

	public XOR() {
		og = new OverGen(2, 1, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FitnessFunction#getOG()
	 */
	@Override
	public OverGen getOG() {
		return og;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FitnessFunction#calculateFitness(Genome)
	 */
	@Override
	public void calculateFitness(Genome gnm) {

		List<Float> inList = new ArrayList<Float>();
		inList.add(0f);
		inList.add(0f);
		float d1 = gnm.calculate(inList).get(0);
		inList.set(0, 0f);
		inList.set(1, 1f);
		float d2 = 1f - gnm.calculate(inList).get(0);
		inList.set(0, 1f);
		inList.set(1, 0f);
		float d3 = 1f - gnm.calculate(inList).get(0);
		inList.set(0, 1f);
		inList.set(1, 1f);
		float d4 = gnm.calculate(inList).get(0);
		float d = d1 + d2 + d3 + d4;
		gnm.fitness = (4f - d) * (4f - d);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see FitnessFunction#run(int, int)
	 */
	public Genome run(int popSize, int maxLoops) {
		og.createBasePop(popSize);
		List<Genome> top = new ArrayList<Genome>(og.topFit);
		GenomeFitnessComparatorDesc gfc = new GenomeFitnessComparatorDesc();
		Collections.sort(top, gfc);
		int genBT = 0;
		while (top.get(0).fitness < 15.9f) {
			og.populateGen();
			top.addAll(og.topFit);
			Collections.sort(top, gfc);
			if (top.get(0).fitness > 9f && genBT == 0) {
				genBT = og.gen;
			}
			if (og.gen >= maxLoops) {
				return null;
			}
		}
		System.out.println("Winning Fitness: " + top.get(0).fitness);
		System.out.print("BreakThrough Generation: " + genBT);
		System.out.print("\tFinal Generation: " + og.gen);
		return top.get(0);
	}

}
