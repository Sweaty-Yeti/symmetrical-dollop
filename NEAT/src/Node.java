

class Node implements Comparable<Node> {

	static int count = 0;
	int id;
	static OverGen og;
	boolean bias, input, output;
	boolean calculated;
	public float value = 0f;

	public Node(OverGen ovG) {
		// Use to create bias node
		Node.og = ovG;
		id = count++;
		bias = true;
		input = false;
		output = false;
		value = 1f;
		calculated = true;
		// og.addGene(og.bias,this);
	}

	public Node(int mode) {
		// Use to create input/output nodes
		id = count++;
		bias = false;
		input = (mode == 0);
		output = (mode == 1);
		calculated = input;
	}

	public Node() {
		// Use to create hidden nodes
		id = count++;
		bias = false;
		input = false;
		output = false;
	}

	@Override
	public int compareTo(Node arg0) {
		return this.id - arg0.id;
	}

	public float calculateNode(Genome gnm, int depth) {
		if (calculated) {
			return value;
		}
		if(depth <= 0){
			@SuppressWarnings("unused")
			int a  = 0;
			//System.out.println();
		}
		float tmp = 0f;
		for (Gene g : gnm.genome.get(this)) {
			tmp += g.in.calculateNode(gnm, depth -1) * g.weightMap.get(gnm);
		}

		calculated = true;
		value = (float) (1 / (1 + Math.exp(-5 * tmp)));
		return value;
	}
}
/*
 * class Node implements Comparable<Node>{ Genome gnm; int id; float val;
 * boolean input; boolean output; boolean bias; boolean calculated; int state;
 * TreeSet<Node> outBan; public Node(Genome g){ gnm = g; id = gnm.nodeCount++;
 * val = 0f;
 * 
 * outBan = new TreeSet<Node>(); outBan.add(this); } public Node(int mode,
 * Genome g){ gnm = g; id = gnm.nodeCount++; state = mode; input = (mode == 0 ?
 * true : false); output= (mode == 2 ? true : false); outBan = new
 * TreeSet<Node>(); outBan.add(this); if(mode != 0){ gnm.addGene(gnm.bias,
 * this); } } public int getId(){ return id; }
 * 
 * public void update(Node a){
 * 
 * }
 * 
 * public boolean equals(Node arg0){ return (this.id == arg0.id); }
 * 
 * @Override public int compareTo(Node arg0) {
 * 
 * return arg0.id - this.id; }
 * 
 * }
 */