/**
 * 
 * @author Maryam Najafi, mnajafi2012@my.fit.edu
 *
 * Feb 22, 2017
 * 
 * Course:  CSE 5693, Fall 2017
 * Project: HW2, Decision Tree_ID3
 */
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
