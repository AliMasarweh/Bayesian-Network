import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CPTX {
	
	private List<String> hiddeNames;
	private Map<String,Double> cptOfHidden;
	
	public List<String> getNamesOfHidden() {
		return hiddeNames;
	}

	public Map<String, Double> getCptOfHidden() {
		return cptOfHidden;
	}
	
	public boolean containsHiddenName(String Name) {
		return hiddeNames.contains(Name);
	}
	
	private CPTX() {
		hiddeNames = new ArrayList<>();
		cptOfHidden = new TreeMap<>();
	}
	
	/**
	 * 
	 * @param var
	 * @param observedVar
	 * @param evidence
	 * @return a CPTX unique for the query variable, if a parent are not hidden, unlike the CPTX for others,
	 * will keep him in the hidden names and the CPT keys will conclude only his observed values.
	 */
	public static CPTX createForQueryVariable(Variable var, List<Variable> observedVar, List<String> evidence) {
		CPTX ans = new CPTX();
		int numOfParents = var.numOfParents();
		for(int i = 0; i < numOfParents; i++)
			ans.hiddeNames.add(var.parentAt(i).getName());
		ans.hiddeNames.add(var.getName());
		Map<String,Double> tmp = new TreeMap<>(var.getCPT());
		Set<String> keys = tmp.keySet();
		boolean put = true;
		for(String key : keys) {
			put = true;
			String[] splitedKey = key.split(",");
			for (int i = 0; i < splitedKey.length; i++) {
				Variable parentI = i == splitedKey.length-1?var:var.parentAt(i);
				if(observedVar.contains(parentI) 
						&& !splitedKey[i].equals(evidence.get(observedVar.indexOf(parentI)))) {
					//System.out.println(parentI.getName() + " " + splitedKey[i]);
					put = false;
				}
			}
			if(put) {
				ans.cptOfHidden.put(key, tmp.get(key));
			}
		}
		return ans;
	}
	/**
	 * Create a CPTX for a given variable, every hidden variable will be saved to hiddenNames and each of their 
	 * values, while the observed one won't be saved neither to hiddenNames nor to CPT. (Filtering each value of
	 * the observed and keeping the keys that contains the evidence).
	 * @param A
	 * @param observedVars
	 * @param evidence
	 */
	public CPTX(Variable A, List<Variable> observedVars,List<String> evidence) {
		hiddeNames = new ArrayList<>();
		cptOfHidden = new TreeMap<>();
		fillHiddenNames(A,observedVars,evidence);
		Map<String,Double> tmpCpt = A.getCPT();
		Set<String> keySet = tmpCpt.keySet();
		for(String key : keySet) {
			if(isKeyWithRightEvidence(A,key,observedVars,evidence))
				cptOfHidden.put(onlyHiddenInKey(A,key), tmpCpt.get(key));
		}
	}
	
	/**
	 * Removes evidence and keeps the hidden variable's values
	 * @param a
	 * @param key
	 * @return a sequence of the hidden variable's values only
	 */
	private String onlyHiddenInKey(Variable a, String key) {
		String ans = "";
		String[] splitedKey = key.split(",");
		int i = 0, numOfParents = a.numOfParents();
		for(; i < numOfParents; i++)
			if(this.hiddeNames.contains(a.parentAt(i).getName()))
				ans+= splitedKey[i] + ",";
		if(this.hiddeNames.contains(a.getName()))
			ans += splitedKey[i];
		if(!ans.equals("") && ans.charAt(ans.length()-1)==',')
			ans = ans.substring(0, ans.length()-1);
		return ans;
	}
	/**
	 * 
	 * @param a
	 * @param key
	 * @param observedVars
	 * @param evidence
	 * @return true, if every value of observed variables is in the evidence.
	 */
	private boolean isKeyWithRightEvidence(Variable a, String key, List<Variable> observedVars, List<String> evidence) {
		String[] SKey = key.split(",");
		int i = 0, numOfParentsA = a.numOfParents();
		for(; i < numOfParentsA; i++) {
			if(observedVars.contains(a.parentAt(i)) && 
					!SKey[i].equals(evidence.get(observedVars.indexOf(a.parentAt(i)))))
					return false;
		}
		if(observedVars.contains(a) && !SKey[i].equals(evidence.get(observedVars.indexOf(a))))
			return false;
		return true;
	}
	/**
	 * Takes only the hidden variable's names
	 * @param A
	 * @param observedVars
	 * @param evidence
	 */
	private void fillHiddenNames(Variable A,List<Variable> observedVars, List<String> evidence) {
		int numOfParents = A.numOfParents();
		int i = 0;
		if(numOfParents == 0) {
			this.hiddeNames.add(A.getName());
			return;
		}
		for (; i < numOfParents; i++) {
			if(!observedVars.contains(A.parentAt(i)))
				this.hiddeNames.add(A.parentAt(i).getName());
		}
		if(!observedVars.contains(A))
			this.hiddeNames.add(A.getName());
	}
	/**
	 * 
	 * @param o
	 * @param operationsC
	 * @return returns the Cartesian product of this x o.
	 */
	public CPTX CartasianProduct(CPTX o, int[] operationsC) {
		CPTX ans = new CPTX();
		List<String> mutualVars = getMutualVars(o);
		ans.hiddeNames = hiddenNamesAfterJoin(o);
		Set<String> keySetT = this.cptOfHidden.keySet()
				,keySetO = o.cptOfHidden.keySet();
		for(String keyT:keySetT) {
			String[] splitedKeyT = keyT.split(",");
			for(String keyO:keySetO) {
				String[] splitedKeyO = keyO.split(",");
				boolean AMEV =AllMutualEqualsValue(o,mutualVars,splitedKeyT,splitedKeyO);
				if(AMEV) {
					operationsC[1]++;
					String newKey = Union(o,mutualVars,keyT,splitedKeyO);
					ans.cptOfHidden.put(newKey, this.cptOfHidden.get(keyT)*o.cptOfHidden.get(keyO));
				}
			}
		}
		return ans;
	}
	
	/**
	 * 
	 * @param o
	 * @param mutualVars
	 * @param keyT
	 * @param splitedKeyO
	 * @return keyT + (keyO / keyT). (In group theory terms).
	 */
	private String Union(CPTX o, List<String> mutualVars, String keyT, String[] splitedKeyO) {
		String ans = keyT + ",";
		for (int i = 0; i < o.hiddeNames.size(); i++) {
			String name = o.hiddeNames.get(i);
			if(!mutualVars.contains(name))
				ans += splitedKeyO[i] + ",";
		}
		if(ans.charAt(ans.length()-1) == ',')
			ans = ans.substring(0, ans.length()-1);
		if(ans.charAt(0) == ',')
			ans = ans.substring(1);
		return ans;
	}
	/**
	 * return is both rows are join-able?
	 * @param o
	 * @param mutualVars
	 * @param splitedKeyT
	 * @param splitedKeyO
	 * @return if each mutual variable have the same value return true, otherwise false.
	 */
	private boolean AllMutualEqualsValue(CPTX o, List<String> mutualVars, String[] splitedKeyT, String[] splitedKeyO) {	
		for (int i = 0; i < mutualVars.size(); i++) {
			String nameOfMutaulVar = mutualVars.get(i);
			int indexT = this.hiddeNames.indexOf(nameOfMutaulVar);
			int indexO = o.hiddeNames.indexOf(nameOfMutaulVar);
			if(!splitedKeyT[indexT].equals(splitedKeyO[indexO]))
				return false;
		}
		return true;
	}
	/**
	 * 
	 * @param o
	 * @return the new hidden names after joining this with o.
	 */
	private List<String> hiddenNamesAfterJoin(CPTX o) {
		List<String> ans = new ArrayList<>(this.hiddeNames);
		for(String name : o.hiddeNames)
			if(!this.hiddeNames.contains(name))
				ans.add(name);
		return ans;
	}
	/**
	 * 
	 * @param o
	 * @return mutual variables names
	 */
	private List<String> getMutualVars(CPTX o) {
		List<String> ans = new ArrayList<>();
		for(String nameT : this.hiddeNames) {
			for(String nameO : o.hiddeNames) {
				if(nameT.equals(nameO)) {
					ans.add(nameT);
					break;
				}
			}
		}
		return ans;
	}
	/**
	 * eliminates each hidden variable in this if not found in any CPTX in listCPTX.
	 * @param listCPTX
	 * @param operationsC
	 */
	public void eliminateHidden(List<CPTX> listCPTX, int[] operationsC) {
		List<Boolean> foundedVarsList = getListOfFoundedVars(listCPTX);
		int i = 0;
		List<Integer> eliminateIndxs = new ArrayList<>();
		for (; i < foundedVarsList.size();i++) {
			if(!foundedVarsList.get(i)) {
				eliminateIndxs.add(i);
				/*EliminateOn(i, operationsC);
				foundedVarsList.remove(i);
				i--;*/
			}
		}
		EliminateOn(eliminateIndxs, operationsC);
	}
	/**
	 * Eliminates variables based of the eliminateIndxs argument.
	 * @param eliminateIndxs
	 * @param operationsC
	 */
	private void EliminateOn(List<Integer> eliminateIndxs, int[] operationsC) {
		Map<String,Double> newCpt = new TreeMap<>();
		Set<String> keySet1 = this.cptOfHidden.keySet(),
				keySet2 = this.cptOfHidden.keySet();
		for(String key1 : keySet1) {
			String[] SKey1 = key1.split(",");
			String newKey = "";
			for (int j = 0; j < this.hiddeNames.size(); j++) {
				if(!eliminateIndxs.contains(j))
					newKey += SKey1[j] + ",";
			}
			if(!newKey.equals("") && newKey.charAt(newKey.length()-1) == ',')
				newKey = newKey.substring(0, newKey.length()-1);
			if(newCpt.get(newKey) == null) {
			double sum = 0; int c = 0;
			for(String key2 : keySet2) {
				String[] SKey2 = key2.split(",");
				if(AllEqualBut(eliminateIndxs,SKey1,SKey2)) {
					operationsC[0] += c>0 ?1:0;
					sum += this.cptOfHidden.get(key2);
					c++;
				}
			}
				newCpt.put(newKey, sum);
			}
		}
		this.cptOfHidden = newCpt;
		int shift=0;
		for(int eliIndx : eliminateIndxs)
			this.hiddeNames.remove(eliIndx-(shift++));
	}
	/**
	 * 
	 * @param eliminateIndxs
	 * @param sKey1
	 * @param sKey2
	 * @return if all values in splitted key in indexes that are not in eliminateIndxs equal, returns true
	 * otherwise false.
	 */
	private boolean AllEqualBut(List<Integer> eliminateIndxs, String[] sKey1, String[] sKey2) {
		for (int j = 0; j < sKey1.length; j++) {
			if(!eliminateIndxs.contains(j) && ! sKey1[j].equals(sKey2[j]))
				return false;
		}
		return true;
	}
	/**
	 * 
	 * @param listCPTX
	 * @return a list boolean, each boolean value corresponds to hidden name, if hidden name is found in 
	 * the list, the corresponding value will be true, otherwise false.
	 */
	private List<Boolean> getListOfFoundedVars(List<CPTX> listCPTX) {
		List<Boolean> ans = new ArrayList<>();
		for(int i = 0; i < this.hiddeNames.size(); i++) {
			String name = this.hiddeNames.get(i);
			for(CPTX cptF : listCPTX) {
				if(cptF.containsHiddenName(name)) {
					ans.add(i ,true);
					break;
				}
			}
			if(ans.size() == i)
				ans.add(i, false);
		}
		return ans;
	}
	/**
	 * Eliminates every hidden value in the CPTX of query variable.
	 * @param queryVarName
	 * @param operationsC
	 */
	public void eliminateOnOtherThan(String queryVarName ,int[] operationsC) {
		List<Integer> eliminateIndxs = new ArrayList<>();
		for (int i = 0; i < this.hiddeNames.size(); i++) {
			String name = this.hiddeNames.get(i);
			if(!name.equals(queryVarName)) {
				eliminateIndxs.add(i);
				/*EliminateOn(i, operationsC);
				i--;*/
			}
		}
		EliminateOn(eliminateIndxs, operationsC);
	}
	/**
	 * 
	 * @return if hiddenName has more than one value, returns true, otherwise, false.
	 */
	public boolean stillHiddenVariables() {
		if(this.hiddeNames.size() > 1)
			return true;
		return false;
	}
	/**
	 * @return data of CPTX as String
	 */
	public String toString() {
		String ans = "";
		ans = "Names of Hidden: " + this.hiddeNames.toString();
		ans += "Cpt : " +this.cptOfHidden.toString();
		return ans;
	}
}
