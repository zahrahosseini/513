/*
 * 
 */
package dc.aap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

public class StandaloneMain {
	
	/**
	 * Write in file.
	 *
	 * @param FileName
	 *            the file name
	 * @param Output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void writeinFile(String FileName, String Output) throws IOException {
		FileWriter fw = new FileWriter(FileName);

		fw.write(Output);

		fw.close();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		File someClassFile = new File("./bin/").getAbsoluteFile();
		soot.options.Options.v().set_keep_line_number(true);
		soot.options.Options.v().setPhaseOption("jb", "use-original-names:true");
		soot.options.Options.v().setPhaseOption("cg", "verbose:true");
		Scene.v().setSootClassPath(Scene.v().getSootClassPath() + File.pathSeparator + someClassFile);
		String ClassName = "TestCase.Elevator";
		SootClass c = Scene.v().loadClassAndSupport(ClassName);
		c.setApplicationClass();
		Scene.v().loadNecessaryClasses();

		List<SootMethod> m1 = c.getMethods();
		LineCounter LOC = new LineCounter();
		String LocationoftestFile = "src\\TestCase\\Elevator.java";
		StringBuilder Output1 = new StringBuilder();
		StringBuilder Output2 = new StringBuilder();
		;
		Output1.append("LOC" + LOC.LOC(LocationoftestFile) + "\n");

		for (int iterator = 0; iterator < c.getMethodCount(); iterator++) {
			long startTime = System.currentTimeMillis();

			SootMethod m = m1.get(iterator); // c.getMethodByName("entryPoint");
			Body b = m.retrieveActiveBody();

			UnitGraph g = new BriefUnitGraph(b);
			Anderson_Alg x = new Anderson_Alg(g);

			Output1.append("Function name:" + m.getName() + ", " + "AVG:" + Float.toString(x.Get_AVG()) + ", ");

			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			Output1.append("TIME:" + totalTime + "ms" + "\n");
			Output2.append("Method:" + m.getName() + "\n");
			Output2.append(x.P2S(LocationoftestFile));
		}
		writeinFile("output1.txt", Output1.toString());
		writeinFile("output2.txt", Output2.toString());

		System.out.println(Output2.toString());
		System.out.println(Output1.toString());
	}
}
