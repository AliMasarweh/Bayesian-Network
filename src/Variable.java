import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Variable implements Comparable<Variable>{
	
	private String Name;
	private List<String> Values;
	private List<Variable> Parents;
	private Map<String,Double> CPT;//** Hashed sequence of evidence
	
	
	/* ***Constructors*** */
	/**
	 * Initializing only name with argument, the rest are empty
	 * @param name
	 */
	public Variable(String name) {
		Name = name;
		Values = new ArrayList<>();
		Parents = new ArrayList<>();
		CPT = new TreeMap<>();
	}
	
	/**
	 * Deep copy constructors without copying CPT
	 * @param var
	 */
	public Variable(String name,List<String> Values,List<Variable> Parents) {
		this(name,Values,Parents,new TreeMap<>());
	}
	
	/**
	 * Deep copy constructors
	 * @param var
	 */
	public Variable(Variable var) {
		this(var.Name,var.Values,var.Parents,var.CPT);
	}
	
	/**
	 * Initializing all fields with arguments
	 * @param name
	 * @param Values
	 * @param Parents
	 * @param CPT
	 */
	public Variable(String name,List<String> Values,List<Variable> Parents,Map<String,Double> CPT) {
		Name = new String(name);
		this.Values = new ArrayList<>(Values);
		this.Parents = Parents == null? null:new ArrayList<>(Parents);
		this.CPT = new TreeMap<>(CPT);
	}
	
	/* ***Methods*** */
	/**
	 * 
	 * @return number of parents
	 */
	public int numOfParents() {
		return (Parents == null)?0:Parents.size();
	}
	/**
	 * 
	 * @param parent
	 * @return the index of given parent if exists, otherwise null
	 */
	public int indexOf(Variable parent) {
		return (Parents == null)?0:Parents.indexOf(parent);
	}
	/**
	 * 
	 * @param i
	 * @return parent at index i if exists, otherwise null
	 */
	public Variable parentAt(int i) {
		return (Parents == null?null:new Variable(Parents.get(i)));
	}
	/**
	 * 
	 * @return name of variable
	 */
	public String getName() {
		return Name;
	}
	/**
	 * 
	 * @return values
	 */
	public ArrayList<String> getValues(){
		return new ArrayList<>(Values);
	}
	/**
	 * 
	 * @return parents
	 */
	public ArrayList<Variable> getParents(){
		return (Parents!=null)? new ArrayList<>(Parents): null;
	}
	/**
	 * 
	 * @return all parents names
	 */
	private String parentsNames(){
		String ans = new String("[");
		for(Variable par:Parents)
			ans += par.getName()+",";
		ans = ans.substring(0, ans.length()-1) +"]";
		return ans;
	}
	/**
	 * 
	 * @return conditional probability table (CPT)
	 */
	public TreeMap<String,Double> getCPT(){
		return new TreeMap<>(CPT);
	}

	/**
	 * Adds the value to variable's values
	 * @param values
	 */
	public void AddValues(List<String> values) {
		Values = new ArrayList<>(values.size());
		for(String val:values) {
			while(val.charAt(val.length()-1)==' ')
				val = val.substring(0, val.length()-1);
			while(val.charAt(0) == ' ')
				val = val.substring(1);
			Values.add(val);
		}
	}
	/**
	 * Adds the parents to variable's parents
	 * @param parents
	 */
	public void AddParents(List<Variable> parents) {
		Parents = (parents != null)? new ArrayList<>(parents):null;
	}
	/**
	 * 
	 * @return number of values
	 */
	int numOfValues() {
		return this.Values.size();
	}
	/**
	 * Adds the key and values to CPT
	 * @param sequenceValues
	 * @param probability
	 */
	public void AddToCPT(String sequenceValues,Double probability) {
		while(sequenceValues.charAt(sequenceValues.length()-1)==' ')
			sequenceValues = sequenceValues.substring(0, sequenceValues.length()-1);
		while(sequenceValues.charAt(0)==' ')
			sequenceValues = sequenceValues.substring(1);
		CPT.put(sequenceValues, probability);
	}
	/**
	 * 
	 * @return depth of variable in network
	 */
	public int depth() {
		if(Parents == null)
			return 0;
		int max = 0;
		for(Variable par:Parents)
			max = Math.max(max, par.depth()+1);
		return max;
	}
	
	/**
	 * 
	 * @param network
	 * @param evidence
	 * @param operationsC
	 * @return the probability of given event for every variable. (All variables are observed).
	 */
	private static double CalculateProbForObservedNetwork(BayesianNetwork network,List<Variable> observedVar,List<String> evidence,int[] operationsC) {
		double ans = 1;String eviOfVar = "";double prob=0;
		for (int indexOfVar = 0; indexOfVar < network.size(); indexOfVar++) {
			Variable var = network.varAt(indexOfVar);
			if(var.Parents == null || var.Parents.isEmpty()) {
				eviOfVar = evidence.get(observedVar.indexOf(var));
				prob = var.CPT.get(eviOfVar);
				operationsC[1]++;
				ans*=prob;
			}
			else {
				String conditionalProb = "";
				for (int i = 0; i < var.Parents.size(); i++) {
					Variable par = var.Parents.get(i);
					String eviOfPar = evidence.get(observedVar.indexOf(par));
					conditionalProb += eviOfPar +",";
				}
				eviOfVar = evidence.get(observedVar.indexOf(var));
				conditionalProb += eviOfVar;
				prob = var.CPT.get(conditionalProb);
				operationsC[1]++;
				ans *= prob;
			}
		}
		return ans;
	}
	/**
	 * 
	 * @param network
	 * @param obeservedVar
	 * @param evidence
	 * @param operationsC
	 * @param i index of current variable in network
	 * @param append if not observed, observe for each value, add to observedVar and evidence by append
	 * @return observes every possible value for each hidden variable and summarize their probabilities.
	 */
	private static double ObserveHiddenVars(BayesianNetwork network,List<Variable> obeservedVar,
			List<String> evidence,int[] operationsC,int i,int append) {
		if(i==network.size()) {
			operationsC[1]--;
			return CalculateProbForObservedNetwork(network, obeservedVar, evidence, operationsC);
		}
		Variable var = network.varAt(i);
		if(obeservedVar.contains(var)) {
			return ObserveHiddenVars(network, obeservedVar, evidence, operationsC, i+1,append);
		}
		double sum = 0;
		List<String> vals = var.Values;
		operationsC[0] += var.numOfValues()-1;
		obeservedVar.add(append,var);
		for(String val : vals) {
			evidence.add(append,val);
			sum += ObserveHiddenVars(network, obeservedVar, evidence, operationsC, i+1,append+1);
			evidence.remove(append);
		}
		obeservedVar.remove(append);
		return sum;
	}
	/**
	 * 
	 * @param network
	 * @param var query variable
	 * @param obeservedVar observed variables
	 * @param evidence evidence of observed variable
	 * @param operationsC
	 * @return probability of query using algorithm 1
	 */
	public static double probabilityOfNoFactoring(BayesianNetwork network,Variable var,List<Variable> obeservedVar,
			List<String> evidence,int[] operationsC) {
		double sumForVal = 0,sumForOther = 0;
		sumForVal = ObserveHiddenVars(network, obeservedVar, evidence, operationsC, 0,obeservedVar.size());
		String Value = evidence.remove(0);
		for(String val:var.Values) {
			if(!Value.equals(val)) {
				evidence.add(0,val);
				double tmp = ObserveHiddenVars(network, obeservedVar, evidence, operationsC, 0, obeservedVar.size());
				sumForOther += tmp;
				evidence.remove(0);
			}
		}
		operationsC[0]++;
		double alpha = 1.0/(sumForVal+sumForOther)*100000.0;
		return Math.round(sumForVal*alpha)/100000.0;
	}
	
/*	private double probOf(List<Variable> obeservedVar,List<String> evidence,int[] operationsC) {
		double ans = 1;
		String eviOfThis = evidence.get(obeservedVar.indexOf(this));
		if(this.Parents == null)
			ans = this.CPT.get(eviOfThis);
		else {
			String conditionEvi = "";
			for (int i = 0; i < this.Parents.size(); i++) {
				Variable par = this.Parents.get(i);
				String eviOfPar = evidence.get(obeservedVar.indexOf(par));
				conditionEvi += eviOfPar +",";
			}
			conditionEvi += eviOfThis;
			ans = this.CPT.get(conditionEvi);
		}
		return ans;
	}
	
	private static double ObserveHiddenVarsFac(BayesianNetwork network,List<Variable> obeservedVar,
			List<String> evidence,int[] operationsC,int i,int append) {
		if(i==network.size()) {
			operationsC[1]--;
			return 1;
		}
		Variable var = network.varAt(i);
		if(obeservedVar.contains(var)) {
			operationsC[1]++;
			return var.probOf(obeservedVar, evidence, operationsC)*ObserveHiddenVarsFac(network, obeservedVar, evidence, operationsC, i+1,append);
		}
		double sum = 0;
		List<String> vals = var.Values;
		obeservedVar.add(append,var);
		for(String val : vals) {
			evidence.add(append,val);
			sum += var.probOf(obeservedVar, evidence, operationsC)*ObserveHiddenVarsFac(network, obeservedVar, evidence, operationsC, i+1,append+1);
			operationsC[1]++;
			operationsC[0]++;
			evidence.remove(append);
		}
		obeservedVar.remove(append);
		operationsC[0]--;
		return sum;
	}	
	
	public static double probabilityWithFactoring(BayesianNetwork network,Variable var, String Value, List<Variable> obeservedVar,
			List<String> evidence,int[] operationsC) {
		obeservedVar.add(0, var);
		evidence.add(0,Value);
		BayesianNetwork tmpNetwork = network.eliminateVariables(var,obeservedVar);
		double sumForVal = 0,sumForOther = 0;
		sumForVal = ObserveHiddenVarsFac(tmpNetwork, obeservedVar, evidence, operationsC, 0,obeservedVar.size());
		for(String val:var.Values) {
			if(!Value.equals(val)) {
				evidence.add(0,val);
				double tmp = ObserveHiddenVarsFac(network, obeservedVar, evidence, operationsC, 0, obeservedVar.size());
				sumForOther += tmp;
				evidence.remove(0);
			}
		}
		operationsC[0]++;
		double alpha = 1.0/(sumForVal+sumForOther)*100000.0;
		return Math.round(sumForVal*alpha)/100000.0;
	}
	*/
	
	/**
	 * 
	 * @param network
	 * @param var
	 * @param value
	 * @param observedVar
	 * @param evidence
	 * @param operationsC
	 * @return probability of query using algorithm 2
	 */
	public static double varEli(BayesianNetwork network,Variable var,String value, List<Variable> observedVar,
			List<String> evidence,int[] operationsC) {
		BayesianNetwork tmpNetwork = network.eliminateVariables(var,observedVar);
		List<CPTX> listCPTX = new ArrayList<>();
		listCPTX.add(CPTX.createForQueryVariable(var,observedVar,evidence));
		for (int i = 0; i < tmpNetwork.size(); i++) {
			Variable tmp = tmpNetwork.varAt(i);
			if(!tmp.equals(var))
				listCPTX.add(new CPTX(tmp,observedVar,evidence));
		}
		for (int i = listCPTX.size() - 1; i > 0; i--) {
			CPTX F1 = listCPTX.remove(i),
					F2 = listCPTX.remove(i-1),
					F12 = F1.CartasianProduct(F2,operationsC);
			if(i != 1)
				F12.eliminateHidden(listCPTX,operationsC);
			listCPTX.add( F12);
		}
		CPTX f = listCPTX.remove(0);
		if(f.stillHiddenVariables()) {
			f.eliminateOnOtherThan(var.getName(),operationsC);
		}
		Map<String, Double> cptOfQueryVar = f.getCptOfHidden();
		double ans = cptOfQueryVar.get(value);
		double sum = 0;
		for(String val : var.Values) {
				sum += cptOfQueryVar.get(val);
		}
		return Math.round(ans*100000.0/sum)/100000.0;
	}
	/**
	 * 
	 * @param network
	 * @param var
	 * @param value
	 * @param observedVar
	 * @param evidence
	 * @param operationsC
	 * @return probability of query using algorithm 3
	 */
	public static double bestOrderOfNetWork(BayesianNetwork network,Variable var,String value, List<Variable> observedVar,
			List<String> evidence,int[] operationsC) {
		BayesianNetwork tmpNetwork = network.bestOrderOfVars(var, observedVar);
		return varEli(tmpNetwork, var, value, observedVar, evidence, operationsC);
	}
	

	/**
	 * @return if names are equal true, else false.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Variable) {
			return this.Name.equals(((Variable) obj).Name);
		}
		
		return false;
	}
	/**
	 * @return data of variable as String
	 */
	@Override
	public String toString() {
		String ans = "Name: "+this.Name +"\n Values: "+Arrays.toString(Values.toArray())+"\n Parents: ";
		ans+=(Parents==null?null:parentsNames())+"\n CPT:\n: "+CPT;
		return ans;
	}
	/**
	 * @return compares the number of values.
	 */
	@Override
	public int compareTo(Variable o) {
		return o.numOfValues() - this.numOfValues();
	}
}
