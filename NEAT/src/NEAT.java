import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NEAT {
	public static void main(String[] args) {
		FitnessFunction xor = new XOR();
		Genome gnm = xor.run(300, 1000);
		if(gnm != null){
			gnm.printFloat();
		}
		/*
		System.out.println("Species Archetypes:");
		for (int i : og.archetypeMap.keySet()) {
			System.out.print("Species Archetype " + i + ":");
			og.archetypeMap.get(i).printGenome();
			System.out.println();
		}*/

	}
}
