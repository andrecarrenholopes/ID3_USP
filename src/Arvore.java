import java.util.*;
import java.util.Map.Entry;


public class Arvore {

	private Arvore pai;
	private String raiz;
	private HashMap<String, Arvore> filho;
	private final int shift_disp = 10;
	private static ArrayList<String> buffer = new ArrayList<String>();
	private static ArrayList<String> condic = new ArrayList<String>();
	private static ArrayList<Regra> regr = new ArrayList<Regra>();
	private static boolean signal = false;
	private String approach; 
	private static String[] atrbs;
	private static Arvore copia_arvr = new Arvore();
	private static boolean stop = false;
	private static boolean up = false;
	private static boolean vaipoda = false;
	private static String corta_atrib;
	private static int counter = 0;
	private static int totalNodes = 0;
	
	public Arvore() {
		this.setPruneApproach("reduced-error pruning");
	}

	public void setRoot(String r) {
		this.raiz = r;
	}

	public String getRoot() {
		return this.raiz;
	}

	@SuppressWarnings("unchecked")
	public void setChildren(HashMap<String, Arvore> argin) {
		if (argin != null) {
			this.filho = (HashMap<String, Arvore>) argin.clone();
		} else {
			this.filho = null;
		}
	}

	public HashMap<String, Arvore> getChildren() {
		return this.filho;
	}

	public void updateChildren(String k, Arvore v) {
		this.filho.put(k, v);

	}

	public void setParent(Arvore p) {
		this.pai = p;
	}

	public Arvore getParent() {
		return this.pai;
	}

	public int display(int spaces) throws CloneNotSupportedException {
		Arvore tmp = new Arvore();
		tmp = this.clone();

		Iterator<Entry<String, Arvore>> itr;

		if (tmp.getChildren() == null) {
			System.out.printf(" >> %s%n", tmp.getRoot());
			return spaces;
		} else {
			System.out.printf(" %s%n", tmp.getRoot());
			itr = tmp.getChildren().entrySet().iterator();
		}
		while (itr.hasNext()) {

			Entry<String, Arvore> t = itr.next();
			System.out.printf("%s", String.format("%1$" + spaces + "s", t.getKey()));
			tmp = t.getValue();

			if (tmp.getChildren() != null) {
				spaces += getShiftSpace();
			}

			spaces = tmp.display(spaces);
		}
		return spaces - getShiftSpace();

	}

	public void displayRules() {

		String raiz = this.getRoot();
		buffer.add(raiz); 
		if (buffer.get(0).equals("false")){
			System.out.println();
		}
		if (this.getChildren() == null) {
			for (int i = 0; i < buffer.size() - 1; i++) {

				if (buffer.get(0).equals("false")){
					System.out.println();
				}
				System.out.printf("%s", buffer.get(i));

				if ((i % 2) == 0) {
					System.out.print(" = ");
				} else {
					if (i < buffer.size() - 2) {
						System.out.print(" ^ ");
					}
				}
			}

			System.out.printf(" => %s%n", buffer.get(buffer.size() - 1));
			signal = true;
			return;
		} else {
			signal = false;
		}

		Iterator<Entry<String, Arvore>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Arvore> t = itr.next();
			if ((buffer.size() > 2) && (signal)) {
				buffer.remove(buffer.size() - 1);
				buffer.remove(buffer.size() - 1);
			}
			buffer.add(t.getKey()); 

			t.getValue().displayRules();

		}
		if ((buffer.size() > 2) && (signal)) {
			buffer.remove(buffer.size() - 1);
			buffer.remove(buffer.size() - 1);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected Arvore clone() {

		Arvore tmp = new Arvore();

		tmp.setRoot(this.getRoot());
		tmp.setParent(this.getParent());
		if (this.getChildren() != null) {
			tmp.setChildren((HashMap<String, Arvore>) this.getChildren().clone());
		}
		return tmp;

	}

	protected int getShiftSpace() {
		return this.shift_disp;
	}

	protected void setPruneApproach(String argin) {
		this.approach = argin.toLowerCase();
	}

	protected String getPruneApproach() {
		return this.approach;
	}

	public Arvore prune(List<Dados> examples_train, List<Dados> examples_val, ArrayList<Regra> regr, String[] Atributos,
			int[] classes, String name_dataset, String[] atrib_orig, double acc_val_ref)
			throws CloneNotSupportedException {

		Arvore pruned_tree;

		pruned_tree = reduced_error_prun(examples_train, examples_val, Atributos, classes, name_dataset, atrib_orig,
				acc_val_ref);

		return pruned_tree;
	}

	public ArrayList<Regra> prune_RulePostPruning(List<Dados> examples_train, List<Dados> examples_val, ArrayList<Regra> regr, String[] Atributos,
			int[] classes, String name_dataset, String[] atrib_orig, double acc_val_ref, String target_default, boolean noise)
			throws CloneNotSupportedException {

		ArrayList<Regra> pruned_ruleset;

		if (!noise){
		System.out.printf("%n--------%nRule Post-Pruning:%n--------");}
		pruned_ruleset = rule_post_prune(examples_train, examples_val, regr, Atributos, classes, name_dataset, atrib_orig,
				acc_val_ref, target_default);
		

		return pruned_ruleset;
	}

	protected void setAttrCut() {
		this.corta_atrib = null;
	}
	
	private Arvore reduced_error_prun(List<Dados> examples_train, List<Dados> examples_val, String[] Atributos,
			int[] classes, String name_dataset, String[] atrib_orig, double acc_val_ref)
			throws CloneNotSupportedException {
		Arvore pruned_tree = this.cut(corta_atrib, examples_train, examples_val, classes, name_dataset, atrib_orig, acc_val_ref);

		return pruned_tree;
	}

	private String what_to_cut() {
		int index = 0;
		String corta_atrib = this.getAttribute()[index];

		return corta_atrib;
	}

	public Arvore cut(String corta_atrib, List<Dados> examples_train, List<Dados> examples_val, int[] classes,
			String name_dataset, String[] atrib_orig, double acc_val_ref) throws CloneNotSupportedException {
		copia_arvr = this.clone();

		buffer.clear();
		setAttrCut();
		copia_arvr = traverse(copia_arvr, examples_train, examples_val, classes, acc_val_ref, name_dataset,
				atrib_orig);
		return copia_arvr;

	}

	private Arvore traverse(Arvore arvr, List<Dados> examples_train, List<Dados> examples_val, int[] classes,
			double acc_val_ref, String name_dataset, String[] atrib_orig) throws CloneNotSupportedException {
		String raiz = arvr.getRoot();
		buffer.add(raiz);

		Iterator<Entry<String, Arvore>> itr;
		Arvore tmp = null;
		
		if (arvr.getChildren() == null) {
			return arvr;
		} else {
			itr = arvr.getChildren().entrySet().iterator();
		}

		if (up) {
			if (arvr.getRoot().equalsIgnoreCase(corta_atrib)) {
				vaipoda = true;
			} else {
				vaipoda = false;
			}
		} else {
			if (corta_atrib == null) {
				while (itr.hasNext()) { 
					if (itr.next().getValue().getChildren() != null) {
						vaipoda = false;
						break;
					}
					vaipoda = true;
				}
			} else {
				vaipoda = false;
			}
		}
		if (vaipoda) {
			Arvore t = new Arvore(), t2 = new Arvore();
			t = arvr.clone();
			t2 = arvr.clone();
			int[] counter = new int[classes.length];

			for (int i = 0; i < counter.length; i++) {

				for (Dados exemp : examples_train) {
					if(name_dataset.equals("adult")) {
						if (exemp.getTarget().equals(replaceAdult(i))) { 
							counter[i]++;
						}
					}
				}
			}
			int max = 0;
			int sentinel = Integer.MIN_VALUE;
			for (int i = 0; i < counter.length; i++) {
				if (counter[i] > sentinel) {
					sentinel = counter[i];
					max = i;
				}
			}
			if(name_dataset.equals("adult")) {
				t.setRoot(replaceAdult(max));
			}
			
			
			t.setChildren(null);

			this.counter = 0;

			form(t, copia_arvr);

			Acuracia predictor = new Acuracia(copia_arvr, examples_val, atrbs, classes, name_dataset, atrib_orig);
			double acc = predictor.getAccuracy();
			if (acc >= acc_val_ref) {
				if(t.getParent()!=null) {
					corta_atrib = t.getParent().getRoot();
				}
				up = true;


			} else {
				up = false;
				this.counter = 0;
				unform(t2, copia_arvr);

			}

			return t;
		}

		if (arvr.getChildren() == null) {
			return arvr;
		} else {
			itr = arvr.getChildren().entrySet().iterator();
		}

		while (itr.hasNext()) {

			if (up) {
				break;
			}
			Entry<String, Arvore> t = itr.next();

			tmp = t.getValue();
			buffer.add(t.getKey());
			tmp = traverse(tmp, examples_train, examples_val, classes, acc_val_ref, name_dataset, atrib_orig);
			if (buffer.size() > 1) {
				buffer.remove(buffer.size() - 1);
				buffer.remove(buffer.size() - 1);
			}

			if (up) {
				up = false;
				buffer.clear();
				tmp = traverse(copia_arvr, examples_train, examples_val, classes, acc_val_ref, name_dataset,
						atrib_orig);
			}

		}
		return copia_arvr;
	}

	private void unform(Arvore t, Arvore ref_tree) throws CloneNotSupportedException {

		String next = buffer.get(counter);
		if (counter == buffer.size() - 1) {
			ref_tree.setRoot(t.getRoot());
			ref_tree.setChildren(t.getChildren());

			stop = true;

			return;
		}
		while (next != null) {
			if (ref_tree.getRoot().equalsIgnoreCase(next)) {
				counter++;
				next = buffer.get(counter);
				Arvore tmp = ref_tree.getChildren().get(next);
				counter++;
				unform(t, tmp);
				if (stop) {
					break;
				}
			}

		}

	}

	private void form(Arvore t, Arvore ref_tree) throws CloneNotSupportedException {

		String next = buffer.get(counter);
		if (counter == buffer.size() - 1) {
			ref_tree.setRoot(t.getRoot());
			ref_tree.setChildren(null);

			stop = true;

			return;
		}
		while (next != null) {
			if (ref_tree.getRoot().equalsIgnoreCase(next)) {
				counter++;
				next = buffer.get(counter);
				Arvore tmp = ref_tree.getChildren().get(next);
				counter++;
				form(t, tmp);
				if (stop) {
					break;
				}
			}

		}

	}

	private ArrayList<Regra> rule_post_prune(List<Dados> examples_train, List<Dados> examples_val, ArrayList<Regra> regr, String[] Atributos,
			int[] classes, String name_dataset, String[] atrib_orig, double acc_val_ref, String target_default) {

		do {

			ArrayList<Regra> rules_cloned = deepcopy(regr);

			regr = sort(regr, 1);
			Regra rule_max = regr.get(0);
			cut(rule_max); 
			if (rule_max.isEmpty()) {
				regr.remove(0);
			}

			Acuracia predictor = new Acuracia(regr, examples_val, atrib_orig, target_default);
			double acc_tmp = predictor.getAccuracy();
			if (acc_tmp < acc_val_ref) {
				regr = deepcopy(rules_cloned);
				break;
			} else {
				for (Regra r : regr) {
					r.assignScore(examples_train, atrib_orig);
				}
				if (!rule_max.isEmpty()){
					rule_max.setScore(10);
				}
				
				if(acc_val_ref < acc_tmp)acc_val_ref = acc_tmp;
				continue;
			}

		} while (true);
		
		
		double accuracy_prunedRuleset = .0;
		do {

			
			ArrayList<Regra> rules_backup = deepcopy(regr);
			for (int i = 0; i < regr.size(); i++){
				Regra rule_candidate = regr.get(i);
				ArrayList<Regra> rules_cloned = deepcopy(regr);
				
				cut(rule_candidate);
				if (rule_candidate.isEmpty()) {
					rule_candidate.setScore(0.0);
					regr.remove(0);
					i--;
				}
				Acuracia predictor = new Acuracia(regr, examples_val, atrib_orig, target_default);
				double acc_tmp = predictor.getAccuracy();
				
				if (acc_tmp < rule_candidate.getScore()) {
					regr = deepcopy(rules_cloned);
					regr.get(i).setScore(acc_tmp);
				}else {
					for (Regra r : regr) {
						r.assignScore(examples_train, atrib_orig);
					}
					 
					if (!rule_candidate.isEmpty()){
						rule_candidate.setScore(acc_tmp);
					}
					
					if(acc_val_ref < acc_tmp)acc_val_ref = acc_tmp;
				}

			}
			
			regr = sort(regr, -1);
			
			Acuracia predictor = new Acuracia(regr, examples_val, atrib_orig, target_default);
			accuracy_prunedRuleset = predictor.getAccuracy();
			
			if (accuracy_prunedRuleset == 100){
				break;
			}
			if (accuracy_prunedRuleset < acc_val_ref){
				regr = deepcopy (rules_backup);
				break;
			}
			if (acc_val_ref < 60){
				System.out.println();
			}
			
		} while (accuracy_prunedRuleset > acc_val_ref);

		return regr;

	}

	private void printRegras(ArrayList<Regra> regr) {

		for (Regra r : regr) {

			for (int i = 0; i < r.size() - 1; i = i + 2) {

				System.out.print(r.getPreconditions().get(i) + " = ");
				System.out.print(r.getPreconditions().get(i + 1) + " ^ ");

			}
			System.out.printf(" => %s%n", r.getTarget());

		}

	}

	private void cut(Regra rule) {
		if (rule.isEmpty()) {
			rule = null;
			System.out.println();
		} else {
			rule = rule.remove(rule.size() - 1);
			rule = rule.remove(rule.size() - 1);
		}
	}

//	private Arvore rules_to_tree(ArrayList<Regra> rules2) {
//		return null;
//	}

	private ArrayList<Regra> deepcopy(ArrayList<Regra> regr) {
		ArrayList<Regra> rules_cloned = new ArrayList<Regra>();

		for (Regra r : regr) {
			Regra tmp = r.clone();
			rules_cloned.add(tmp);
		}

		return rules_cloned;
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

	public void deriveRules() {
		String raiz = this.getRoot();
		condic.add(raiz); 
		if (this.getChildren() == null) {

			Regra rule = new Regra(condic);

			regr.add(rule);

			signal = true;

			return;
		} else {
			signal = false;
		}

		Iterator<Entry<String, Arvore>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Arvore> t = itr.next();
			if ((condic.size() > 2) && (signal)) {
				condic.remove(condic.size() - 1);
				condic.remove(condic.size() - 1);
			}
			condic.add(t.getKey());
			t.getValue().deriveRules();

		}
		if ((condic.size() > 2) && (signal)) {
			condic.remove(condic.size() - 1);
			condic.remove(condic.size() - 1);
		}

	}

	public ArrayList<Regra> getRules() {
		return this.regr;
	}

	public void setAttribute(String[] args) {
		atrbs = new String[args.length];
		for (int i = 0; i < atrbs.length; i++) {
			atrbs[i] = args[i];
		}
	}

	public String[] getAttribute() {
		return atrbs;
	}

	private String replaceAdult(int i) {

		String output = "noun";

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

		return output;
	}
	
	public void assignParents(Arvore p) {
		if (this.getChildren() == null) {
			this.setParent(p);

			return;
		}

		Arvore tmp = this;
		this.setParent(p);
		Iterator<Entry<String, Arvore>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			itr.next().getValue().assignParents(this);

		}

	}

	public void resetRules() {
		buffer = new ArrayList<String>();
		regr = new ArrayList<Regra>();
		condic = new ArrayList<String>();
		
	}
	
	public int displayCount(int spaces) throws CloneNotSupportedException {
		totalNodes=0;
		this.countNode(spaces);
		return totalNodes;
	}
	public int countNode(int spaces) throws CloneNotSupportedException {
		Arvore tmp = new Arvore();
		tmp = this.clone();
		Iterator<Entry<String, Arvore>> itr;

		if (tmp.getChildren() == null) {
			
			return spaces;
		} else {
			totalNodes++;
			itr = tmp.getChildren().entrySet().iterator();
		}
		while (itr.hasNext()) {

			Entry<String, Arvore> t = itr.next();
			tmp = t.getValue();

			if (tmp.getChildren() != null) {
				spaces += getShiftSpace();
			}

			spaces = tmp.countNode(spaces);
		}
		return spaces - getShiftSpace();
	}
}
