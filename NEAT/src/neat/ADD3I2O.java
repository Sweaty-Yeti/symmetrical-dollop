package neat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ADD3I2O implements FitnessFunction {

	OverGen og;

	public ADD3I2O() {
		og = new OverGen(3, 2, this);
	}

	@Override
	public OverGen getOG() {
		return og;
	}

	@Override
	public void calculateFitness(Genome gnm) {
		List<Float> inList = new ArrayList<Float>();
		inList.add(1f);
		inList.add(1f);
		inList.add(1f);
		float d10 = 1f - gnm.calculate(inList).get(0);
		float d11 = 1f - gnm.calculate(inList).get(1);
		inList.set(0, 0f);
		inList.set(1, 0f);
		inList.set(2, 0f);
		float d20 = gnm.calculate(inList).get(0);
		float d21 = gnm.calculate(inList).get(1);
		inList.set(0, 0f);
		inList.set(1, 0f);
		inList.set(2, 1f);
		float d30 = 1f - gnm.calculate(inList).get(0);
		float d31 = gnm.calculate(inList).get(1);
		inList.set(0, 0f);
		inList.set(1, 1f);
		inList.set(2, 0f);
		float d40 = 1f - gnm.calculate(inList).get(0);
		float d41 = gnm.calculate(inList).get(1);
		inList.set(0, 0f);
		inList.set(1, 1f);
		inList.set(2, 1f);
		float d50 = gnm.calculate(inList).get(0);
		float d51 = 1f - gnm.calculate(inList).get(1);
		inList.set(0, 1f);
		inList.set(1, 0f);
		inList.set(2, 0f);
		float d60 = 1f - gnm.calculate(inList).get(0);
		float d61 = gnm.calculate(inList).get(1);
		inList.set(0, 1f);
		inList.set(1, 0f);
		inList.set(2, 1f);
		float d70 = gnm.calculate(inList).get(0);
		float d71 = 1f - gnm.calculate(inList).get(1);
		inList.set(0, 1f);
		inList.set(1, 1f);
		inList.set(2, 0f);
		float d80 = gnm.calculate(inList).get(0);
		float d81 = 1f - gnm.calculate(inList).get(1);
		float d = d10 + d11 + d20 + d21 + d30 + d31 + d40 + d41 + d50 + d51 + d60 + d61 + d70 + d71 + d80 + d81;
		gnm.fitness = (16f - d) * (16f - d);

	}

	@Override
	public Genome run(int popSize, int maxLoops) {
		og.createBasePop(popSize);
		List<Genome> top = new ArrayList<Genome>(og.topFit);
		GenomeFitnessComparatorDesc gfc = new GenomeFitnessComparatorDesc();
		Collections.sort(top, gfc);
		int genBT = 0;
		while (top.get(0).fitness < 255f) {
			og.populateGen();
			top.addAll(og.topFit);
			Collections.sort(top, gfc);
			if(top.get(0).fitness > 225f && genBT == 0){
				genBT = og.gen;
			}
			if (og.gen >= maxLoops){
				return null;
			}
		}
		System.out.println("Winning Fitness: " + top.get(0).fitness);
		System.out.print("BreakThrough Generation: " + genBT);
		System.out.print("\tFinal Generation: " + og.gen);
		return top.get(0);
	}

}
