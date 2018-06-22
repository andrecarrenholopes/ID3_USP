
public class Dados{
	
	private String[] data = new String[4];
	private String target;
	
	Dados(String argin){	 
		this.add(argin.split(" "));
	}
	
	Dados (){
		data = new String[4];
		target = "";
	}
	
	Dados (int datasize){
		data = new String[datasize];
		target = "";
	}
	
	Dados (String argin, int datasize){
		this.data = new String[datasize];
		this.add(argin.split(" "));
	}
	
	void add(String[] strings){
		
		for (int i = 0; i < strings.length - 1; i++){
			this.data[i] = strings[i];
		}
		
		this.settarget(strings[strings.length - 1]);
	}
	
	public String get(int idx){
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
	
	public int compareTo(Dados exemp, int a) {
		int res = Double.compare(Double.valueOf(this.getData()[a]), Double.valueOf(exemp.getData()[a]));

		return res;
	}
	
	public Dados clone (){
		
		Dados e = new Dados(this.data.length);
		try {
			e.settarget(this.getTarget());
			e.setData(this.getData());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return e;
	}
}
