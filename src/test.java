import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String x = "P(B=true|J=true,M=true),1";
		String[] y = x.split("[|]");
		System.out.println(y[0]+" "+y[1]);
		String[] VarAndVal = (y[0].substring(2)).split("=");
		System.out.println(VarAndVal[0]+" "+VarAndVal[1]);
		String z[]=y[1].split(","),type=z[z.length-1];
		z = Arrays.copyOf(z, z.length-1);
		System.out.println(z[0]+','+z[1]);*/
		
		
		/*String xyz = "XYZ";
		System.out.println(xyz.substring(0,xyz.length()-1));
		TreeMap<String,Double> CPT= new TreeMap<>();
		CPT.put("true,true,true", 0.95);
		CPT.put("true,true,false", 0.05);
		CPT.put("true,false,true", 0.94);
		CPT.put("true,false,false", 0.06);
		CPT.put("false,true,true", 0.29);
		CPT.put("false,true,false", 0.71);
		CPT.put("false,false,true", 0.001);
		CPT.put("false,false,false", 0.999);
		String ans = "";
		ans = Arrays.toString(CPT.keySet().toArray());
		ans+="\n values: "+Arrays.toString(CPT.values().toArray());
		System.out.println(ans);*/
		
		/*Variable b = new Variable("X");
		Variable c = new Variable("X");
		System.out.println(b.equals(c));*/
		
		
		/*TreeMap<String,Double> x = new TreeMap<>();
		x.put("123", 0.5);
		double fac1 = 2.0,fac2 = 0.12;
		System.out.println(x.computeIfAbsent("123", (k)->fac1));
		System.out.println(x.computeIfPresent("123", (k,v)->v*fac1));
		System.out.println(x.computeIfPresent("122", (k,v)->v*fac2));
		System.out.println(x.computeIfAbsent("122", (k)->fac2));
		System.out.println(x.get("123"));
		System.out.println(x.get("122"));*/
		
		
		TreeMap<Integer,List<String>> depthToVar = new TreeMap<>();
		Arrays.asList((Stream.of("root").toArray(String[]::new)));
		depthToVar.put(0, Arrays.asList((Stream.of("root").toArray(String[]::new))));
		depthToVar.put(1, Arrays.asList((Stream.of("A","B").toArray(String[]::new))));
		depthToVar.put(2, Arrays.asList((Stream.of("C","E").toArray(String[]::new))));
		/*depthToVar.computeIfPresent(2, (k,v)-> {
			v.add("E");
			return v;
		});*/
		depthToVar.computeIfAbsent(2, (k)->Arrays.asList((Stream.of("D").toArray(String[]::new))));
		
		Set<Integer> keys = depthToVar.keySet();
		for(int key : keys) {
			List<String> x = depthToVar.get(key);
			System.out.println(x);
		}
		String x = "asfasfz";
		System.out.println(x.substring(0, x.length()-1));
	}

}
