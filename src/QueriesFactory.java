import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueriesFactory {
	/**
	 * Builds queries using the input data and Bayesian Network
	 * @param bnt
	 * @param queriesData
	 * @return list of Queries
	 * @throws Exception
	 */
	public static List<Query> BuildQueries(BayesianNetwork bnt,List<String> queriesData) throws Exception {
		List<Query> ans = new ArrayList<>();
		while(!queriesData.isEmpty()){
			while(queriesData.get(0).equals("") && queriesData.size()>1) {
				queriesData.remove(0);
			}
			String[] splittedData = queriesData.remove(0).split("[|]");
			String[] varAndVal = splittedData[0].substring(2).split("=");
			Variable v = bnt.getVarByName(varAndVal[0]);
			String[] varsEvidenceAndType = splittedData[1].split(",");
			String type = varsEvidenceAndType[varsEvidenceAndType.length-1];
			//OPE:= observed parents evidence
			String[] OPE = Arrays.copyOf(varsEvidenceAndType, varsEvidenceAndType.length-1);
			String tmp = OPE[OPE.length - 1].substring(0,OPE[OPE.length - 1].length()-1);
			OPE[OPE.length - 1] = tmp;
			ArrayList<Variable> observedVars = new ArrayList<>(OPE.length);
			ArrayList<String> evidence = new ArrayList<>(OPE.length);
			for(int i = 0;i < OPE.length;i++) {
				observedVars.add(bnt.getVarByName(OPE[i].split("=")[0]));
				evidence.add(OPE[i].split("=")[1]);
			}
			ans.add(new Query(bnt,v,varAndVal[1],observedVars,evidence,type));
		}

		
		return ans;
	}

}
