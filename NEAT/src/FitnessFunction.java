
public interface FitnessFunction {
	public OverGen getOG();
	public void calculateFitness(Genome gnm);
	public Genome run(int popSize, int maxLoops);
	
}
