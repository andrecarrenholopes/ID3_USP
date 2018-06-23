import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Regra implements Comparable<Regra>{

	private ArrayList<String> condic;
	private String target;
	private double score;
	private double num_matched_exp_global;
	
	public double getNum_matched_exp_global() {
		return num_matched_exp_global;
	}

	public void setNum_matched_exp_global(double num_matched_exp_global) {
		this.num_matched_exp_global = num_matched_exp_global;
	}

	Regra(ArrayList<String> pre, String t, double s){
		this.setPreconditions(pre);
		this.setTarget(t);
		this.setScore(s);
	}
	
	Regra (ArrayList<String> pre){
		ArrayList<String> tmp = new ArrayList<String>();
		for (int i = 0; i < pre.size() - 1; i++){
			tmp.add(pre.get(i));
		}
		
		this.setPreconditions(tmp);
		this.setTarget(pre.get(pre.size()-1));
		this.setScore(0);
	}
	
	Regra (){
		this.setPreconditions(new ArrayList<String>());
		this.setTarget(null);
		this.setScore(0);
	}
	
	@Override
	public int compareTo(Regra r) {
		int res = Double.compare(this.getScore(), r.getScore());
		return res;
	}
	
	public Regra clone (){
		Regra r = new Regra();
		
		r.setPreconditions(this.getPreconditions());
		r.setTarget(this.getTarget());
		r.setScore(this.getScore());
		
		return r;
	}
	
	public void assignScore(List<Dados> examples, String[] atrib_orig) {
		double acc = 0.0;
		double num_matched_exp = .0;
		boolean match = true;
		
		for (Dados exemp: examples){
			
			String target_exp = exemp.getTarget();
			String target_rule = this.getTarget();
			
			for (int i = 0; i < this.size(); i = i + 2){
				
				String attr = this.getPreconditions().get(i);
				String v = this.getPreconditions().get(i + 1);
				
				int idx = Arrays.asList(atrib_orig).indexOf(attr.split("<=")[0]);
				String v_exp = exemp.get(idx);
				
				if(attr.contains("<=")){
					Double thr = Double.valueOf(attr.split("<=")[1]); 

					Double v_exp_double = Double.valueOf(v_exp);
					if (v.equals("true")) {
						match = v_exp_double <= thr ? true:false;
						
					}else if (v.equals("false")){
						match = thr < v_exp_double ? true:false;
					}
					
				}else {
					match = v.equals(v_exp);
				}
				
				if (!match){
					break; 
				}
	
			}
			
			if (match){
				if (target_exp.equals(target_rule)){
					acc++;
				}
				num_matched_exp++;
			}
		}
		
		assert (num_matched_exp <= 0);
		score = acc/(num_matched_exp);
		score = num_matched_exp == 0? 0 : score;

		//score = num_matched_exp / examples.size();
		num_matched_exp_global = num_matched_exp;
		this.setScore(score);	
	}

	
	public int size(){
		return (this.condic.size());
	}
	
	public Regra remove (int idx){
		this.getPreconditions().remove(idx);
		return this;
	}
	
	public boolean isEmpty (){
		return (this.getPreconditions().size()== 0?true:false);
	}

	public void setPreconditions(ArrayList<String> argin){
		this.condic = new ArrayList<String>();
		for (String p: argin){
			condic.add(p);
		}
	}
	
	public void setTarget(String argin){
		this.target = argin;
	}
	
	public void setScore(double s){
		this.score = s;
	}
	
	public ArrayList<String> getPreconditions(){
		return this.condic;
	}
	
	public String getTarget(){
		return this.target;
	}
	
	public double getScore(){
		return this.score;
	}
}
