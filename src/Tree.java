import java.util.*;
import java.util.Map.Entry;
/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 22, 2017
 * 
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 * 
 */
public class Tree {

	private Tree parent;
	private String root;
	private HashMap<String, Tree> children;
	private final int shift_disp = 10;
	private static ArrayList<String> buffer = new ArrayList<String>();
	private static ArrayList<String> preconditions = new ArrayList<String>();
	private static ArrayList<Rule> rules = new ArrayList<Rule>();
	private static boolean signal = false;
	private String approach; // "reduced-error pruning" or "rule post-pruning"
	private static String[] attrs;
	private static Tree mirror_tree = new Tree();
	private static boolean stop = false;
	private static boolean up = false;
	private static boolean continuepruning = false;
	private static String cutting_attr;
	private static int counter = 0;

	public Tree() {
		// set an approach default to "reduced-error pruning"
		this.setPruneApproach("reduced-error pruning");
	}

	// accessors and mutators
	public void setRoot(String r) {
		this.root = r;
	}

	public String getRoot() {
		return this.root;
	}

	@SuppressWarnings("unchecked")
	public void setChildren(HashMap<String, Tree> argin) {
		if (argin != null) {
			this.children = (HashMap<String, Tree>) argin.clone();
		} else {
			this.children = null;
		}
	}

	public HashMap<String, Tree> getChildren() {
		return this.children;
	}

	public void updateChildren(String k, Tree v) {
		this.children.put(k, v);

	}

	public void setParent(Tree p) {
		this.parent = p;
	}

	public Tree getParent() {
		return this.parent;
	}

	public int display(int spaces) throws CloneNotSupportedException {
		// print out this tree
		Tree tmp = new Tree();
		tmp = this.clone();

		Iterator<Entry<String, Tree>> itr;

		if (tmp.getChildren() == null) {
			System.out.printf(" >> %s%n", tmp.getRoot());
			return spaces;
		} else {
			System.out.printf(" %s%n", tmp.getRoot());
			itr = tmp.getChildren().entrySet().iterator();
		}
		while (itr.hasNext()) {

			Entry<String, Tree> t = itr.next();
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

		String root = this.getRoot();
		buffer.add(root); // e.g. outlook
		if (buffer.get(0).equals("false")){
			System.out.println();
		}
		// termination condition
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

		// System.out.printf(" %s ", root);
		Iterator<Entry<String, Tree>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Tree> t = itr.next();
			if ((buffer.size() > 2) && (signal)) {
				buffer.remove(buffer.size() - 1);
				buffer.remove(buffer.size() - 1);
			}
			buffer.add(t.getKey()); // e.g. sunny

			// System.out.printf("%s", t.getKey());

			t.getValue().displayRules();

		}
		if ((buffer.size() > 2) && (signal)) {
			buffer.remove(buffer.size() - 1);
			buffer.remove(buffer.size() - 1);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected Tree clone() {

		Tree tmp = new Tree();

		tmp.setRoot(this.getRoot());
		tmp.setParent(this.getParent());
		if (this.getChildren() != null)
			tmp.setChildren((HashMap<String, Tree>) this.getChildren().clone());

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

	public Tree prune(List<Exp> examples_train, List<Exp> examples_val, ArrayList<Rule> rules, String[] Attributes,
			int[] classes, String name_dataset, String[] attrs_orig, double acc_val_ref)
			throws CloneNotSupportedException {

		Tree pruned_tree;

		pruned_tree = reduced_error_prun(examples_train, examples_val, Attributes, classes, name_dataset, attrs_orig,
				acc_val_ref);
		System.out.printf("%n--------%nTree pruning using Reduced-Error Pruning:%n--------");

		return pruned_tree;
	}

	public ArrayList<Rule> prune_RulePostPruning(List<Exp> examples_train, List<Exp> examples_val, ArrayList<Rule> rules, String[] Attributes,
			int[] classes, String name_dataset, String[] attrs_orig, double acc_val_ref, String target_default, boolean noise)
			throws CloneNotSupportedException {

		ArrayList<Rule> pruned_ruleset;

		if (!noise){
		System.out.printf("%n--------%nRule Post-Pruning:%n--------");}
		pruned_ruleset = rule_post_prune(examples_train, examples_val, rules, Attributes, classes, name_dataset, attrs_orig,
				acc_val_ref, target_default);
		

		return pruned_ruleset;
	}

	private Tree reduced_error_prun(List<Exp> examples_train, List<Exp> examples_val, String[] Attributes,
			int[] classes, String name_dataset, String[] attrs_orig, double acc_val_ref)
			throws CloneNotSupportedException {
		// The default tree pruning approach

		// 1. CONSIDER A CANDIDATE NODE TO CUT OFF TREE FROM. NAME IT "A".
		// String cutting_attr = what_to_cut();
		// cutting_attr = "sepal-width<=2.65"; // change this hard coded
		// part!!!!!

		// 2. CUT THE TREE FROM THE CANDIDATE ATTRIBUTE A
		Tree pruned_tree = this.cut(cutting_attr, examples_train, examples_val, classes, name_dataset, attrs_orig,
				acc_val_ref);

		// 3. EVALUATE THE TREE AFTER PRUNING THE TREE

		// 4. STOP IF THE PRUNING IS GETTING HARMFUL (GAIN IS DECREASING)

		return pruned_tree;
	}

	private String what_to_cut() {
		// index is the index of the particular attribute from which
		// the tree is pruned.
		// This index is computed from the original attribute set.

		// find the index of cutting attr in Attributes.
		int index = 0;
		String cutting_attr = this.getAttribute()[index];

		return cutting_attr;
	}

	public Tree cut(String cutting_attr, List<Exp> examples_train, List<Exp> examples_val, int[] classes,
			String name_dataset, String[] attrs_orig, double acc_val_ref) throws CloneNotSupportedException {
		// create a mirror of this tree
		// return the mirror in case the pruning does not improve the accuracy.
		mirror_tree = this.clone();

		// CUT HERE
		buffer.clear();
		mirror_tree = traverse(mirror_tree, examples_train, examples_val, classes, acc_val_ref, name_dataset,
				attrs_orig);

		// mirror_tree.display(10);

		return mirror_tree;

	}

	private Tree traverse(Tree tree, List<Exp> examples_train, List<Exp> examples_val, int[] classes,
			double acc_val_ref, String name_dataset, String[] attrs_orig) throws CloneNotSupportedException {
		String root = tree.getRoot();
		buffer.add(root);

		Iterator<Entry<String, Tree>> itr;
		Tree tmp = null;

		if (tree.getChildren() == null) {
			// you have reached to the end with no observing the cutting
			// attribute
			// go back and try different rule
			return tree;
		} else {

			itr = tree.getChildren().entrySet().iterator();
		}

		// 1st recursion termination condition
		if (up) {
			if (tree.getRoot().equalsIgnoreCase(cutting_attr)) {
				continuepruning = true;
			} else {
				continuepruning = false;
			}
		} else {
			if (cutting_attr == null) {
				while (itr.hasNext()) { // Check if you have reached to a
										// subtree whose branches end to
										// targets.
					if (itr.next().getValue().getChildren() != null) {
						continuepruning = false;
						break;
					}
					continuepruning = true;
				}
			} else {
				continuepruning = false;
			}
		}
		if (continuepruning) {
			Tree t = new Tree(), t2 = new Tree();
			t = tree.clone();
			t2 = tree.clone();
			// calculate the majority vote to assign a substitute for the
			// cut-off target
			int[] counter = new int[classes.length];

			for (int i = 0; i < counter.length; i++) {

				for (Exp exp : examples_train) {
					if (exp.getTarget().equals(replace(i))) {
						counter[i]++;
					}
				}
			}
			// vote for the most (find the maximum)
			int max = 0;
			int sentinel = Integer.MIN_VALUE;
			for (int i = 0; i < counter.length; i++) {
				if (counter[i] > sentinel) {
					sentinel = counter[i];
					max = i;
				}
			}

			t.setRoot(replace(max));
			t.setChildren(null);

			// form the mirror-tree
			this.counter = 0;

			form(t, mirror_tree);

			//mirror_tree.display(10);

			// 3. EVALUATE THE NEWLY PRUNED TREE
			Predictor predictor = new Predictor(mirror_tree, examples_val, attrs, classes, name_dataset, attrs_orig);
			double acc = predictor.getAccuracy();
			if (acc >= acc_val_ref) {
				cutting_attr = t.getParent().getRoot();
				up = true;

				// clone the pruned tree to the original tree

			} else {
				// cutting_attr = null;
				up = false;
				this.counter = 0;
				unform(t2, mirror_tree);

				// mirror_tree.display(10);
			}

			return t;
		}

		// 2nd recursion termination condition
		if (tree.getChildren() == null) {
			// you have reached to the end with no observing the cutting
			// attribute
			// go back and try different rule
			return tree;
		} else {

			itr = tree.getChildren().entrySet().iterator();
		}

		while (itr.hasNext()) {

			if (up) {
				break;
			}
			Entry<String, Tree> t = itr.next();

			tmp = t.getValue();
			buffer.add(t.getKey());
			tmp = traverse(tmp, examples_train, examples_val, classes, acc_val_ref, name_dataset, attrs_orig);
			if (buffer.size() > 1) {
				buffer.remove(buffer.size() - 1);
				buffer.remove(buffer.size() - 1);
			}

			if (up) {
				up = false;
				buffer.clear();
				tmp = traverse(mirror_tree, examples_train, examples_val, classes, acc_val_ref, name_dataset,
						attrs_orig);
			}

		}

		return mirror_tree;
	}

	private void unform(Tree t, Tree ref_tree) throws CloneNotSupportedException {

		String next = buffer.get(counter);
		// termination condition
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
				Tree tmp = ref_tree.getChildren().get(next);
				counter++;
				unform(t, tmp);
				if (stop) {
					break;
				}
			}

		}

	}

	private void form(Tree t, Tree ref_tree) throws CloneNotSupportedException {
		// track the mirror_tree using buffer and substitute according to t

		String next = buffer.get(counter);
		// termination condition
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
				Tree tmp = ref_tree.getChildren().get(next);
				counter++;
				form(t, tmp);
				if (stop) {
					break;
				}
			}

		}

	}

	private ArrayList<Rule> rule_post_prune(List<Exp> examples_train, List<Exp> examples_val, ArrayList<Rule> rules, String[] Attributes,
			int[] classes, String name_dataset, String[] attrs_orig, double acc_val_ref, String target_default) {
		// The rule post-pruning approach

		/*// Go through a loop over the set of all rules
		do {

			// 0. FOR EVERY ITERATION BEFORE PRUNING KEEP A COPY OF UNPRUNED
			// RULES IN CASE TO UNDO
			ArrayList<Rule> rules_cloned = deepcopy(rules);

			// SORT RULES BASED ON THEIR SCORES IN AN (e.g. ASCENDING) ORDER
			rules = sort(rules, 1);// -1/1 descending or ascending order
			
			//printRules(rules);
			
			// PICK THE RULE WITH THE LOWEST SCORE OR ACCURACY (HIGHEST ERROR)
			Rule rule_max = rules.get(0);

			// PRUNE THE RULE BY REMOVING A PRECONDITION (FROM THE RIGHTMOST)
			cut(rule_max); // rule set automatically will be modified as well
			// if the rule has no more attributes remove it from the rule set
			if (rule_max.isEmpty()) {
				rules.remove(0);
			}

			// FOR EACH RULE CALCULATE ITS SCORE AGAIST ALL EXAMPLES IN
			// VALIDATION SET
			// ONCE YOU PRUNED CALCULATE THE ACCURACY IMMEDIATELY
			Predictor predictor = new Predictor(rules, examples_val, attrs_orig, target_default);
			double acc_tmp = predictor.getAccuracy();
			//System.out.printf("%nThe accuracy of pruned tree over val. set is %.1f%%%n", predictor.getAccuracy());

			// CHECK WHETHER THIS PRUNING IMPROVED THE RULE'S ACCURACY OVER THE
			// HOLD-OUT SET
			// Termination Condition STOP PRUNING WHEN THE ACCURACY IS WORSENED
			// OR NOT IMPROVING
			if (acc_tmp < acc_val_ref) {
				// No good pruning! UNDO!
				rules = deepcopy(rules_cloned);
				break;
			} else {
				// Good pruning! Go ahead!
				// RESET EACH RULE'S SCORE
				for (Rule r : rules) {
					r.assignScore(examples_train, attrs_orig);
				}
				if (!rule_max.isEmpty()){
					rule_max.setScore(10);
				}
				
				
				
				
				acc_val_ref = acc_tmp;
				continue;
			}

		} while (true);*/
		
		
		double accuracy_prunedRuleset = .0;
		// Go through a loop over the set of all rules
		do {

			
			ArrayList<Rule> rules_backup = deepcopy(rules);
			for (int i = 0; i < rules.size(); i++){
				Rule rule_candidate = rules.get(i);
				// 0. FOR EVERY ITERATION BEFORE PRUNING KEEP A COPY OF UNPRUNED
				// RULES IN CASE TO UNDO
				ArrayList<Rule> rules_cloned = deepcopy(rules);
				
				// 1. PRUNE THE RULE BY REMOVING A PRECONDITION (FROM THE RIGHTMOST)
				cut(rule_candidate);
				// if the rule has no more attributes remove it from the rule set
				if (rule_candidate.isEmpty()) { //CHECK THIS PART!!!!!!!!!!!
					rule_candidate.setScore(0.0);
					rules.remove(0);
					i--;
				}
				
				// FOR EACH RULE CALCULATE ITS SCORE AGAIST ALL EXAMPLES IN
				// VALIDATION SET
				// ONCE YOU PRUNED CALCULATE THE ACCURACY IMMEDIATELY
				Predictor predictor = new Predictor(rules, examples_val, attrs_orig, target_default);
				double acc_tmp = predictor.getAccuracy();
				
				// CHECK WHETHER THIS PRUNING IMPROVED THE RULE'S ACCURACY OVER THE
				// HOLD-OUT SET
				if (acc_tmp < rule_candidate.getScore()) {
					// No good pruning! UNDO!
					rules = deepcopy(rules_cloned);
					rules.get(i).setScore(acc_tmp);
				}else {
					// Good pruning! Go ahead!
					// RESET EACH RULE'S SCORE
					/*for (Rule r : rules) {
						r.assignScore(examples_train, attrs_orig);
					}*/
					 
					// UPDATE THE ESTIMATE ACCURACY OF THE RULE IF IT'S NOT BEEN ELIMINATED
					if (!rule_candidate.isEmpty()){
						rule_candidate.setScore(acc_tmp);
					}

					//acc_val_ref = acc_tmp;
				}

			}
			
			// SORT RULE SET GIVEN THEIR ESTIMATED ACCURACIES IN AN (e.g. ASCENDING) ORDER
			rules = sort(rules, -1);
			
			// ESTIMATE THE ACCURACY OF THE RULE SET BASED ON THE PRUNED RULE SET
			Predictor predictor = new Predictor(rules, examples_val, attrs_orig, target_default);
			accuracy_prunedRuleset = predictor.getAccuracy();
			
			// TERMINATION CONDITION (if the accuracy is decreasing)
			if (accuracy_prunedRuleset == 100){
				break;
			}
			if (accuracy_prunedRuleset < acc_val_ref){
				rules = deepcopy (rules_backup);
				break;
			}
			if (acc_val_ref < 60){
				System.out.println();
			}
			
		} while (accuracy_prunedRuleset > acc_val_ref);

		return rules;

	}

	private void printRules(ArrayList<Rule> rules) {

		for (Rule r : rules) {

			for (int i = 0; i < r.size() - 1; i = i + 2) {

				System.out.print(r.getPreconditions().get(i) + " = ");
				System.out.print(r.getPreconditions().get(i + 1) + " ^ ");

			}
			System.out.printf(" => %s%n", r.getTarget());

		}

	}

	private void cut(Rule rule) {
		// cut off the rightmost element in this rule
		if (rule.isEmpty()) {
			rule = null;
			System.out.println();
		} else {
			rule = rule.remove(rule.size() - 1);
			rule = rule.remove(rule.size() - 1);
		}
	}

	private Tree rules_to_tree(ArrayList<Rule> rules2) {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Rule> deepcopy(ArrayList<Rule> rules) {
		ArrayList<Rule> rules_cloned = new ArrayList<Rule>();

		for (Rule r : rules) {
			Rule tmp = r.clone();
			rules_cloned.add(tmp);
		}

		return rules_cloned;
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

	public void deriveRules() {
		// derive rules from the current tree
		String root = this.getRoot();
		preconditions.add(root); // e.g. outlook
		

		// termination condition
		if (this.getChildren() == null) {

			// record a new rule
			Rule rule = new Rule(preconditions);

			rules.add(rule);

			signal = true;

			return;
		} else {
			signal = false;
		}

		// System.out.printf(" %s ", root);
		Iterator<Entry<String, Tree>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Tree> t = itr.next();
			if ((preconditions.size() > 2) && (signal)) {
				preconditions.remove(preconditions.size() - 1);
				preconditions.remove(preconditions.size() - 1);
			}
			preconditions.add(t.getKey()); // e.g. sunny

			// System.out.printf("%s", t.getKey());

			t.getValue().deriveRules();

		}
		if ((preconditions.size() > 2) && (signal)) {
			preconditions.remove(preconditions.size() - 1);
			preconditions.remove(preconditions.size() - 1);
		}

	}

	public ArrayList<Rule> getRules() {
		return this.rules;
	}

	public void setAttribute(String[] args) {
		attrs = new String[args.length];
		for (int i = 0; i < attrs.length; i++) {
			attrs[i] = args[i];
		}
	}

	public String[] getAttribute() {
		return attrs;
	}

	private String replace(int i) {
		// 0 is replaced with Yes or Iris-setosa
		// 1 is replaced with No or Iris-versicolor
		// 2 is replaced with Iris-virginica

		String output = "noun";

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

		return output;
	}

	public void assignParents(Tree p) {
		// termination condition
		if (this.getChildren() == null) {
			this.setParent(p);

			return;
		}

		// System.out.printf(" %s ", root);
		Tree tmp = this;
		this.setParent(p);
		Iterator<Entry<String, Tree>> itr = this.getChildren().entrySet().iterator();
		while (itr.hasNext()) {
			itr.next().getValue().assignParents(this);

		}

	}

	public void resetRules() {
		buffer = new ArrayList<String>();
		rules = new ArrayList<Rule>();
		preconditions = new ArrayList<String>();
		
	}

}
