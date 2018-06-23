import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Main {

	static int[] classes;
	static HashMap<String, ArrayList<String>> atrib_val = new HashMap<String, ArrayList<String>>();
	private static int tamnho_atrib = 4;
	static String[] atrbs; 
	static String[] atrib_orig = new String[4];
	private static double ratio = 1;
	static double seed = .0;
	static int fold_size = 10;
	private static List<Double> acuraciaTreinamento = new ArrayList<Double>();
	private static List<Double> acuraciaValidacao = new ArrayList<Double>();
	private static List<Double> acuraciaTeste = new ArrayList<Double>();

	public static void main(String[] args) throws FileNotFoundException, IOException, CloneNotSupportedException {

		ArrayList<String> input_args = new ArrayList<String>();
		String name_dataset; boolean noise =false, do_prune = false;
		for (String s: args){input_args.add(s);}
		switch (input_args.size()){
		case 1: {name_dataset = input_args.get(0); break;}
		case 2: {name_dataset = input_args.get(0); noise = true; break;}
		case 3: {name_dataset = input_args.get(0); do_prune= true; 
		ratio = Double.valueOf(input_args.get(2)); break;}
		case 4: {name_dataset = input_args.get(0); noise = true; do_prune= true; 
		ratio = Double.valueOf(input_args.get(3)); break;}
		default: {
			name_dataset = input_args.get(0);
		}
		}

		String txt_attrs = "", txt_input_train = "", txt_input_test;
		List<Dados> examples_train = new ArrayList<Dados>();
		List<Dados> examples_train_orig = new ArrayList<Dados>();
		List<Dados> examples_val = new ArrayList<Dados>();
		List<Dados> examples_test = new ArrayList<Dados>();
		int spaces = 10;
		Arvore arvr = new Arvore();

		if (name_dataset.equalsIgnoreCase("tennis")){
			System.out.printf("Arquivo PlayTennis: %n");
			txt_attrs = System.getProperty("user.dir").concat("/tennis-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/tennis-train.txt");
			txt_input_test = System.getProperty("user.dir").concat("/tennis-test.txt");

			setAttrs_size(4);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);
			examples_test = readExamples(txt_input_test);

		}else if (name_dataset.equalsIgnoreCase("adult")){
			System.out.printf("Arquivo Adult: %n");
			txt_attrs = System.getProperty("user.dir").concat("/adult-attr.txt");
			txt_input_train = System.getProperty("user.dir").concat("/adult-train-reduzido.txt");
			txt_input_test = System.getProperty("user.dir").concat("/adult-test.txt");

			setAttrs_size(14);
			readAttributes(txt_attrs);
			examples_train = readExamples(txt_input_train);

			// split training data into training and validation sets
			//Conjunto<List<Dados>, List<Dados>> train_val = split_into_train_val(examples_train, getRatio());
			//examples_train = train_val.getfirst();
			//examples_val = train_val.getsecond();
			System.out.println("Limpando missing values de train data");
			examples_train = cleanMissingValues(examples_train);
			examples_train_orig = examples_train;
			//if(examples_train.size() >0) {
				//System.out.println("Limpando missing values de validação de train data para poda");
				//examples_val = cleanMissingValues(examples_val);
			//}
			//examples_test = readExamples(txt_input_test);
			//System.out.println("Limpando missing values de teste...");
			//examples_test = cleanMissingValues(examples_test);
			atrib_orig = atrbs.clone();

			System.out.println();
		}

		if(name_dataset.equalsIgnoreCase("tennis")) {
			Aprendizado aprend = new Aprendizado(examples_train, atrbs, atrib_val, classes, name_dataset, atrib_orig,examples_test);
			arvr = aprend.getTree();

			arvr.display(spaces);

			System.out.printf("-------------%nRegras: %n");
			arvr.displayRules();
			
			System.out.printf("-------------%nAcuracia: %n");
			Acuracia acuracia = new Acuracia (arvr, examples_test, atrbs, classes, name_dataset, atrib_orig);
			System.out.printf("Acuracia no teste: %.1f%%%n" , acuracia.getAccuracy());

			
			acuracia = new Acuracia (arvr, examples_train, atrbs, classes, name_dataset, atrib_orig);
			System.out.printf("Acuracia no treinamento: %.1f%%%n%n" , acuracia.getAccuracy());

		}else if(name_dataset.equalsIgnoreCase("adult")) {
			preprocess_Adult(examples_train);

			Arvore bestTree = new Arvore();
			double bestAccuracy = 0;
			double mediumAccuracy = 0; 
			List<List<Dados>> folds = new ArrayList<List<Dados>>();
			System.out.println("Iniciando 10-Fold-Cross-Validation");
			folds = createFolds(examples_train); 
			for(int i = 0; i<fold_size; i++){
				List<Dados> tempExamples_train = new ArrayList<Dados>();
				for(int j = 0; j <fold_size; j++) {
					if(i!=j){
						tempExamples_train.addAll(folds.get(j));
					}
				}
				
				examples_test = folds.get(i);
				examples_train = tempExamples_train;
				
				Aprendizado aprend = new Aprendizado(examples_train, atrbs, atrib_val, classes, name_dataset, atrib_orig,examples_test);
				arvr = aprend.getTree();
							
				System.out.printf("-------------%nAcuracia 10-fold:%n");
				Acuracia acuracia = new Acuracia (arvr, examples_train, atrbs, classes, name_dataset, atrib_orig);
				System.out.printf("Acuracia no treinamento: %.1f%%%n" , acuracia.getAccuracy());
				
				acuracia = new Acuracia (arvr, examples_test, atrbs, classes, name_dataset, atrib_orig);
				System.out.printf("Acuracia no teste: %.1f%%%n" , acuracia.getAccuracy());
				
				
				
				if(bestTree == null) {
					bestTree = arvr;
				} else {
					if(bestAccuracy < acuracia.getAccuracy()){
						bestAccuracy = acuracia.getAccuracy();
						bestTree = arvr;
					}
				}
				
				mediumAccuracy = mediumAccuracy + acuracia.getAccuracy();  				
				
			}
			mediumAccuracy = mediumAccuracy/fold_size;  
			System.out.println("Acurácia média no 10-fold: " + mediumAccuracy);

			System.out.println("Fim do 10-Fold-Cross-Validation");
			
			System.out.println("Inicio do holdout para fazer a poda");
			List<List<Dados>> foldsHoldout = new ArrayList<List<Dados>>();
			fold_size = 3;
			foldsHoldout = createFolds(examples_train_orig); 
			examples_train.addAll(foldsHoldout.get(0));
			examples_train.addAll(foldsHoldout.get(1));
			examples_test.addAll(foldsHoldout.get(2));
			
			Conjunto<List<Dados>, List<Dados>> train_val = split_into_train_val(examples_train, getRatio());
			examples_train = train_val.getfirst();
			examples_val = train_val.getsecond();
			
			Aprendizado aprend = new Aprendizado(examples_train, atrbs, atrib_val, classes, name_dataset, atrib_orig,examples_test);
			arvr = aprend.getTree();
		
			System.out.printf("-------------%nAcuracia no Holdout:%n");
			Acuracia acuracia = new Acuracia (arvr, examples_train, atrbs, classes, name_dataset, atrib_orig);
			System.out.printf("Acuracia no treinamento: %.1f%%%n%n" , acuracia.getAccuracy());
			
			acuracia = new Acuracia (arvr, examples_test, atrbs, classes, name_dataset, atrib_orig);
			System.out.printf("Acuracia no teste: %.1f%%%n%n" , acuracia.getAccuracy());			
			//arvr.display(spaces);

			acuracia = new Acuracia (arvr, examples_test, atrbs, classes, name_dataset, atrib_orig);
			System.out.printf("Acuracia no teste: %.1f%%%n%n" , acuracia.getAccuracy());
			
			//System.out.printf("-------------%nRegras:%n");
			//arvr.displayRules();

		}
		System.out.println("Total de nós antes da poda " + arvr.displayCount(spaces));

		if (do_prune && !noise){
			if((name_dataset.equals("adult"))) {
				
				System.out.println("");
				System.out.println("");
				System.out.println("Primeira poda....................");
				System.out.println("Total de nós antes da poda " + arvr.displayCount(spaces));
				int totalDeNosAntesPoda = arvr.displayCount(spaces);
				//acuracia em validation
				Acuracia acuracia = new Acuracia (arvr, examples_val, atrbs, classes, name_dataset, atrib_orig);
				double acuraciaValidation = acuracia.getAccuracy();
				//for(int i = 0; i<35; i++) {
				int numNo = arvr.displayCount(spaces) + 1;
				while((acuraciaValidation <= acuracia.getAccuracy())&&(numNo > arvr.displayCount(spaces))) {
					numNo = arvr.displayCount(spaces);
					acuracia = new Acuracia (arvr, examples_val, atrbs, classes, name_dataset, atrib_orig);
					arvr = podaArvoreAdult(arvr, examples_val, name_dataset, examples_train, examples_test, spaces);
					//System.out.print("Total de nós depois da poda" + arvr.displayCount(spaces));
					System.out.print(arvr.displayCount(spaces) + " ");
					if(numNo%100 ==0){
						System.out.println(" ");
					}
					//System.out.println("Total de nós " + arvr.displayCount(spaces));
					acuraciaValidation = acuracia.getAccuracy();
					
				}
				
				if(acuraciaTreinamento.size()==acuraciaTeste.size() && acuraciaTeste.size()==acuraciaValidacao.size() && acuraciaValidacao.size()>0) {
					//Iterator<Double> itr = acuraciaTreinamento.iterator();
					BufferedWriter writer = null;
					try {
						File logFile = new File("logs_acuracia" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".csv");
						//System.out.println(logFile.getCanonicalPath());
						writer = new BufferedWriter(new FileWriter(logFile));
						writer.write("Total de Nós; Treinamento;Teste;Validação");
						writer.newLine();
						for(int i = 0; i < acuraciaTreinamento.size(); i++){
							writer.write(totalDeNosAntesPoda-- + ";" + acuraciaTreinamento.get(i) + ";" + acuraciaTeste.get(i) + ";" + acuraciaValidacao.get(i)+ "");
							writer.newLine();
						}
					} catch (Exception e) {
			            e.printStackTrace();
			        } finally {
			            try {
			                // Close the writer regardless of what happens...
			                writer.close();
			            } catch (Exception e) {
			            }
			        }
				}
				System.out.println("Fim da poda...........");
				
				//printar as regras e suas acuracias
				arvr.resetRules();
				arvr.deriveRules();
				ArrayList<Regra> regr = arvr.getRules();
				
				// initialize regr' scores to accuracy over validation set
				for (Regra r: regr){
					r.assignScore(examples_test, atrib_orig);
//					r.setScore(acc_val);
//					r.setScore(acc_val + Math.random());
//					System.out.println(r.getScore()); 
				}
				
				printRegras(arvr.getRules());
				
			}
		}
	}
	
	private static List<List<Dados>> createFolds(List<Dados> examples_train) {
		
		List<Dados> examplesTemp = new ArrayList<Dados>(); 
		examplesTemp.addAll(examples_train);
		long seed = System.nanoTime();
		Collections.shuffle(examplesTemp, new Random(seed));
		
		List<List<Dados>> returningFolds = new ArrayList<List<Dados>>();
		for(int i = 0; i < examples_train.size(); i++) {
			List<Dados> temp = new ArrayList<Dados>();
			if(returningFolds.size()<fold_size){
				temp.add(examplesTemp.get(i));
				returningFolds.add(temp);
			}else {
				returningFolds.get(i%fold_size).add(examplesTemp.get(i));
			}
		}
		
		return returningFolds;
		
	}

	
	private static Arvore podaArvoreAdult(Arvore arvr, List<Dados> examples_val, String name_dataset, List<Dados> examples_train, List<Dados> examples_test, int spaces) throws CloneNotSupportedException{
		boolean noise = false;
		Acuracia acuracia = new Acuracia (arvr, examples_val, atrbs, classes, name_dataset, atrib_orig);
		//System.out.printf("Acuracia na validacao %.1f%%%n" , acuracia.getAccuracy());

		double acc_val = acuracia.getAccuracy();

		arvr.setPruneApproach("reduced-error pruning");
		Arvore tree_pruned = arvr.prune(examples_train, examples_val, null, atrbs, classes, name_dataset, atrib_orig, acc_val);
		arvr = tree_pruned;
		acuracia = new Acuracia (tree_pruned, examples_test, atrbs, classes, name_dataset, atrib_orig);
		//System.out.printf("Poda | Acuracia no teste: %.1f%%%n" , acuracia.getAccuracy());
		acuraciaTeste.add(acuracia.getAccuracy());
		acuracia = new Acuracia (tree_pruned, examples_train, atrbs, classes, name_dataset, atrib_orig);
		//System.out.printf("Poda | Acuracia no treinamento: %.1f%%%n" , acuracia.getAccuracy());
		acuraciaTreinamento.add(acuracia.getAccuracy());
		acuracia = new Acuracia (tree_pruned, examples_val, atrbs, classes, name_dataset, atrib_orig);
		//System.out.printf("Poda | Acuracia na validacao: %.1f%%%n" , acuracia.getAccuracy());
		acuraciaValidacao.add(acuracia.getAccuracy());
		//System.out.println("Poda:");
		//tree_pruned.display(spaces);

		
		// 6-2. RULE POST PRUNING
		// set the pruning approach to rule post-pruning
		
		//arvr.setPruneApproach("rule post-pruning");
		 

		//arvr.resetRules();
		//arvr.deriveRules();
		//ArrayList<Regra> regr = arvr.getRules();
		/*
		// initialize regr' scores to accuracy over validation set
		for (Regra r: regr){
			r.assignScore(examples_test, atrib_orig);
//			r.setScore(acc_val);
//			r.setScore(acc_val + Math.random());
//			System.out.println(r.getScore()); 
		}

		// Compute the majority of class targets for the default target value
		String target_default = majoraAdult(examples_train);
		//arvr.displayRules();
		
		ArrayList<Regra> pruned_rules = arvr.prune_RulePostPruning(examples_train, examples_val, regr, atrbs, classes, name_dataset, atrib_orig, acc_val, target_default, noise);

		acuracia = new Acuracia(pruned_rules, examples_test, atrib_orig, target_default);
		System.out.printf("%nThe accuracy of pruned arvr over test data is %.1f%%%n" , acuracia.getAccuracy());

		acuracia = new Acuracia(pruned_rules, examples_train, atrib_orig, target_default);
		System.out.printf("The accuracy of pruned arvr over train data is %.1f%%%n" , acuracia.getAccuracy());

		acuracia = new Acuracia(pruned_rules, examples_val, atrib_orig, target_default);
		System.out.printf("The accuracy of pruned arvr over validation data is %.1f%%%n" , acuracia.getAccuracy());

		System.out.println("Pruned Regra Set:");
		//printRegras(pruned_rules);
		*/
		//printRegras(arvr.getRules());
		return arvr;
	}
	
	
	private static void printRegras(ArrayList<Regra> regr) {
		BufferedWriter writer = null;
		try {
			File logFile = new File("logs_regras" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".csv");
			//System.out.println(logFile.getCanonicalPath());
			writer = new BufferedWriter(new FileWriter(logFile));
			//writer.write("Total de Nós; Treinamento;Teste;Validação");
			//writer.newLine();
			for (Regra r : regr) {
				writer.write(r.getScore() + ";" + r.getNum_matched_exp_global() + ";");
				for (int i = 0; i < r.size() - 1; i = i + 2) {
	
					//System.out.print(r.getPreconditions().get(i) + " = ");
					//System.out.print(r.getPreconditions().get(i + 1) + " ^ ");
					writer.write(r.getPreconditions().get(i) + " = ");
					writer.write(r.getPreconditions().get(i + 1) + " ^ ");
	
				}
				//System.out.printf(" => %s%n", r.getTarget());
				writer.write(" => " + r.getTarget());
				writer.newLine();
			}
		}catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}



	private static void preprocess_Adult(List<Dados> examples) {
		//remove os valores continuos de atrib_val para poder inserir eles discretizados
		HashMap<String, ArrayList<String>> attr_vals_temp = new HashMap<String, ArrayList<String>>();
		attr_vals_temp.putAll(atrib_val);
		Iterator it = attr_vals_temp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next(); 
			ArrayList<String> s = (ArrayList) pair.getValue();
			if(s.get(0).equals("continuous")){
				atrib_val.remove(pair.getKey());//, pair.getValue());
			}
			it.remove(); // avoids a ConcurrentModificationException
		}

		ArrayList<String> attrs_tmp = new ArrayList<String>();

		//limpar missing values
		//examples = cleanMissingValues(examples);
		
		// DISCRETIZE THE CONTINUOUS ATTRIBUTES

		System.out.println("começando a ordenar");
		long tempoIni = System.currentTimeMillis();

		for (int i = 0; i < atrbs.length; i++){
			if( (i==0) || 
					(i==2) ||
					(i==4) ||
					(i==10) ||
					(i==11) ||
					(i==12) ){	
				List<Dados> examples_sorted = sort(examples, i);

				Set<Double> c = find_c(examples_sorted, i);

				c = bestThreshold(examples_sorted, i, c);
				attrs_tmp.addAll(formAttributes(c, i));

				updateAttributes(attrs_tmp);
			}
		}

		long tempoFim = System.currentTimeMillis();
		System.out.println("tempo para ordenar: " + (tempoFim-tempoIni)/1000);

		updateAttributes();

	}


	private static void updateAttributes() {
		Iterator<String> itr = atrib_val.keySet().iterator();
		atrbs = new String[atrib_val.size()];
		int i = 0;
		while (itr.hasNext()){
			atrbs[i] = itr.next();
			i++;
		}
	}

	private static void updateAttributes(ArrayList<String> attrs_tmp) {

		ArrayList<String> vals;

		for (int i = 0; i < attrs_tmp.size(); i++){
			vals = new ArrayList<String>();
			vals.add("true"); vals.add("false");
			atrib_val.put(attrs_tmp.get(i), vals);

		}

	}

	private static ArrayList<String> formAttributes(Set<Double> c, int i) {
		String attr_label = atrbs[i]; 
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

	private static Set<Double> find_c(List<Dados> examples, int idx) {
		Set<Double> c = new HashSet<Double>();

		String sentinel = examples.get(0).getTarget();
		for (int i = 0; i <examples.size(); i++){
			if (!examples.get(i).getTarget().equals(sentinel)){
				double tmp = ((Double.valueOf(examples.get(i - 1).get(idx)) + Double.valueOf(examples.get(i).get(idx))) / 2);

				tmp = Double.valueOf(tmp);

				c.add(tmp);
				sentinel = examples.get(i).getTarget();
			}
		}
		return c;
	}


	private static List<Dados> cleanMissingValues(List<Dados> examples) {
		int count = 0;
		System.out.println("Quantidade de exemplos: " + examples.size());
		Iterator<Dados> itr = examples.iterator();
		while(itr.hasNext()){
			String [] data = itr.next().getData();
			for(int j = 0; j < data.length; j++){
				if(data[j].equals("?")){
					itr.remove();
					count++;
					break;
				}
			}
		}
		System.out.println("Quantidade de exemplos removidos: " + count);
		System.out.println("Quantidade de exemplos após a remoção: " + examples.size());
		return examples;
	}

	private static List<Dados> sort(List<Dados> examples, int a) {
		for (int i = 0; i < examples.size(); i++){
			if (i < examples.size() - 1){
				for (int j = i + 1; j < examples.size(); j++){
					Double candidate1 = Double.valueOf(examples.get(i).getData()[a]);
					Double candidate2 = Double.valueOf(examples.get(j).getData()[a]);

					if (candidate1 > candidate2) {

						examples = swap (examples, i, j);

					}
				}
			}
		}

		return examples;
	}


	private static String majoraAdult(List<Dados> examples) {

		int[] counter = new int[classes.length];

		for (Dados exemp: examples){
			switch (exemp.getTarget()) {
			case ">50K":
				counter[0]++;
				break;
			case "<=50K":
				counter[1]++;
				break;
			default:
				break;
			}
		}

		int max = Integer.MIN_VALUE, idx = 0;
		for (int i = 0; i < counter.length - 1; i++){
			if ( counter[i] >= max){
				max = counter[i];
				idx = i;
			}
		}

		String t = trocaAdult(idx);

		return t;
	}


	private static String trocaAdult(int i) {

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

	private static void setAttrs_size(int i) {
		tamnho_atrib = i;

	}

	private static double getRatio(){
		return ratio;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Conjunto<List<Dados>, List<Dados>> split_into_train_val(List<Dados> examples, double ratio) {
		int sz = examples.size(); int thr = (int) (sz * ratio);
		List<Dados> train_set = new ArrayList<Dados>();
		List<Dados> validation_set = new ArrayList<Dados>();

		if (thr != 0){
			for (int i = 0; i < thr; i++){
				train_set.add(examples.get(i));
			}

			for (int i = thr; i < sz; i++){
				validation_set.add(examples.get(i));
			}
			Conjunto<List<Dados>, List<Dados>> output = new Conjunto(train_set, validation_set);
			return output;
		}else{
			Conjunto<List<Dados>, List<Dados>> output = new Conjunto(examples, examples);
			return output;
		}
	}

	private static List<Dados> swap(List<Dados> examples, int i, int j) {
		Dados tmp = new Dados(examples.get(0).getData().length);
		tmp = examples.get(i).clone();
		examples.set(i, examples.get(j).clone());
		examples.set(j, tmp);

		return examples;
	}

	private static List<Dados> readExamples(String filepath) throws FileNotFoundException, IOException {
		List<Dados> examples = new ArrayList<Dados>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();

			while (line != null){

				Dados e = new Dados(line, atrbs.length);

				examples.add(e);

				line = reader.readLine();
			}

		}

		return examples;
	}

	private static void readAttributes(String filepath) throws FileNotFoundException, IOException {
		atrbs = new String[tamnho_atrib];

		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line = reader.readLine();
			int counter = 0;

			while (!line.isEmpty()){

				String[] tmp = line.split(" ");
				ArrayList<String> tmp_vals = new ArrayList<String>();

				for (int i = 1; i < tmp.length; i++){
					tmp_vals.add(tmp[i]);
				}

				atrib_val.put(tmp[0], tmp_vals);

				atrbs[counter] = tmp[0];

				counter++;

				line = reader.readLine();
			}

			line = reader.readLine();
			String[] tmp = line.split(" ");
			classes = new int[tmp.length - 1];
			for (int i = 0; i < classes.length; i++){
				classes[i] = i;
			}
		}

	}

	private static Set<Double> bestThreshold(List<Dados> S, int idx, Set<Double> c) {
		String[] temp_c = new String[c.size()];
		int i = 0;
		for(Double d: c){
			temp_c[i] = String.valueOf(d);
			i++;
		}

		int[] Target_attributes = {0,1}; 
		double entropy_S = entropiaAdult(S, Target_attributes);

		double S_size = S.size();

		double gain = 0.0, max = -Double.MAX_VALUE;

		String best_attribute = "";

		for (String attr: temp_c){

			double sigma = 0.0;

			i = 0;
			String[] itr = {"true", "false"};
			while(i<itr.length) {

				String v = itr[i];
				List<Dados> S_v = deriva_Adult(S,atrbs[idx]+"<="+ attr, v);

				double Sv_size = S_v.size();

				double ratio = Sv_size / S_size;

				double entropy_Sv = 0;
				if (ratio == 0){
					entropy_Sv = 0;
				}else{

					entropy_Sv = entropiaAdult(S_v, Target_attributes);
				}
				sigma+= - ratio * entropy_Sv;

				i++;
			}

			gain = entropy_S + sigma;
			if (gain > max){
				max = gain;
				best_attribute = attr;
			}
		}
		Set<Double> best_threshold = new HashSet<Double>();
		best_threshold.add(Double.valueOf(best_attribute));



		return best_threshold;
	}

	private static double entropiaAdult(List<Dados> S, int[] Target_attributes){
		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Dados exemp: S){
				if (exemp.getTarget().equals(trocaAdult(i))){
					counter[i]++;
				}
			}
		}

		double[] p = new double[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){
			p[i] = counter[i]/(double) S.size();
		}

		double sum = 0.0;
		for (int i = 0; i < p.length; i++){
			if (p[i] != 0){
				sum += (-p[i] * Math.log(p[i]) / Math.log(2));
			}
		}

		return sum;
	}

	private static List<Dados> deriva_Adult(List<Dados> S, String A, String v) {
		List<Dados> tmp = new ArrayList<Dados>();

		int idx;
		if (A.contains("<=")){
			idx = Arrays.asList(atrib_orig).indexOf(A.split("<=")[0]);

			Double thr = Double.valueOf(A.split("<=")[1]);

			for (Dados exemp: S){
				if (v.equals("true")){
					if (Double.valueOf(exemp.get(idx)) <= thr){
						tmp.add(exemp);
					}
				}else{
					if (Double.valueOf(exemp.get(idx)) > thr){
						tmp.add(exemp);
					}
				}
			}
		}else { 
			idx = Arrays.asList(atrib_orig).indexOf(A);

			for (Dados exemp: S){
				if (v.equals(String.valueOf(Double.valueOf(exemp.get(idx))))){
					tmp.add(exemp);
				}
			}
		}


		return tmp;
	}

	public static void random(List<Dados> Examples, int fold) {
		int r = fold;
		Integer[] arr = new Integer[Examples.size()];
		List<List<Dados>> train = new ArrayList<List<Dados>>(r);
		List<List<Dados>> val = new ArrayList<List<Dados>>(r);

		for (int i = 0; i < arr.length; i++) {
			arr[i] = i;
		}
		Collections.shuffle(Arrays.asList(arr));

		System.out.println(Arrays.toString(arr));
		System.out.println(Examples.size());
		System.out.println(arr[32]);

		int qtd = Examples.size()/r;

		List<Dados> temptrain = new ArrayList<Dados>();
		List<Dados> tempval = new ArrayList<Dados>();

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
