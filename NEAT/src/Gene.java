
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

class Gene implements Comparable<Gene>{
	
	static int count = 0;
	final int id;
	
	final Node in, out;
	final OverGen og;
	
	HashMap<Genome, Float> weightMap = new HashMap<Genome,Float>();
	
	HashMap<Genome, Boolean> enableMap = new HashMap<Genome, Boolean>();
	Node disabler = null;

	public Gene(Node inp, Node outp, OverGen g) {
		og = g;
		id = count++;
		in = inp;
		out = outp;
		og.addGene(this);
		//System.out.println(in.id + " -> " + out.id);
	}
	
	public int getId(){
		return id;
	}
	
	public float getWeight(Genome g){
		return weightMap.get(g);
	}
	
	public void changeWeight(Genome g){
		Random rand = og.rand;
		float weight = weightMap.get(g).floatValue();
		weight = weight + rand.nextFloat()*2-1f;
		weightMap.replace(g, weight);
		
	}
	
	public Node getDisabler(){
		return disabler;
	}
	
	public void copyToGenome(Genome from, Genome to){
		weightMap.put(to, weightMap.get(from));
		enableMap.put(to, enableMap.get(from));
	}
	
	public void addGenome(Genome g){
		
		Random rand = og.rand;
		float weight = rand.nextFloat()*2-1;
		weightMap.put(g, weight);
		this.enable(g);
		
	}
	
	public boolean getEnabled(Genome g){
		return enableMap.get(g);
	}

	public void disable(Genome g, Node n) {
		enableMap.put(g, false);
		disabler = n;
	}

	public void disable(Genome g) {
		enableMap.put(g, false);
		// enabled = false;
	}
	
	public void enable(Genome g){
		enableMap.put(g, true);
	}


	@Override
	public int compareTo(Gene arg0) {
		// TODO Auto-generated method stub
		return this.getId() - arg0.getId();
		}
}

class GeneOutComparator implements Comparator<Gene> {
	@Override
	public int compare(Gene o1, Gene o2) {
		return o1.out.id - o2.out.id;
	}
}
