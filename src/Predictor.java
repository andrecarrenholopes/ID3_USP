
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 22, 2017
 * 
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 */
public class Predictor {

	private String[] Attributes;
	private double accuracy;
	private String dataset;
	private String[] Attributes_orig;
	
	// predict over a trained "tree"
	Predictor(Tree tree, List<Exp> examples, String[] Attributes, int[] Target_attributes, String name_dataset, String[] attrs_orig){
		
		setAttributes(Attributes);
		setDatasetName(name_dataset);
		setOriginalAttributes(attrs_orig);
		
		accuracy = test(tree, examples);
	}
	
	// predict over a trained "rule-set"
	Predictor(ArrayList<Rule> rules, List<Exp> examples, String[] attrs_orig, String target_default){
		
		setOriginalAttributes(attrs_orig);
		
		accuracy = test (rules, examples, target_default);


	}

	private double test(ArrayList<Rule> rules, List<Exp> examples, String target_default) {
		double acc = 0.0;
		double num_matched_exp = .0; // preconditions match; not necessarily the targets
		boolean match = true;
		ArrayList<Rule> matched_rules;
		String target_rule;
		
		for (Exp exp: examples){
			
			matched_rules = new ArrayList<Rule>();
			
			// get targets from example
			String target_exp = exp.getTarget();
			
			for (Rule rule: rules){
				
				// get targets from rule
				target_rule = rule.getTarget();
				
				for (int i = 0; i < rule.size(); i = i + 2){
					
					String attr = rule.getPreconditions().get(i); // e.g. sepal-width <= 1.5
					String v = rule.getPreconditions().get(i + 1); // e.g. t or f
					
					// find the index of attr in global Attributes.
					int idx = Arrays.asList(this.getOrigAttributes()).indexOf(attr.split("<=")[0]);
					// find the value of attribute in example
					String v_exp = exp.get(idx);
					
					// convert both values from string to double
					Double thr = Double.valueOf(attr.split("<=")[1]); // e.g. 1.5
					Double v_exp_double = Double.valueOf(v_exp);

					if (v.equals("true")) {
						match = v_exp_double <= thr ? true:false;
						
					}else if (v.equals("false")){
						match = thr < v_exp_double ? true:false;
					}
					
					if (!match){
						break; // the current example does not match the rule
					}
				}
				
				if (match){
					// keep those rules that match the candidate example "exp"
					matched_rules.add(rule);
				}
				
			}
			
			// if there are multiple rules matched the entry example
			// among all matched rules get the one's target with the highest score
			if (matched_rules.size() > 1){
				// -1/1 descending or ascending order	
				matched_rules = sort(matched_rules, -1);
			}
			
			if (matched_rules.size() == 0){
				// Get the majority of targets over training as default
				target_rule = target_default;
			}else{
				target_rule = matched_rules.get(0).getTarget();
			}
			
			if (target_exp.equals(target_rule)){
				acc++;
			}
			num_matched_exp++;
		}
		
		return 100*(acc/(num_matched_exp));
	}

	private ArrayList<Rule> sort(ArrayList<Rule> rules, int order) {
		for (int i = 0; i < rules.size(); i++) {
			if (i < rules.size() - 1) {
				for (int j = i + 1; j < rules.size(); j++) {
					if (order == rules.get(i).compareTo(rules.get(j))) {
						// compare two examples (-1/1 for less/greater than, and
						// 0 for equals to)
						rules = swap(rules, i, j);
					}
				}
			}
		}
		return rules;
	}

	private ArrayList<Rule> swap(ArrayList<Rule> rules, int i, int j) {
		Rule tmp = new Rule();
		tmp = rules.get(i).clone();
		rules.set(i, rules.get(j).clone());
		rules.set(j, tmp);

		return rules;
	}

	/**
	 * 
	 * @param tree the model (decision tree)
	 * @param examples test data (examples)
	 * @return acc the accuracy
	 */
	private double test(Tree tree, List<Exp> examples) {
		double acc = 0.0;
		
		for (Exp exp: examples){
			String target_exp = exp.getTarget();
			
			// traverse the tree to get to the leaf
			String target_tree = null;
			target_tree = traverse(exp, tree, target_tree);
			
			// compare the tree's target value with example's target value
			if (target_exp.equals(target_tree)){
				acc++;
			}	
			
		}
		
		return 100*(acc/(examples.size()));
	}
	
	private String traverse(Exp exp, Tree tree, String target_tree) throws NullPointerException{
		
		// termination condition
		if (tree.getChildren() == null){
			target_tree = tree.getRoot();
			// get the target value of the tree and return it
			
			return target_tree;
		}
		String root = tree.getRoot();

		if (this.getdatasetName().equals("tennis") || this.getdatasetName().equals("bool")|| this.getdatasetName().equals("enjoy")){
			
			// find the index of attr in global Attributes.
			int idx = Arrays.asList(this.getAttributes()).indexOf(root);
			
			// find the value of attribute in example
			String v = exp.get(idx);
			
			// go deeper to in tree the next level via branch v
			Tree next = tree.getChildren().get(v);
			
			target_tree = traverse(exp, next, target_tree);
			
		}else if (this.getdatasetName().equals("iris")){
			
			// find the index of attr in global Attributes.
			int idx = Arrays.asList(this.getOrigAttributes()).indexOf(root.split("<=")[0]);
			
			// find the value of attribute in example
			String v = exp.get(idx);
			
			// Go deeper to in tree the next level via branch v
			// for A: sepal-length<=6.9 return 6.9 as thr		
			Double thr = Double.valueOf(root.split("<=")[1]);
			Tree next;
			if (Double.valueOf(exp.get(idx)) <= thr){
				next = tree.getChildren().get("true");
			}else{
				next = tree.getChildren().get("false");
			}
			
			target_tree = traverse(exp, next, target_tree);

			
		}
		
		return target_tree;
	}

	// auxiliary functions

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
	
	protected double getAccuracy(){
		return this.accuracy;
	}
}
