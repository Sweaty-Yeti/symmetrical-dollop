package neat;

/**
 * The Interface FitnessFunction. Implement this interface to define the
 * function for the Genome to complete
 */
public interface FitnessFunction {

	public OverGen getOG();

	/**
	 * Calculates the fitness of the input Genome.
	 *
	 * @param gnm
	 *            the gnm
	 */
	public void calculateFitness(Genome gnm);

	/**
	 * Begin evolving the network
	 *
	 * @param popSize
	 *            the population size
	 * @param maxLoops
	 *            the max number of loops
	 * @return the winning genome
	 */
	public Genome run(int popSize, int maxLoops);

}
