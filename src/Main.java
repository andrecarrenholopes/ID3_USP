import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 22, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 */
public class Main {


	static int[] classes;
	// classes in tennis: 0/1 = yes/no; whereas in iris: 0/1/2 = setosa versicolor virginica
	static HashMap<String, ArrayList<String>> attr_vals = new HashMap<String, ArrayList<String>>();
	private static int attrs_size = 4;
	static String[] attrs; // Tennis has 4 attrs while Iris needs to be preprocessed.
	static String[] attrs_orig = new String[4];
	private static double ratio = 1; // e.g. 90% goes as training, the rest as val. set
	private static int max_noise_limit = 20; // 20% given by the problem
	static double seed = .0;

	public static void main(String[] args) throws FileNotFoundException, IOException, CloneNotSupportedException {

		// variable definitions
		ArrayList<String> input_args = new ArrayList<String>();
		String name_dataset; boolean noise =false, do_prune = false;
		for (String s: args){input_args.add(s);}
		switch (input_args.size()){
		case 1: {name_dataset = input_args.get(0); break;} // iris
		case 2: {name_dataset = input_args.get(0); noise = true; break;}
		case 3: {name_dataset = input_args.get(0); do_prune= true; 
		ratio = Double.valueOf(input_args.get(2)); break;}
		case 4: {name_dataset = input_args.get(0); noise = true; do_prune= true; 
		ratio = Double.valueOf(input_args.get(3)); break;}
		default: {
			name_dataset = input_args.get(0);
		}
		}

		Random generator = new Random((long) seed);

		String txt_attrs = "", txt_input_train = "", txt_input_test;
		List<Exp> examples_train = new ArrayList<Exp>();
		List<Exp> examples_val = new ArrayList<Exp>();
		List<Exp> examples_test = new ArrayList<Exp>();
		int spaces = 10;
		Tree tree = new Tree();

		// 1. PRE-PROCESS
		if (name_dataset.equalsIgnoreCase("tennis")){
			System.out.printf("Tennis Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/tennis-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/tennis-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/tennis-test.txt");

			// a) read from input text file for the tennis dataset
			setAttrs_size(4);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			examples_test = readExamples(txt_input_test);

		}else if (name_dataset.equalsIgnoreCase("iris")){
			System.out.printf("Iris Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/iris-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/iris-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/iris-test.txt");

			// b) read from input text file for the iris dataset
			setAttrs_size(4);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);

			// split training data into training and validation sets
			Pair<List<Exp>, List<Exp>> train_val = split_into_train_val(examples_train, getRatio());
			examples_train = train_val.getfirst();
			examples_val = train_val.getsecond();

			examples_test = readExamples(txt_input_test);
			attrs_orig = attrs.clone();

			// pre-process iris dataset to discretize the attributes
			preprocess_Iris(examples_train);


			System.out.println();
		}else if (name_dataset.equalsIgnoreCase("bool")){
			System.out.printf("Boolean Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/bool-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/bool-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/bool-test.txt");

			// a) read from input text file for the tennis dataset
			setAttrs_size(8);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			examples_test = readExamples(txt_input_test);

		}else if (name_dataset.equalsIgnoreCase("enjoy")){
			System.out.printf("Enjoy (problem 3.4) Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/enjoy-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/enjoy-train.txt");
			//txt_input_test = System.getProperty("user.dir").concat("/enjoy-test.txt");
			txt_input_test = System.getProperty("user.dir").concat("/enjoy-train.txt");

			// a) read from input text file for the tennis dataset
			setAttrs_size(6);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			examples_test = readExamples(txt_input_test);
		}else if (name_dataset.equalsIgnoreCase("adult")){
			System.out.printf("Adult Data Set: %n-------------%n");
			txt_attrs = System.getProperty("user.dir").concat("/adult-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/adult-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/adult-test.txt");

			// b) read from input text file for the iris dataset
			setAttrs_size(14);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);

			// split training data into training and validation sets
			Pair<List<Exp>, List<Exp>> train_val = split_into_train_val(examples_train, getRatio());
			examples_train = train_val.getfirst();
			examples_val = train_val.getsecond();

			examples_test = readExamples(txt_input_test);
			attrs_orig = attrs.clone();

			// pre-process iris dataset to discretize the attributes
			preprocess_Adult(examples_train);


			System.out.println();
		}

		// 2. LEARNER
		//printExamples(examples_train);
		Learner learner = new Learner(examples_train, attrs, attr_vals, classes, name_dataset, attrs_orig);
		tree = learner.getTree();


		// 3. PRINT the result tree
		tree.display(spaces);


		// 4. PRINT THE RULES
		System.out.printf("-------------%nRule Set:%n");
		tree.displayRules();

		// 5. CLASSIFIER/ PREDICTOR (test over test data)
		System.out.println("");
		System.out.println("Limpando missing values de teste...");
		examples_test = cleanMissingValues(examples_test);
		System.out.printf("-------------%nEvaluation:");
		Predictor predictor = new Predictor (tree, examples_test, attrs, classes, name_dataset, attrs_orig);
		System.out.printf("%nThe accuracy over test data is %.1f%%%n" , predictor.getAccuracy());

		System.out.println("Limpando missing values de train data");
		examples_train = cleanMissingValues(examples_train);
		predictor = new Predictor (tree, examples_train, attrs, classes, name_dataset, attrs_orig);
		System.out.printf("The accuracy over train data is %.1f%%%n%n" , predictor.getAccuracy());


		if (do_prune && !noise){
			if (name_dataset.equals("iris")){

				// 6. PRUN THE TREE

				// 6-0. show the performance of the un-pruned tree on the validation set
				predictor = new Predictor (tree, examples_val, attrs, classes, name_dataset, attrs_orig);
				//System.out.printf("The accuracy over validation data is %.1f%%%n" , predictor.getAccuracy());

				// The accuracy of un-pruned tree over validation dataset 
				double acc_val = predictor.getAccuracy();



				/*
				// 6-1. TREE PRUNING (REDUCED-ERROR PRUNING)
				// set the pruning approach to reduced-error pruning
				tree.setPruneApproach("reduced-error pruning");
				Tree tree_pruned = tree.prune(examples_train, examples_val, null, attrs, classes, name_dataset, attrs_orig, acc_val);

				predictor = new Predictor (tree_pruned, examples_test, attrs, classes, name_dataset, attrs_orig);
				System.out.printf("%nThe accuracy of pruned tree over test data is %.1f%%%n" , predictor.getAccuracy());

				predictor = new Predictor (tree_pruned, examples_train, attrs, classes, name_dataset, attrs_orig);
				System.out.printf("The accuracy of pruned tree over train data is %.1f%%%n" , predictor.getAccuracy());

				predictor = new Predictor (tree_pruned, examples_val, attrs, classes, name_dataset, attrs_orig);
				System.out.printf("The accuracy of pruned tree over validation set is %.1f%%%n" , predictor.getAccuracy());

				System.out.println("Pruned Tree:");
				tree_pruned.display(spaces);
				 */



				// 6-2. RULE POST PRUNING
				// set the pruning approach to rule post-pruning
				tree.setPruneApproach("rule post-pruning");

				tree.resetRules();
				tree.deriveRules();
				ArrayList<Rule> rules = tree.getRules();
				// initialize rules' scores to accuracy over validation set
				for (Rule r: rules){
					//r.assignScore(examples_val, attrs_orig);
					r.setScore(acc_val);
					//r.setScore(acc_val + Math.random());
				}

				// Compute the majority of class targets for the default target value
				String target_default = getMajority(examples_train);
				//tree.displayRules();
				ArrayList<Rule> pruned_rules = tree.prune_RulePostPruning(examples_train, examples_val, rules, attrs, classes, name_dataset, attrs_orig, acc_val, target_default, noise);

				predictor = new Predictor(pruned_rules, examples_test, attrs_orig, target_default);
				System.out.printf("%nThe accuracy of pruned tree over test data is %.1f%%%n" , predictor.getAccuracy());

				predictor = new Predictor(pruned_rules, examples_train, attrs_orig, target_default);
				System.out.printf("The accuracy of pruned tree over train data is %.1f%%%n" , predictor.getAccuracy());

				predictor = new Predictor(pruned_rules, examples_val, attrs_orig, target_default);
				System.out.printf("The accuracy of pruned tree over validation data is %.1f%%%n" , predictor.getAccuracy());

				System.out.println("Pruned Rule Set:");
				printRules(pruned_rules);

			}

		}


		if (noise) {
			// CORRPUT THE TRAIN DATA AND TEST IT WITHOUT/WITH PRUNING (RULE
			// POST-PRUNING)
			if (name_dataset.equals("iris")) {

				double acc_val = .0;
				ArrayList<Integer> list = getShuffledList(examples_train);
				List<Exp> examples_train_cloned = new ArrayList<Exp>();
				for (int i = 0; i < examples_train.size(); i++) {
					Exp e = new Exp();
					e = examples_train.get(i).clone();
					examples_train_cloned.add(e);
				}

				// INJECT NOISE
				for (int percentage = 0; percentage <= max_noise_limit; percentage = percentage + 2) {
					// 1. CORRPUT THE TRAINING EXAMPLES
					examples_train = corrupt(examples_train, list, percentage);
					System.out.printf("%n%d %% of noise%n", percentage);

					// 2. CREATE THE TREE
					learner = new Learner(examples_train, attrs, attr_vals, classes, name_dataset, attrs_orig);
					tree = learner.getTree();

					// 3. TEST THE TREE WITHOUT PRUNING & EVALUATE
					predictor = new Predictor(tree, examples_test, attrs, classes, name_dataset, attrs_orig);
					System.out.printf("The accuracy of the tree over test data is %.1f%%", predictor.getAccuracy());


					// tree.resetRules();
					// tree.displayRules();
					// System.out.println();

					if (do_prune) {
						// 4. DERIVE THE RULE SET
						tree.resetRules();
						tree.deriveRules();
						ArrayList<Rule> rules = tree.getRules();

						// 5. TEST THE TREE WITH PRUNING

						// Compute the majority of class targets for the default
						// target value
						String target_default = getMajority(examples_train);

						//if (percentage == 0) {
						// 7-0. show the performance of the un-pruned tree
						// on the validation set
						predictor = new Predictor(tree, examples_val, attrs, classes, name_dataset, attrs_orig);
						// System.out.printf("The accuracy over validation
						// data is %.1f%%%n" , predictor.getAccuracy());

						// The accuracy of un-pruned tree over validation
						// dataset
						acc_val = predictor.getAccuracy();

						// initialize rules' scores to accuracy over validation
						// set
						for (Rule r : rules) {
							//r.assignScore(examples_train, attrs_orig);
							r.setScore(acc_val);
						}

						//}


						ArrayList<Rule> pruned_rules = tree.prune_RulePostPruning(examples_train, examples_val, rules, attrs, classes,
								name_dataset, attrs_orig, acc_val, target_default, noise);

						Predictor predictor2 = new Predictor(pruned_rules, examples_test, attrs_orig, target_default);
						System.out.printf("%nThe accuracy of pruned tree over test data is %.1f%%%n",
								predictor2.getAccuracy());

						//printRules(pruned_rules);
						System.out.println();
					}
				}
			}
		}

	}

	private static ArrayList<Integer> getShuffledList(List<Exp> examples_train) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for( int i = 0; i < examples_train.size(); i++){
			list.add(i);
		}
		Collections.shuffle(list);

		return list;
	}

	private static List<Exp> corrupt(List<Exp> examples, ArrayList<Integer> list, int percentage) {
		// corrupt training examples by "percentage"%
		if (percentage == 0) {return examples;}
		for (int i = percentage - 2; i< percentage; i++) {
			String target_actual = examples.get(list.get(i)).getTarget();
			String target_random = getRandomTarget(target_actual);
			//target_random = "Iris-setosa";

			//System.out.println(target_actual + " " + target_random);
			examples.get(list.get(i)).settarget(target_random);
		}

		return examples;
	}

	private static String getRandomTarget(String target_actual) {
		String output = target_actual;
		while (output.equals(target_actual)){
			double t = Math.random();
			int rnd = (int) (t * classes.length);
			//System.out.println(rnd);
			output = replace(classes[rnd]);
		}

		return output;
	}

	private static void printRules(ArrayList<Rule> rules) {

		for (Rule r : rules) {

			for (int i = 0; i < r.size() - 1; i = i + 2) {

				System.out.print(r.getPreconditions().get(i) + " = ");
				System.out.print(r.getPreconditions().get(i + 1) + " ^ ");

			}
			System.out.printf(" => %s%n", r.getTarget());

		}

	}

	private static void preprocess_Iris(List<Exp> examples) {
		attr_vals.clear();
		ArrayList<String> attrs_tmp = new ArrayList<String>();
		// DISCRETIZE THE CONTINUOUS ATTRIBUTES
		for (int i = 0; i < attrs.length; i++){

			// 1. SORT EXAMPLES w.r.t. EACH ATTRIBUTE A (one at a time) (e.g. sepal-length)
			List<Exp> examples_sorted = sort(examples, i);

			// 2. FIND c THRESHOLDS FOR THE ATTRIBUTE A
			Set<Double> c = find_c(examples_sorted, i);

			// 3. FORM NEW DISCRETE ATTRIBUTES (e.g. sepal-length < 5.0)
			attrs_tmp.addAll(formAttributes(c, i));

			// 4. ADD THE ATTRIBUTE WITH ITS VALUES TO OUR KNOWLEDGE (e.g. attr_vals or attrs)
			//    The values for each discretized attribute A is now true or false 
			updateAttributes(attrs_tmp);

		}
		// 5. ADD ALL NEWLY FORMED ATTRIBUTES TO OUR KNOWLEDGE
		updateAttributes();

		//printExamples(examples);

	}

	private static void preprocess_Adult(List<Exp> examples) {
		//remove os valores continuos de attr_vals para poder inserir eles discretizados
		HashMap<String, ArrayList<String>> attr_vals_temp = new HashMap<String, ArrayList<String>>();
		attr_vals_temp.putAll(attr_vals);
		Iterator it = attr_vals_temp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next(); 
			ArrayList<String> s = (ArrayList) pair.getValue();
			if(s.get(0).equals("continuous")){
				attr_vals.remove(pair.getKey(), pair.getValue());
			}
			it.remove(); // avoids a ConcurrentModificationException
		}

		ArrayList<String> attrs_tmp = new ArrayList<String>();

		//limpar missing values
		examples = cleanMissingValues(examples);
		
		// DISCRETIZE THE CONTINUOUS ATTRIBUTES

		System.out.println("come�ando a ordenar");
		long tempoIni = System.currentTimeMillis();

		for (int i = 0; i < attrs.length; i++){
			if( (i==0) || 
					(i==2) ||
					(i==4) ||
					(i==10) ||
					(i==11) ||
					(i==12) ){
				// 1. SORT EXAMPLES w.r.t. EACH ATTRIBUTE A (one at a time) (e.g. sepal-length)		
				List<Exp> examples_sorted = sort(examples, i);

				// 2. FIND c THRESHOLDS FOR THE ATTRIBUTE A
				Set<Double> c = find_c(examples_sorted, i);

				c = bestThreshold(examples_sorted, i, c);
				// 3. FORM NEW DISCRETE ATTRIBUTES (e.g. sepal-length < 5.0)
				attrs_tmp.addAll(formAttributes(c, i));

				// 4. ADD THE ATTRIBUTE WITH ITS VALUES TO OUR KNOWLEDGE (e.g. attr_vals or attrs)
				//    The values for each discretized attribute A is now true or false 
				updateAttributes(attrs_tmp);
			}
		}

		long tempoFim = System.currentTimeMillis();
		System.out.println("tempo para ordenar: " + (tempoFim-tempoIni)/1000);

		// 5. ADD ALL NEWLY FORMED ATTRIBUTES TO OUR KNOWLEDGE
		updateAttributes();

		//printExamples(examples);

	}


	private static void updateAttributes() {
		Iterator<String> itr = attr_vals.keySet().iterator();
		attrs = new String[attr_vals.size()];
		int i = 0;
		while (itr.hasNext()){
			attrs[i] = itr.next();
			i++;
		}
	}

	private static void updateAttributes(ArrayList<String> attrs_tmp) {

		ArrayList<String> vals;

		for (int i = 0; i < attrs_tmp.size(); i++){
			vals = new ArrayList<String>();
			vals.add("true"); vals.add("false");
			attr_vals.put(attrs_tmp.get(i), vals);

		}

	}

	private static ArrayList<String> formAttributes(Set<Double> c, int i) {
		// takes a set of numbers representing thresholds over i-th attribute of the original data
		// [ 4.3, 5.9,...] over sepal-length

		String attr_label = attrs[i]; // e.g. sepal-length
		String tmp;
		ArrayList<String> attrs_tmp = new ArrayList<String>();

		Iterator<Double> itr = c.iterator();
		while (itr.hasNext()){
			tmp = attr_label;

			tmp = tmp.concat("<=" + (String.valueOf(itr.next())));
			attrs_tmp.add(tmp);
		}

		return attrs_tmp;
	}

	private static Set<Double> find_c(List<Exp> examples, int idx) {
		// pinpoint thresholds on which the target value switches

		Set<Double> c = new HashSet<Double>();
		DecimalFormat dec_form = new DecimalFormat("0.##");

		String sentinel = examples.get(0).getTarget();
		for (int i = 0; i <examples.size(); i++){
			if (!examples.get(i).getTarget().equals(sentinel)){
				double tmp = ((Double.valueOf(examples.get(i - 1).get(idx)) + Double.valueOf(examples.get(i).get(idx))) / 2);

				tmp = Double.valueOf(tmp);//dec_form.format(tmp));

				c.add(tmp);
				sentinel = examples.get(i).getTarget();
				//envia um threshold aleatorio enquanto n�o conseguimos fazer ele enviar o melhor
				//return c;

			}
		}


		return c;
	}



	private static List<Exp> cleanMissingValues(List<Exp> examples) {
		int count = 0;
		System.out.println("Quantidade de exemplos: " + examples.size());
		for (int i = 0; i < examples.size(); i++){
			String [] data = examples.get(i).getData();
			for(int j = 0; j < data.length; j++){
				if(data[j].equals("?")){
					examples.remove(i);
					count++;
					break;
				}
			}
		}
		System.out.println("Quantidade de exemplos removidos: " + count);
		System.out.println("Quantidade de exemplos ap�s a remo��o: " + examples.size());
		return examples;
	}

	private static List<Exp> sort(List<Exp> examples, int a) {
		// sort all examples with respect to attribute A
		for (int i = 0; i < examples.size(); i++){
			if (i < examples.size() - 1){
				for (int j = i + 1; j < examples.size(); j++){
					Double candidate1 = Double.valueOf(examples.get(i).getData()[a]);
					Double candidate2 = Double.valueOf(examples.get(j).getData()[a]);
					//System.out.printf("%s, ", examples.get(i).getData()[a]);
					//System.out.printf("%s%n", examples.get(j).getData()[a]);

					// compare two examples (-1/1 for less/greater than, and 0 for equals to)
					if (candidate1 > candidate2) {

						// swap examples
						examples = swap (examples, i, j);

						//System.out.printf("%s, ", examples.get(i).getData()[a]);
						//System.out.printf("%s%n", examples.get(j).getData()[a]);


					}
				}
			}
		}

		return examples;
	}

	private static String getMajority(List<Exp> examples) {

		int[] counter = new int[classes.length];

		for (Exp exp: examples){
			switch (exp.getTarget()) {
			case "Iris-setosa":
				counter[0]++;
				break;
			case "Iris-versicolor":
				counter[1]++;
				break;
			case "Iris-virginica":
				counter[2]++;
				break;
			default:
				break;
			}
		}

		// find the max
		int max = Integer.MIN_VALUE, idx = 0;
		for (int i = 0; i < counter.length - 1; i++){
			if ( counter[i] >= max){
				max = counter[i];
				idx = i;
			}
		}

		String t = replace(idx);

		return t;
	}

	// auxiliary functions

	private static String replace(int i) {
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

	private static String replaceAdult(int i) {
		// 0 is replaced with Yes or Iris-setosa
		// 1 is replaced with No or Iris-versicolor
		// 2 is replaced with Iris-virginica

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

	private static void printExamples(List<Exp> examples) {
		for (int i = 0; i < examples.size(); i++){
			System.out.printf("%d ", i);
			for (int j = 0; j < 4; j++){
				System.out.printf("%s ", examples.get(i).getData()[j]);
			}
			System.out.printf(": %s%n", examples.get(i).getTarget());
		}

	}

	private static void setAttrs_size(int i) {
		attrs_size = i;

	}

	private static double getRatio(){
		return ratio;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Pair<List<Exp>, List<Exp>> split_into_train_val(List<Exp> examples, double ratio) {
		// split the training data into two sections
		// the hold-out (10%) set and the training (90%) set itself

		int sz = examples.size(); int thr = (int) (sz * ratio);
		List<Exp> train_set = new ArrayList<Exp>();
		List<Exp> validation_set = new ArrayList<Exp>();

		if (thr != 0){
			for (int i = 0; i < thr; i++){
				train_set.add(examples.get(i));
			}

			for (int i = thr; i < sz; i++){
				validation_set.add(examples.get(i));
			}
			Pair<List<Exp>, List<Exp>> output = new Pair(train_set, validation_set);
			return output;
		}else{
			Pair<List<Exp>, List<Exp>> output = new Pair(examples, examples);
			return output;
		}


	}

	private static List<Exp> swap(List<Exp> examples, int i, int j) {
		Exp tmp = new Exp(examples.get(0).getData().length);
		tmp = examples.get(i).clone();
		examples.set(i, examples.get(j).clone());
		examples.set(j, tmp);

		return examples;
	}

	private static List<Exp> readExamples(String filepath) throws FileNotFoundException, IOException {
		// reads input txt file line by line
		// every line contains a row of a value for all possible attributes plus a target class at the end.
		// Exp is a class containing only one example. (line)
		// we have an array of Exp comprises with the entire training dataset.

		List<Exp> examples = new ArrayList<Exp>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();

			while (line != null){

				// create an example
				Exp e = new Exp(line, attrs.length);

				// add an example to the list
				examples.add(e);

				line = reader.readLine();
			}

		}

		return examples;
	}

	private static void readAttributes(String filepath) throws FileNotFoundException, IOException {
		// reads input txt file line by line
		// There are 4 attributes: Outlook, Temperature, Humidity, Wind, plus target PlayTennis

		// Outlook Sunny Overcast Rain
		// Temperature Hot Mild Cool
		// Humidity High Normal
		// Wind Weak Strong

		// PlayTennis Yes No

		attrs = new String[attrs_size];

		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();
			int counter = 0;

			// read the first 4 lines
			while (!line.isEmpty()){

				String[] tmp = line.split(" ");
				ArrayList<String> tmp_vals = new ArrayList<String>();

				for (int i = 1; i < tmp.length; i++){
					tmp_vals.add(tmp[i]);
				}

				attr_vals.put(tmp[0], tmp_vals);

				attrs[counter] = tmp[0];

				counter++;

				line = reader.readLine();
			}

			// now read PlayTennis Yes No
			line = reader.readLine();
			String[] tmp = line.split(" ");
			classes = new int[tmp.length - 1];
			for (int i = 0; i < classes.length; i++){
				classes[i] = i;
			}
			// classes contains 0 or 1 for tennis dataset meanning yes or no
			// classes contains 0, 1, or 2 for iris dataset meaning setosa versicolor virginica

		}

	}

	private static Set<Double> bestThreshold(List<Exp> S, int idx, Set<Double> c) {
		// The entropy for the entire set S before being partitioned
		String[] temp_c = new String[c.size()];
		int i = 0;
		for(Double d: c){
			temp_c[i] = String.valueOf(d);
			i++;
		}

		int[] Target_attributes = {0,1}; // >50K <=50K
		double entropy_S = cal_entropyAdult(S, Target_attributes);

		double S_size = S.size();

		double gain = 0.0, max = -Double.MAX_VALUE;

		String best_attribute = "";

		// Expected value of the entropy after S is partitioned using attr A
		// for each attr A calculate the entropy and subtract it from S's entropy
		//String attr = attrs[idx];
		for (String attr: temp_c){

			double sigma = 0.0;

			//Iterator itr = temp_c.iterator();
			//while (itr.hasNext()){
			i = 0;
			String[] itr = {"true", "false"};
			while(i<itr.length) {

				// 1. count the number of examples whose attribute "attr" has the value v
				String v = itr[i];
				List<Exp> S_v = derive_examplesAdult(S,attrs[idx]+"<="+ attr, v);

				double Sv_size = S_v.size();

				// 2. calculate |S_v| / |S|
				double ratio = Sv_size / S_size;

				// 3. calculate (|S_v| / |S|) * entropy(Sv)
				double entropy_Sv = 0;
				if (ratio == 0){
					entropy_Sv = 0;
				}else{

					entropy_Sv = cal_entropyAdult(S_v, Target_attributes);
				}
				// 4. calculate SIGMA [(|S_v| / |S|) * entropy(Sv)]
				sigma+= - ratio * entropy_Sv;

				i++;
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
		Set<Double> best_threshold = new HashSet<Double>();
		best_threshold.add(Double.valueOf(best_attribute));



		return best_threshold;
	}

	private static double cal_entropyAdult(List<Exp> S, int[] Target_attributes){
		// Measure the impurity in collection S

		// count each target attribute's value
		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Exp exp: S){
				if (exp.getTarget().equals(replaceAdult(i))){
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

	private static List<Exp> derive_examplesAdult(List<Exp> S, String A, String v) {
		// count the number of examples whose attribute "attr" has the value v
		// v is implied in each iteration using iterator

		List<Exp> tmp = new ArrayList<Exp>();

		// find the index of attr in global Attributes.

		int idx;
		if (A.contains("<=")){//se for continuo
			idx = Arrays.asList(attrs_orig).indexOf(A.split("<=")[0]);

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
			idx = Arrays.asList(attrs_orig).indexOf(A);

			for (Exp exp: S){
				if (v.equals(String.valueOf(Double.valueOf(exp.get(idx))))){
					tmp.add(exp);
				}
			}
		}


		return tmp;
	}

	public static void random(List<Exp> Examples, int fold) {
		int r = fold;
		Integer[] arr = new Integer[Examples.size()];
		List<List<Exp>> train = new ArrayList<List<Exp>>(r);
		List<List<Exp>> val = new ArrayList<List<Exp>>(r);

		for (int i = 0; i < arr.length; i++) {
			arr[i] = i;
		}
		Collections.shuffle(Arrays.asList(arr));

		System.out.println(Arrays.toString(arr));
		System.out.println(Examples.size());
		System.out.println(arr[32]);

		int qtd = Examples.size()/r;

		List<Exp> temptrain = new ArrayList<Exp>();
		List<Exp> tempval = new ArrayList<Exp>();

		int linhaTrain =0;
		int linhaVal =0;

		for(int linha =0; linha<=r;linha++) {

			int fimFold = (linha+1)*qtd;
			int inicioFold = fimFold-qtd;

			for(int preenche = 0; preenche <=Examples.size()-1; preenche++) {
				if(preenche < fimFold && preenche >= inicioFold) {
					temptrain.add(linhaTrain,Examples.get(arr[preenche]));
					linhaTrain++;
				}
				else {
					tempval.add(linhaVal,Examples.get(arr[preenche]));
					linhaVal++;
				}
				System.out.println("preenche:"+preenche+" linhaTrain:"+linhaTrain+" linhaVal:"+linhaVal);
			}
			train.add(linha,temptrain);
			val.add(linha,tempval);
			linhaTrain=0;
			linhaVal=0;
		}

		for(int linha =0; linha<=r;linha++) {
			System.out.println(linha);
			for(int preenche = 0; preenche <=Examples.size(); preenche++) {
				System.out.println(Arrays.asList(val.get(linha).get(preenche)));
			}
		}

	}

}
