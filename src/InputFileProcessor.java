import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class InputFileProcessor {
	
	private BayesianNetwork BNT;
	private List<Query> queriesList;
	/**
	 * Processes and builds the network and queries.
	 * @param FileName
	 */
	public void ProcessFile(String FileName) {
		BufferedReader buffInFlReader = null;
		List<String> VariablesData = null,QueriesData = null;
		try {
			buffInFlReader = new BufferedReader(new FileReader(FileName));
			VariablesData = DataOfVariablesOrQueries(buffInFlReader);
			QueriesData = DataOfVariablesOrQueries(buffInFlReader);
			buffInFlReader.close();
			BuildBayesianNetwork(VariablesData);
			BuildQueriesList(QueriesData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	/**
	 * 
	 * @param buffInFlReader
	 * @return First time being called collects the network data, Second time collects queries data.
	 * @throws IOException
	 */
	private List<String> DataOfVariablesOrQueries(BufferedReader buffInFlReader) throws IOException{
		List<String> ans = new ArrayList<>();
		String Line = buffInFlReader.readLine();
		while( Line != null && !Line.equals("Queries")) {
			ans.add(Line);
			Line = buffInFlReader.readLine();
		}
		return ans;
	}
	/**
	 * Builds the network using factory
	 * @param VariablesData
	 * @throws Exception
	 */
	private void BuildBayesianNetwork(List<String> VariablesData) throws Exception{
		BNT = BayesianNetworkFactory.BuildBayesNetwork(VariablesData);
	}
	/**
	 * Builds the queries using factory
	 * @param QueriesData
	 * @throws Exception
	 */
	private void BuildQueriesList(List<String> QueriesData) throws Exception {
		queriesList = QueriesFactory.BuildQueries(BNT,QueriesData);
	}
	/**
	 * processes the queries and yields outupt.txt file
	 */
	public void processQueries() {
		int[] operationsCount = new int[2];
		File output = new File("output.txt");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(output);
			for(Query query:queriesList) {
				operationsCount[0] = 0;
				operationsCount[1] = 0;
				double ans = query.process(operationsCount);
				pw.println(ans+","+operationsCount[0]+","+operationsCount[1]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		pw.close();
	}

}
