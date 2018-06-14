
public class Pair<F , S> {
	
	private F first;
	private S second;
	
	Pair(F first, S second){
		this.first = first;
		this.second = second;
		
	}
	
	protected F getfirst(){
		return this.first;
	}
	protected S getsecond() {
		return this.second;
	}

}
