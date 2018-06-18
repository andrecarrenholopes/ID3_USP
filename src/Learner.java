import java.util.*;

/**
 * 
 * Learner receives an array list of training examples and trains a decision tree given ID3 algorithm.
 * Learner returns a Tree.
 */
public class Learner {

	private HashMap<String, ArrayList<String>> attr_vals;
	private Tree tree = new Tree();
	private String[] Attributes;
	private String dataset;
	private String[] Attributes_orig;
	private static String[] Attributes_minus_A;
	//private static int counter = 0;
	private List<Exp> Examples_test;


	// constructor
	Learner (List<Exp> Examples, String[] Attributes, HashMap<String,
			ArrayList<String>> attr_vals, int[] Target_attributes, String name_dataset, String[] attrs_orig, List<Exp> Test){

		setAttribute_values(attr_vals);
		setAttributes(Attributes);
		setDatasetName(name_dataset);
		setOriginalAttributes(attrs_orig);
		Attributes_minus_A = getAttributes();
		Examples_test = Test;
		
		// train the dataset by modeling a decision tree
		tree = train(Examples, Target_attributes);

		// set the general attributes of the tree
		tree.setAttribute(this.getAttributes());

		// assign parents to each node
		tree.assignParents(null);

	}

	// called functions
	@SuppressWarnings("rawtypes")
	private Tree train(List<Exp> Examples, int[] Target_attributes){

		// 1. CREATE A Root NODE FOR THE TREE
		Tree tree = new Tree();

		// 2. TERMINATION CRITEREA
		tree = check_termination_conditions(tree, Examples, Attributes_minus_A, Target_attributes);
		if (tree != null) {
			return tree;
		} else {

			// 3. OTHERWISE BEGIN ALGORITHM
			tree = new Tree();


			// 4. A = FIND THE BEST ATTRIBUTE THAT BEST CLASSIFIES EXAMPLES (e.g. Outlook)
			String best_attr = findBestAttr(Examples, Target_attributes, Attributes_minus_A);

			/*if (counter == 0) { best_attr = "Outlook";}//tree.setRoot(best_attr);} 
			else if (counter == 1) { best_attr = "Humidity"; }
			else if (counter == 2){ best_attr = "Wind"; }
			counter++;
			 */

			// 5. MOUNT THE BEST ATTR (A) FROM 4. TO THE Root
			tree.setRoot(best_attr);


			// 6. GO OVER EACH VALUE vi OF A
			Iterator itr = attr_vals.get(best_attr).iterator();
			HashMap <String, Tree> children = new HashMap<String, Tree>();

			Attributes_minus_A = trim_Attrs(Attributes_minus_A, best_attr);

			while (itr.hasNext()){
				Tree subtree = new Tree();

				// 7. FOR vi ADD A NEW TREE BRANCH BELOW Root
				String vi = (String) itr.next();
				children.put(vi, null);


				// 8. EXAMPLES_vi IS THE SUBSET OF EXAMPLES THAT HAVE VALUE vi FOR A
				//List<Exp> examples_vi = trim_Examples (Examples, best_attr, vi); 
				List<Exp> examples_vi = derive_examples (Examples, best_attr, vi);

				// 9. IF EXAMPLES_vi IS EMPTY (YOU REACHED TO THE TARGET-ATTRIBUTE)
				if (examples_vi.isEmpty()){
					subtree = getMostCommonValue(subtree, Examples, Target_attributes);
					children.put(vi, subtree);
					tree.setChildren(children);
				}else{

					// 10. ELSE BELOW THIS NEW BRANCH ADD A SUBTREE (Recursively)
					//Attributes_minus_A = trim_Attrs(Attributes_minus_A, best_attr);
					subtree = train( examples_vi, Target_attributes);
					children.put(vi, subtree);
					tree.setChildren(children);
				}	
				/*if(examples_vi.size() != 0) {
				Predictor predictor = new Predictor (subtree, Examples, Attributes, Target_attributes, dataset, Attributes_orig);
				System.out.printf("Train "+Examples.size()+"Size of "+examples_vi.size()+" The accuracy is %.1f%%%n%n" , predictor.getAccuracy());
				Predictor predictortest = new Predictor (subtree, Examples_test, Attributes, Target_attributes, dataset, Attributes_orig);
				System.out.printf("Test "+Examples.size()+"Size of "+examples_vi.size()+" The accuracy is %.1f%%%n%n" , predictortest.getAccuracy());
				}*/
			}


		}
		return tree;

	}

	private String[] trim_Attrs(String[] attributes, String best_attr) {
		// remove best_attr from attributes and return the rest
		String[] output_attrs;

		if ((attributes.length - 1) == 0){
			return null; // empty attribute
		}else{
			output_attrs = new String[attributes.length - 1];
			//output_attrs = new String[attributes.length];

			int j = 0;

			for (int i = 0; i < attributes.length; i++){
				if (!(attributes[i].equals(best_attr))){
					output_attrs[j] = attributes[i];
					j++;
				}
			}
			return output_attrs;
		}

	}

	private Tree getMostCommonValue(Tree tree, List<Exp> examples, int[] Target_attributes) {
		// given a set of examples returns a tree 
		// This tree contains only a target attribute as it's root.

		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Exp exp: examples){
				if (exp.getTarget().equals(replace(i))){
					counter[i]++;
				}
			}
		}

		// vote for the most (find the maximum)
		int sentinel = Integer.MIN_VALUE;
		for (int i = 0; i < counter.length; i++){
			if (counter[i] > sentinel){
				tree.setRoot(replace(i));
				sentinel = counter[i];
			}
		}


		return tree;
	}

	private String replace(int i) {
		// 0 is replaced with Yes or Iris-setosa
		// 1 is replaced with No or Iris-versicolor
		// 2 is replaced with Iris-virginica

		String output = "noun";

		if (this.getdatasetName().equals("tennis") || this.getdatasetName().equals("bool") || this.getdatasetName().equals("enjoy")){
			switch (i) {
			case 0:
				output = "Yes";
				break;
			case 1:
				output = "No";
				break;
			default:
				output = "noun";
				break;
			}
		}else if (this.getdatasetName().equals("iris")){
			switch (i) {
			case 0:
				output = "Iris-setosa";
				break;
			case 1:
				output = "Iris-versicolor";
				break;
			case 2:
				output = "Iris-virginica";
				break;
			default:
				output = "noun";
				break;
			}
		}else if (this.getdatasetName().equals("adult")) {
			switch (i) {
			case 0:
				output = ">50K";
				break;
			case 1:
				output = "<=50K";
				break;
			default:
				output = "noun";
				break;
			}
		}

		return output;
	}

	/*
	private List<Exp> trim_Examples(List<Exp> examples, String A, String vi) {
		// returns those examples whose value for the attribute A is vi

		List<Exp> output_examples = new ArrayList<Exp>();

		// find the index of attributes that we are looking for.
		int idx = Arrays.asList(this.getAttributes()).indexOf(A);

		for (Exp exp: examples){
			if (exp.get(idx).equals(vi)){
				// add this example to examples_vi for output
				output_examples.add(exp);
			}
		}

		return output_examples;
	}*/

	public String findBestAttr(List<Exp> S, int[] Target_attributes, String[] Attributes) {

		// The entropy for the entire set S before being partitioned
		double entropy_S = cal_entropy(S, Target_attributes);

		double S_size = S.size();

		double gain = 0.0, max = -Double.MAX_VALUE;

		String best_attribute = "";

		// Expected value of the entropy after S is partitioned using attr A
		// for each attr A calculate the entropy and subtract it from S's entropy

		for (String attr: Attributes){

			double sigma = 0.0;

			Iterator itr = attr_vals.get(attr).iterator();
			while (itr.hasNext()){

				// 1. count the number of examples whose attribute "attr" has the value v
				String v = (String) itr.next();
				List<Exp> S_v = derive_examples(S, attr, v);

				double Sv_size = S_v.size();

				// 2. calculate |S_v| / |S|
				double ratio = Sv_size / S_size;

				// 3. calculate (|S_v| / |S|) * entropy(Sv)
				double entropy_Sv = 0;
				if (ratio == 0){
					entropy_Sv = 0;
				}else{

					entropy_Sv = cal_entropy(S_v, Target_attributes);
				}
				// 4. calculate SIGMA [(|S_v| / |S|) * entropy(Sv)]
				sigma+= - ratio * entropy_Sv;

			}

			// calculate Gain (S, A)
			// Gain(S, A) = entropy(S) - SIGMA [(|S_v| / |S|) * entropy(Sv)]
			gain = entropy_S + sigma;
			if (gain > max){
				max = gain;
				// keep this attribute. It's gain is the maximum amonge all attributes.
				best_attribute = attr;
			}
		}

		return best_attribute;
	}


	private List<Exp> derive_examples(List<Exp> S, String A, String v) {
		// count the number of examples whose attribute "attr" has the value v
		// v is implied in each iteration using iterator

		List<Exp> tmp = new ArrayList<Exp>();

		if (this.getdatasetName().equals("tennis") || this.getdatasetName().equals("bool") || this.getdatasetName().equals("enjoy")){

			// find the index of attr in global Attributes.
			int idx = Arrays.asList(this.getAttributes()).indexOf(A);

			for (Exp exp: S){
				if (exp.get(idx).equals(v)){
					tmp.add(exp);
				}
			}
		}else if (this.getdatasetName().equals("iris")) {

			// find the index of attr in global Attributes.
			int idx = Arrays.asList(this.getOrigAttributes()).indexOf(A.split("<=")[0]);

			// for A: sepal-length<=6.9 return 6.9 as thr		
			Double thr = Double.valueOf(A.split("<=")[1]);

			//int comparable = v.equals("true")?

			for (Exp exp: S){
				if (v.equals("true")){
					if (Double.valueOf(exp.get(idx)) <= thr){
						tmp.add(exp);
					}
				}else{
					if (Double.valueOf(exp.get(idx)) > thr){
						tmp.add(exp);
					}
				}
			}
		} else if (this.getdatasetName().equals("adult")){
			// find the index of attr in global Attributes.
			
			int idx;
			if (A.contains("<=")){//se for continuo
				idx = Arrays.asList(this.getOrigAttributes()).indexOf(A.split("<=")[0]);
			
			// for A: sepal-length<=6.9 return 6.9 as thr	
				Double thr = Double.valueOf(A.split("<=")[1]);
	
				//int comparable = v.equals("true")?
	
				for (Exp exp: S){
					if (v.equals("true")){
						if (Double.valueOf(exp.get(idx)) <= thr){
							tmp.add(exp);
						}
					}else{
						if (Double.valueOf(exp.get(idx)) > thr){
							tmp.add(exp);
						}
					}
				}
			}else { //se for discreto
				idx = Arrays.asList(this.getOrigAttributes()).indexOf(A);
			
				for (Exp exp: S){
					if (exp.get(idx).equals(v)){
						tmp.add(exp);
					}
				}
			}
		}

		return tmp;
	}


	private Tree check_termination_conditions(Tree tree, List<Exp> examples, String[] attrs, int[] Target_attributes) {
		// 1. &
		// 2. if all examples are pos/neg in tennis OR
		// versicolor/setosa/virginica in iris
		// all positive (/versicolor)
		// all negative (/virginica)
		// all setosa

		boolean unified = true;

		// if no more examples
		if (examples.size() == 0){
			return tree;

		}
		String target_val = examples.get(0).getTarget();

		for (Exp exp: examples){
			String tmp_target = exp.getTarget();
			if (!target_val.equals(tmp_target)){
				unified = false;
				break;
			}
		}

		if (unified){
			// return a single node as a tree containing only the value of target attr
			tree.setRoot(target_val);
			return tree;
		}


		// 3. if no more attributes
		// no more attributes
		if (attrs == null){
			tree = getMostCommonValue(tree, examples, Target_attributes);
			return tree;
		}

		if (attrs.length == 0){
			tree = getMostCommonValue(tree, examples, Target_attributes);
			return tree;
		}



		return null;
	}

	private double cal_entropy(List<Exp> S, int[] Target_attributes){
		// Measure the impurity in collection S

		// count each target attribute's value
		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Exp exp: S){
				if (exp.getTarget().equals(replace(i))){
					counter[i]++;
				}
			}
		}

		// find the ratio of each target attr's value
		double[] p = new double[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){
			p[i] = counter[i]/(double) S.size();
		}

		// sum up all p's given the formula Entropy(S) = SUMoverClasses(- p * log(p))
		double sum = 0.0;
		for (int i = 0; i < p.length; i++){
			if (p[i] != 0){
				sum += (-p[i] * Math.log(p[i]) / Math.log(2));
			}
		}

		return sum;
	}

	// auxiliary functions

	@SuppressWarnings("unchecked")
	private void setAttribute_values(HashMap<String, ArrayList<String>> argin) {
		this.attr_vals = (HashMap<String, ArrayList<String>>) argin.clone();


	}
	protected void setAttributes(String[] attrs) {
		this.Attributes = new String[attrs.length];
		for (int i = 0; i < attrs.length; i++){
			Attributes[i] = attrs[i];
		}

	}
	protected String[] getAttributes (){
		return this.Attributes;
	}

	private void setOriginalAttributes(String[] attrs_orig) {
		this.Attributes_orig = new String[attrs_orig.length];
		for (int i = 0; i < attrs_orig.length; i++){
			Attributes_orig[i] = attrs_orig[i];
		}

	}

	protected String[] getOrigAttributes (){
		return this.Attributes_orig;
	}


	private String getdatasetName(){
		return this.dataset;
	}

	private void setDatasetName(String argin) {
		this.dataset = argin;

	}

	public Tree getTree() {	
		return this.tree;
	}

}
