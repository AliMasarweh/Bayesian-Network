import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BayesianNetworkFactory {
	/**
	 * Builds bayesian network using the input data.
	 * @param variablesData
	 * @return {@link BayesianNetwork} of all the variables
	 * @throws Exception format error
	 */
	public static BayesianNetwork BuildBayesNetwork(List<String> variablesData) throws Exception {
		BayesianNetwork BNT = NamesOfVariables(variablesData);
		/*Blank Line*/variablesData.remove(0);
		String Line = null;
		Variable tmpVar = null;
		for(int lineI = 0;lineI < variablesData.size();) {
			tmpVar = BuildVarNameValuesParents(BNT, variablesData);
			Line = variablesData.remove(0);
			if(!Line.contains("CPT:")) throw new IOException("Variable build error of format!");
			//** Builds the conditional probability table.
			Line = variablesData.remove(0);
			List<String> condProbOfEviAndVal =  Arrays.asList(Line.split(","));//Conditional probability of evidence and values
			String Evidence = "";
			while(!Line.equals("")) {
				int valIndx = 0;/*Evidence Index*/
				//** Hashing the sequence of parents values of this variable 
				Evidence = "";
				condProbOfEviAndVal =  Arrays.asList(Line.split(","));
				for(;valIndx < condProbOfEviAndVal.size() - 2*(tmpVar.numOfValues()-1);valIndx++) {
					Evidence += condProbOfEviAndVal.get(valIndx) + ",";
				}
				//** Hashing this variable value
				double sum = 0;
				int indxOfVal = 0,treshold = tmpVar.numOfValues();
				for(;valIndx<condProbOfEviAndVal.size();valIndx++) {
					//** Assigning the conditional probability
					double probability = Double.parseDouble(condProbOfEviAndVal.get(++valIndx));
					sum += probability;
					tmpVar.AddToCPT(Evidence+condProbOfEviAndVal.get(--valIndx).substring(1), probability);
					valIndx++;
					indxOfVal++;
					if(indxOfVal == treshold -1) {
						tmpVar.AddToCPT(Evidence+tmpVar.getValues().get(indxOfVal),Math.round((1-sum)*100000.0)/100000.0);
					}
				}
				
				Line = variablesData.remove(0);
				if(Line.equals("") && !variablesData.isEmpty())
					if(!variablesData.get(0).equals("Queries")&& !variablesData.get(0).contains("Var"))
						Line = variablesData.remove(0);
			}
			/* Now the Variable has the complete data, and Line is empty which means either 
			 * the next line is a new variable we are done here*/
		}
		return BNT;
	}
	/**
	 * 
	 * @param variablesData
	 * @return {@link BayesianNetwork} with empty variables. (only Names).
	 * @throws IOException
	 */
	private static BayesianNetwork NamesOfVariables(List<String> variablesData) throws IOException {
		while(variablesData.get(0).equals(""))	variablesData.remove(0);
		if(!variablesData.remove(0).equals("Network"))
			throw new IOException("Wrong File Format!");
		List<String> netWorkNames = Arrays.asList(variablesData.remove(0).substring(new String("Variables: ").length()).split(","));
		BayesianNetwork BNT = new BayesianNetwork();
		for(String VariableName:netWorkNames)
			BNT.AddVariable(new Variable(VariableName));
		return BNT;
	}
	/**
	 * 
	 * @param BNT
	 * @param variablesData
	 * @return adds to the empty variable (Only Name): Values and Parents
	 * @throws IOException format Error
	 */
	private static Variable BuildVarNameValuesParents(BayesianNetwork BNT, List<String> variablesData) throws IOException {
		while(variablesData.get(0).equals(""))	variablesData.remove(0);
		String Line = variablesData.remove(0);
		Variable tmpVar = null;
		if(Line.contains("Var")) {
			tmpVar = BNT.getVarByName(Line.substring(4));
		}
		else throw new IOException("Variable build error of format!");
		while(variablesData.get(0).equals(""))	variablesData.remove(0);
		Line = variablesData.remove(0);
		if(Line.contains("Values:")) {
			tmpVar.AddValues(Arrays.asList(Line.substring(new String("Values:").length()).split(",")));
		}
		else throw new IOException("Variable build error of format!");
		while(variablesData.get(0).equals(""))	variablesData.remove(0);
		Line = variablesData.remove(0);
		if(Line.contains("Parents: "))
			if(Line.equals("Parents: none"))
				tmpVar.AddParents(null);
			else {
				String[] paretnsNames = Line.substring(new String("Parents: ").length()).split(",");
				ArrayList<Variable> varParents = new ArrayList<>(paretnsNames.length);
				for(int i = 0;i < paretnsNames.length;i++) {
					varParents.add(BNT.getVarByName(paretnsNames[i]));
				}
				tmpVar.AddParents(varParents);
			}
		else throw new IOException("Variable build error of format!");
		return tmpVar;
	}

}
