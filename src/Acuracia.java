
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Acuracia {

	private String[] Atributos;
	private double accuracy;
	private String dataset;
	private String[] Atributos_orig;
	
	Acuracia(Arvore arvr, List<Dados> examples, String[] Atributos, int[] Target_attributes, String name_dataset, String[] atrib_orig){
		
		setAttributes(Atributos);
		setDatasetName(name_dataset);
		setOriginalAttributes(atrib_orig);
		
		accuracy = test(arvr, examples);
	}
	
	Acuracia(ArrayList<Regra> regr, List<Dados> examples, String[] atrib_orig, String target_default){
		setOriginalAttributes(atrib_orig);		
		accuracy = test (regr, examples, target_default);
	}

	private double test(ArrayList<Regra> regr, List<Dados> examples, String target_default) {
		double acc = 0.0;
		double num_matched_exp = .0;
		boolean match = true;
		ArrayList<Regra> matched_rules;
		String target_rule;
		
		for (Dados exemp: examples){
			
			matched_rules = new ArrayList<Regra>();
			
			String target_exp = exemp.getTarget();
			
			for (Regra rule: regr){
				
				target_rule = rule.getTarget();
				
				for (int i = 0; i < rule.size(); i = i + 2){
					
					String attr = rule.getPreconditions().get(i);
					String v = rule.getPreconditions().get(i + 1);
					
					int idx = Arrays.asList(this.getOrigAttributes()).indexOf(attr.split("<=")[0]);
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
					matched_rules.add(rule);
				}
				
			}
			
			if (matched_rules.size() > 1){
				matched_rules = sort(matched_rules, -1);
			}
			
			if (matched_rules.size() == 0){
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

	private ArrayList<Regra> sort(ArrayList<Regra> regr, int order) {
		for (int i = 0; i < regr.size(); i++) {
			if (i < regr.size() - 1) {
				for (int j = i + 1; j < regr.size(); j++) {
					if (order == regr.get(i).compareTo(regr.get(j))) {
						regr = swap(regr, i, j);
					}
				}
			}
		}
		return regr;
	}

	private ArrayList<Regra> swap(ArrayList<Regra> regr, int i, int j) {
		Regra tmp = new Regra();
		tmp = regr.get(i).clone();
		regr.set(i, regr.get(j).clone());
		regr.set(j, tmp);

		return regr;
	}

	private double test(Arvore arvr, List<Dados> examples) {
		double acc = 0.0;
		
		for (Dados exemp: examples){
			String target_exp = exemp.getTarget();
			
			String target_tree = null;
			target_tree = traverse(exemp, arvr, target_tree);
			
			if (target_exp.equals(target_tree)){
				acc++;
			}	
			
		}
		
		return 100*(acc/(examples.size()));
	}
	
	private String traverse(Dados exemp, Arvore arvr, String target_tree) throws NullPointerException{
		
		if (arvr.getChildren() == null){
			target_tree = arvr.getRoot();
			
			return target_tree;
		}
		String raiz = arvr.getRoot();

		if (this.getdatasetName().equals("tennis") || this.getdatasetName().equals("bool")|| this.getdatasetName().equals("enjoy")){
			
			int idx = Arrays.asList(this.getAttributes()).indexOf(raiz);
			
			String v = exemp.get(idx);
			
			Arvore next = arvr.getChildren().get(v);
			
			target_tree = traverse(exemp, next, target_tree);
			
		}else if (this.getdatasetName().equals("iris")){
			int idx = Arrays.asList(this.getOrigAttributes()).indexOf(raiz.split("<=")[0]);
			
			String v = exemp.get(idx);
			
			Double thr = Double.valueOf(raiz.split("<=")[1]);
			Arvore next;
			if (Double.valueOf(exemp.get(idx)) <= thr){
				next = arvr.getChildren().get("true");
			}else{
				next = arvr.getChildren().get("false");
			}
			
			target_tree = traverse(exemp, next, target_tree);

			
		}else if (this.getdatasetName().equals("adult")){
			int idx = Arrays.asList(this.getOrigAttributes()).indexOf(raiz.split("<=")[0]);
			
			String v = exemp.get(idx);
			Arvore next;
			if(raiz.contains("<=")){
				Double thr = Double.valueOf(raiz.split("<=")[1]);
				if (Double.valueOf(exemp.get(idx)) <= thr){
					next = arvr.getChildren().get("true");
				}else{
					next = arvr.getChildren().get("false");
				}
			}else {
				next = arvr.getChildren().get(v);
			}
			
			target_tree = traverse(exemp, next, target_tree);

			
		}
		
		return target_tree;
	}

	protected void setAttributes(String[] atrbs) {
		this.Atributos = new String[atrbs.length];
		for (int i = 0; i < atrbs.length; i++){
			Atributos[i] = atrbs[i];
		}
		
	}
	
	protected String[] getAttributes (){
		return this.Atributos;
	}
	
	private void setOriginalAttributes(String[] atrib_orig) {
		this.Atributos_orig = new String[atrib_orig.length];
		for (int i = 0; i < atrib_orig.length; i++){
			Atributos_orig[i] = atrib_orig[i];
		}
		
	}
	
	protected String[] getOrigAttributes (){
		return this.Atributos_orig;
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
