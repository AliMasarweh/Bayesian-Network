import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BayesianNetwork {

	private List<Variable> varsNetwork;//** List of the network variables
	/**
	 * Initialize empty network
	 */
	public BayesianNetwork() {
		varsNetwork = new ArrayList<>();
	}
	
	/**
	 * Deep copy of the Bayesian network
	 * @param network
	 */
	public BayesianNetwork(BayesianNetwork network) {
		this.varsNetwork = new ArrayList<>(network.varsNetwork);
	}

	/**
	 * 
	 * @return a copy of the Bayesian network
	 */
	public List<Variable> getCopyOfBNTK(){
		return new ArrayList<>(varsNetwork);
	}
	/**
	 * Adds the variable to the network
	 * @param var
	 */
	public void AddVariable(Variable var) {
		varsNetwork.add(var);
	}
	/**
	 * @param i
	 * @return returns variable at that index
	 */
	public Variable varAt(int i) {
		if(i<varsNetwork.size())
			return varsNetwork.get(i);
		return null;
	}
	/**
	 * @param var
	 * @return returns index of variable in network
	 */
	public int indexOf(Variable var) {
		return varsNetwork.indexOf(var);
	}
	/**
	 * @return size if network (number of variables)
	 */
	public int size() {
		return varsNetwork.size();
	}
	/**
	 * return the variable that goes by that name if in the network, otherwise null
	 * @param name of the variable
	 * @return
	 */
	public Variable getVarByName(String name) {
		for(Variable var:varsNetwork) {
			if(var.getName().equals(name))
				return var;
		}
		return null;
	}
	/**
	 * Returns a string describing the network
	 */
	public String toString() {
		return varsNetwork.toString();
	}

	/**
	 * 
	 * @param var
	 * @param observedVar
	 * @return a new network with eliminated irrelevant variables.
	 */
	public BayesianNetwork eliminateVariables(Variable var, List<Variable> observedVar) {
		BayesianNetwork ans = new BayesianNetwork();
		List<String> namesOfVarsAndThierParents = new ArrayList<>();
		List<Variable> keepVars = new ArrayList<>(observedVar);
		int start = this.varsNetwork.indexOf(var),depth = var.depth();
		for(Variable v:observedVar) {
			int tmpD = v.depth();
			if(tmpD > depth) {
				depth = tmpD;
				start = this.varsNetwork.indexOf(v);
			}
			else if(tmpD == depth)
				start = Math.max(start, this.varsNetwork.indexOf(v));
		}
		keepVars.add(var);
		for(int i = start; i >=0; i--) {
			Variable v = this.varsNetwork.get(i);
			namesOfVarsAndThierParents.add(v.getName());
			getNamesOfParentsOf(v,namesOfVarsAndThierParents);
		}
		/*This way is more efficient, but discards a lot and with no 
		 * regards of order keeping only the very relevant variables*/
		/*keepVars.add(var);
		getNamesOfParentsOf(var, namesOfVarsAndThierParents);
		for(Variable v:observedVar) {
			keepVars.add(v);
			getNamesOfParentsOf(v, namesOfVarsAndThierParents);
		}*/
		for(Variable v : varsNetwork) {
			if(namesOfVarsAndThierParents.contains(v.getName()))
				ans.AddVariable(v);
		}
		return ans;
	}
	/**
	 * Adds the variable and the variable parents to the network
	 * @param var
	 * @param nameOfVarParents 
	 */
	private void getNamesOfParentsOf(Variable var, List<String> namesOfVarsAndThierParents) {
		List<Variable> paretns = var.getParents();
		if(paretns!=null)
			for(Variable par : paretns) {
				String Name = par.getName();
				if(!namesOfVarsAndThierParents.contains(Name)) {
					namesOfVarsAndThierParents.add(Name);
					getNamesOfParentsOf(par, namesOfVarsAndThierParents);
				}
			}
	}
	/**
	 * Reorders the network to the best possible sequence
	 * @param var
	 * @param observedVar
	 * @return
	 */
	public BayesianNetwork bestOrderOfVars(Variable var, List<Variable> observedVar) {
		BayesianNetwork ans = new BayesianNetwork();
		List<Integer> listOfDepths = new ArrayList<>();
		int obeservedVarsMaxDepth = var.depth();
		for(Variable v : varsNetwork) {
			int vDepth = v.depth();
			listOfDepths.add(vDepth);
			if(observedVar.contains(v)) {
				obeservedVarsMaxDepth = Math.max(obeservedVarsMaxDepth, vDepth);
			}
		}
		BayesianNetwork tmp = new BayesianNetwork(this);
		for(int i = 0; i < tmp.varsNetwork.size();i++) {
			Variable v = tmp.varAt(i);
			int depthV = v.depth();
			if(depthV > obeservedVarsMaxDepth) {
				tmp.varsNetwork.remove(i);
				listOfDepths.remove(i);
			}
		}
		int depth = 1, start = 0, end = 0;
		observedVar.add(var);
		for(; end < listOfDepths.size(); end++) {
			int depthOfV = listOfDepths.get(end);
			if(depth == depthOfV){
				end--;
				ans.bestOrderFromTo(tmp,start,end,depthOfV-1,obeservedVarsMaxDepth,observedVar);
				start = end+1;
				depth++;
			}
			else if(end == listOfDepths.size() -1) {
				ans.bestOrderFromTo(tmp,start,end,depthOfV-1,obeservedVarsMaxDepth,observedVar);
				break;
			}
		}
		observedVar.remove(observedVar.size()-1);
		for(Variable v : varsNetwork)
			if(!ans.varsNetwork.contains(v))
				ans.varsNetwork.add(v);
		return ans;
	}

	/**
	 * Adds the variable and the variable parents to the network
	 * @param OriginalNet
	 * @param start
	 * @param end
	 * @param currentDepth
	 * @param obeservedVarsMaxDepth
	 * @param observedVar
	 */
	private void bestOrderFromTo(BayesianNetwork OriginalNet, int start, int end, int currentDepth, 
			int obeservedVarsMaxDepth, List<Variable> observedVar) {
		List<Variable> listOfNumOfValues = new ArrayList<>();
		for (int i = start; i <= end; i++) {
			Variable v = OriginalNet.varsNetwork.get(i);
			listOfNumOfValues.add(/*Collections.binarySearch(listOfNumOfValues, v)+1,*/v);
		}
		Collections.sort(listOfNumOfValues);
		if(currentDepth < obeservedVarsMaxDepth) {
			for(Variable v : listOfNumOfValues) {
				this.varsNetwork.add(v);
			}
		}
		else {
			for(Variable v : listOfNumOfValues) {
				if(observedVar.contains(v))
					this.varsNetwork.add(v);
			}
		}

	}

}
