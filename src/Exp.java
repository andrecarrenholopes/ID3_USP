/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 6, 2017
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 * 
 * Exp is a class containing only one example; a line from the input txt file.
 * Each line contains a row of a value for all possible attributes plus a target class at the end.

 */
public class Exp{
	
	private String[] data = new String[4];
	private String target;
	
	// argin could be like [Sunny Hot High Weak No]
	Exp(String argin){	 
		this.add(argin.split(" "));
	}
	
	Exp (){
		data = new String[4];
		target = "";
	}
	
	Exp (int datasize){
		data = new String[datasize];
		target = "";
	}
	
	Exp (String argin, int datasize){
		this.data = new String[datasize];
		this.add(argin.split(" "));
	}
	
	// called functions	
	void add(String[] strings){
		
		for (int i = 0; i < strings.length - 1; i++){
			this.data[i] = strings[i];
		}
		
		this.settarget(strings[strings.length - 1]);
	}
	
	// getters and setters
	public String get(int idx){
		// takes the index of data and returns the particular element of the string array
		return this.getData()[idx];
	}
	public void set(){
		
	}
	public String[] getData(){
		return this.data;
	}
	public void setData(String[] d){
		for (int i = 0; i < d.length; i++){
			this.data[i] = d[i];
		}
	}
	public String getTarget(){
		return this.target;
	}
	public void settarget(String t){
		this.target = t;
	}
	
	public int compareTo(Exp exp, int a) {
		int res = Double.compare(Double.valueOf(this.getData()[a]), Double.valueOf(exp.getData()[a]));

		return res;
	}
	
	public Exp clone (){
		
		Exp e = new Exp(this.data.length);
		try {
			e.settarget(this.getTarget());
			e.setData(this.getData());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return e;
	}
}
