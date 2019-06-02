import java.util.ArrayList;
import java.util.List;

public class Query {
	
	private BayesianNetwork BTN;
	private Variable Var;
	private String Value;
	private List<Variable> ObservedVars;
	private List<String> Evidence;
	private String Type;
	/**
	 * Query Constructor
	 * @param BNTK
	 * @param variable
	 * @param value
	 * @param observedVars
	 * @param evidence
	 * @param type
	 */
	public Query(BayesianNetwork BNTK,Variable variable,String value,List<Variable> observedVars,
			List<String> evidence, String type) {
		BTN=BNTK;
		Var=variable;
		Value = new String(value);
		this.ObservedVars = new ArrayList<>(observedVars);
		this.Evidence = new ArrayList<>(evidence);
		Type = new String(type);
	}
	/**
	 * 
	 * @param operationsCount in first cell counts summarizing operations 
	 * and the second for counting the multiplying operations
	 * @return answer of the query
	 */
	public double process(int[] operationsCount) {
		double ans = 1;
		if(Type.equals("1")) {
			ObservedVars.add(0, Var);
			Evidence.add(0,Value);
			return Variable.probabilityOfNoFactoring(BTN,Var, ObservedVars, Evidence, operationsCount);
		}
		else if(Type.equals("2")) {
			return Variable.varEli(BTN,Var, Value, ObservedVars, Evidence, operationsCount);
		}
		else if(Type.equals("3")) {
			return Variable.bestOrderOfNetWork(BTN,Var, Value, ObservedVars, Evidence, operationsCount);
		}
		return ans;
	}
	/**
	 * @return query as String.
	 */
	public String toString() {
		String ans = new String("P(");
		ans += Var.getName() +"="+ Value +"|";
		for (int i = 0; i < ObservedVars.size(); i++) {
			ans+=ObservedVars.get(i).getName() +"="+ Evidence.get(i)+",";
		}
		ans = ans.substring(0, ans.length()-1) + "),"+Type;
		return ans;
	}
}
