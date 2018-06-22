import java.util.*;

public class Aprendizado {

	private HashMap<String, ArrayList<String>> atrib_val;
	private Arvore arvr = new Arvore();
	private String[] Atributos;
	private String dataset;
	private String[] Atributos_orig;
	private static String[] Atrib_sA;
	private List<Dados> Examples_test;

	Aprendizado (List<Dados> Examples, String[] Atributos, HashMap<String,
			ArrayList<String>> atrib_val, int[] Target_attributes, String name_dataset, String[] atrib_orig, List<Dados> Test){

		setAttribute_values(atrib_val);
		setAttributes(Atributos);
		setDatasetName(name_dataset);
		setOriginalAttributes(atrib_orig);
		Atrib_sA = getAttributes();
		this.Examples_test = Test;
		printAtribGain(Examples, Target_attributes, Atributos);
		arvr = train(Examples, Target_attributes,Atrib_sA);

		arvr.setAttribute(this.getAttributes());

		arvr.assignParents(null);

	}

	@SuppressWarnings("rawtypes")
	private Arvore train(List<Dados> Examples, int[] Target_attributes, String[] Atrib_sA){

		Arvore arvr = new Arvore();

		arvr = checa_fim(arvr, Examples, Atrib_sA, Target_attributes);
		if (arvr != null) {
			return arvr;
		} else {

			arvr = new Arvore();

			String best_attr = melhorAtrib(Examples, Target_attributes, Atrib_sA);

			arvr.setRoot(best_attr);

			Iterator itr = atrib_val.get(best_attr).iterator();
			HashMap <String, Arvore> filho = new HashMap<String, Arvore>();

			Atrib_sA = trim_Attrs(Atrib_sA, best_attr);

			while (itr.hasNext()){
				Arvore subtree = new Arvore();

				String vi = (String) itr.next();
				filho.put(vi, null);
				List<Dados> examples_vi = deriva_exemp (Examples, best_attr, vi);

				if (examples_vi.isEmpty()){
					subtree = getMostCommonValue(subtree, Examples, Target_attributes);
					filho.put(vi, subtree);
					arvr.setChildren(filho);
				}else{

					subtree = train( examples_vi, Target_attributes, Atrib_sA);
					filho.put(vi, subtree);
					arvr.setChildren(filho);
				}	
			}
		}
		return arvr;
	}

	private String[] trim_Attrs(String[] attributes, String best_attr) {
		String[] output_attrs;

		if ((attributes.length - 1) == 0){
			return null;
		}else{
			output_attrs = new String[attributes.length - 1];
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

	private Arvore getMostCommonValue(Arvore arvr, List<Dados> examples, int[] Target_attributes) {

		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Dados exemp: examples){
				if (exemp.getTarget().equals(replace(i))){
					counter[i]++;
				}
			}
		}

		int sentinel = Integer.MIN_VALUE;
		for (int i = 0; i < counter.length; i++){
			if (counter[i] > sentinel){
				arvr.setRoot(replace(i));
				sentinel = counter[i];
			}
		}
		return arvr;
	}

	private String replace(int i) {

		String output = "noun";

		if (this.getdatasetName().equals("tennis")){
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

	public String melhorAtrib(List<Dados> S, int[] Target_attributes, String[] Atributos) {

		double entropy_S = faz_entropia(S, Target_attributes);

		double S_size = S.size();

		double gain = 0.0, max = -Double.MAX_VALUE;

		String best_attribute = "";

		for (String attr: Atributos){

			double sigma = 0.0;

			Iterator itr = atrib_val.get(attr).iterator();
			while (itr.hasNext()){

				String v = (String) itr.next();
				List<Dados> S_v = deriva_exemp(S, attr, v);

				double Sv_size = S_v.size();

				double ratio = Sv_size / S_size;

				double entropy_Sv = 0;
				if (ratio == 0){
					entropy_Sv = 0;
				}else{

					entropy_Sv = faz_entropia(S_v, Target_attributes);
				}
				sigma+= - ratio * entropy_Sv;

			}

			gain = entropy_S + sigma;
			if (gain > max){
				max = gain;
				best_attribute = attr;
			}
			//System.out.println("atributo: " + attr + " ganho: " + gain);
		}

		return best_attribute;
	}

	
	public String printAtribGain(List<Dados> S, int[] Target_attributes, String[] Atributos) {

		double entropy_S = faz_entropia(S, Target_attributes);

		double S_size = S.size();

		double gain = 0.0, max = -Double.MAX_VALUE;

		String best_attribute = "";

		for (String attr: Atributos){

			double sigma = 0.0;

			Iterator itr = atrib_val.get(attr).iterator();
			while (itr.hasNext()){

				String v = (String) itr.next();
				List<Dados> S_v = deriva_exemp(S, attr, v);

				double Sv_size = S_v.size();

				double ratio = Sv_size / S_size;

				double entropy_Sv = 0;
				if (ratio == 0){
					entropy_Sv = 0;
				}else{

					entropy_Sv = faz_entropia(S_v, Target_attributes);
				}
				sigma+= - ratio * entropy_Sv;

			}

			gain = entropy_S + sigma;
			if (gain > max){
				max = gain;
				best_attribute = attr;
			}
			System.out.println("atributo: " + attr + " ganho: " + gain);
		}

		return best_attribute;
	}

	private List<Dados> deriva_exemp(List<Dados> S, String A, String v) {
		List<Dados> tmp = new ArrayList<Dados>();

		if (this.getdatasetName().equals("tennis")){

			int idx = Arrays.asList(this.getAttributes()).indexOf(A);

			for (Dados exemp: S){
				if (exemp.get(idx).equals(v)){
					tmp.add(exemp);
				}
			}
		}else if (this.getdatasetName().equals("adult")){
			
			int idx;
			if (A.contains("<=")){
				idx = Arrays.asList(this.getOrigAttributes()).indexOf(A.split("<=")[0]);
			
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
				idx = Arrays.asList(this.getOrigAttributes()).indexOf(A);
			
				for (Dados exemp: S){
					if (exemp.get(idx).equals(v)){
						tmp.add(exemp);
					}
				}
			}
		}

		return tmp;
	}


	private Arvore checa_fim(Arvore arvr, List<Dados> examples, String[] atrbs, int[] Target_attributes) {

		boolean unified = true;

		if (examples.size() == 0){
			return arvr;

		}
		String target_val = examples.get(0).getTarget();

		for (Dados exemp: examples){
			String tmp_target = exemp.getTarget();
			if (!target_val.equals(tmp_target)){
				unified = false;
				break;
			}
		}

		if (unified){
			arvr.setRoot(target_val);
			return arvr;
		}

		if (atrbs == null){
			arvr = getMostCommonValue(arvr, examples, Target_attributes);
			return arvr;
		}

		if (atrbs.length == 0){
			arvr = getMostCommonValue(arvr, examples, Target_attributes);
			return arvr;
		}
		return null;
	}

	private double faz_entropia(List<Dados> S, int[] Target_attributes){
		int[] counter = new int[Target_attributes.length];

		for (int i = 0; i < counter.length; i++){

			for (Dados exemp: S){
				if (exemp.getTarget().equals(replace(i))){
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


	@SuppressWarnings("unchecked")
	private void setAttribute_values(HashMap<String, ArrayList<String>> argin) {
		this.atrib_val = (HashMap<String, ArrayList<String>>) argin.clone();


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

	public Arvore getTree() {	
		return this.arvr;
	}

}
