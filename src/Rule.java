import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 20, 2017
 * 
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 * 
 * A rule contains a bunch of preconditions (e.g. sepal-width <= 4.49) plus a target (virginica)
 * Also a rule has a score when its preconditions are being pruned.
 */
public class Rule implements Comparable<Rule>{

	private ArrayList<String> preconditions;
	private String target;
	private double score; // rule's example coverage
	
	// overloaded constructor
	Rule(ArrayList<String> pre, String t, double s){
		this.setPreconditions(pre);
		this.setTarget(t);
		this.setScore(s);
	}
	
	Rule (ArrayList<String> pre){
		ArrayList<String> tmp = new ArrayList<String>();
		for (int i = 0; i < pre.size() - 1; i++){
			tmp.add(pre.get(i));
		}
		
		this.setPreconditions(tmp);
		this.setTarget(pre.get(pre.size()-1)); // the last one is the target of the rule
		this.setScore(0);
	}
	
	Rule (){
		this.setPreconditions(new ArrayList<String>());
		this.setTarget(null);
		this.setScore(0);
	}
	
	@Override
	public int compareTo(Rule r) {
		int res = Double.compare(this.getScore(), r.getScore());
		return res;
	}
	
	public Rule clone (){
		Rule r = new Rule();
		
		r.setPreconditions(this.getPreconditions());
		r.setTarget(this.getTarget());
		r.setScore(this.getScore());
		
		return r;
	}
	
	public void assignScore(List<Exp> examples, String[] attrs_orig) {
		double acc = 0.0;
		double num_matched_exp = .0; // preconditions match; not necessarily the targets
		boolean match = true;
		int counter = 1;
		
		for (Exp exp: examples){
			
			// get targets from example and rule
			String target_exp = exp.getTarget();
			String target_rule = this.getTarget();
			
			//System.out.println(counter); counter++;
			for (int i = 0; i < this.size(); i = i + 2){
				
				String attr = this.getPreconditions().get(i); // e.g. sepal-width <= 1.5
				String v = this.getPreconditions().get(i + 1); // e.g. t or f
				
				
				// find the index of attr in global Attributes.
				int idx = Arrays.asList(attrs_orig).indexOf(attr.split("<=")[0]);
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
				// compare the tree's target value with example's target value
				if (target_exp.equals(target_rule)){
					acc++;
				}
				num_matched_exp++;
			}
		}
		
		assert (num_matched_exp <= 0);
		score = acc/(num_matched_exp);
		score = num_matched_exp == 0? 0 : score;

		// DOUBLE CHECK THIS PART!!!!!!!!!!!
		score = num_matched_exp / examples.size();
		this.setScore(score);	
	}

	
	public int size(){
		return (this.preconditions.size());
	}
	
	public Rule remove (int idx){
		this.getPreconditions().remove(idx);
		return this;
	}
	
	public boolean isEmpty (){
		return (this.getPreconditions().size()== 0?true:false);
	}

	// accessors and mutators
	public void setPreconditions(ArrayList<String> argin){
		this.preconditions = new ArrayList<String>();
		for (String p: argin){
			preconditions.add(p);
		}
	}
	
	public void setTarget(String argin){
		this.target = argin;
	}
	
	public void setScore(double s){
		this.score = s;
	}
	
	public ArrayList<String> getPreconditions(){
		return this.preconditions;
	}
	
	public String getTarget(){
		return this.target;
	}
	
	public double getScore(){
		return this.score;
	}

	
	
}
