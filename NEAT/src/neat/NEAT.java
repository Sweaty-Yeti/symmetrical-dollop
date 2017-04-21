package neat;

/**
 * The Class NEAT. This is the Main class
 */
public class NEAT {
	
	/**
	 * The main method. 
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		//Two input XOR
		FitnessFunction xor = new XOR();
		//Three input XOR
		//FitnessFunction xor = new XOR3I();
		//sum of 3 [1 bit]-inputs
		//FitnessFunction xor = new ADD3I2O();
		
		
		Genome gnm = xor.run(100, 500);
		if(gnm != null){
			gnm.printFloat();
		}
	}
}
