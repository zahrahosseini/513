package CallGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

public class CHA {
	public static AtlasSet<GraphElement> calculateSuper(GraphElement callsite, Q typesToSearch) {
		Q Contains = Common.universe().edgesTaggedWithAny(XCSG.Contains);
		Q TypeEdges = Common.universe().edgesTaggedWithAny(XCSG.TypeOf);
		Q Hierachy = Common.universe().edgesTaggedWithAny(XCSG.Supertype);
		Q candidateMethods = Contains.forwardStep(typesToSearch).nodesTaggedWithAny(XCSG.Method);
		String methodName = callsite.getAttr(XCSG.name).toString();
		methodName = methodName.substring(0, methodName.indexOf("("));
		Q matchingMethods = candidateMethods.selectNode(XCSG.name, methodName);
		AtlasSet<GraphElement> result = new AtlasHashSet<GraphElement>();

		for (GraphElement matchingMethod : matchingMethods.eval().nodes()) {

			result.add(matchingMethod);

		}

		return result;
	}

	private static AtlasSet<GraphElement> getDynamicDispatches(AtlasSet<GraphElement> callsites) {
		return Common.toQ(callsites).nodesTaggedWithAny(XCSG.DynamicDispatchCallSite).eval().nodes();
	}

	public static void WriteInFile() throws IOException {

		AtlasSet<GraphElement> callsites = Common.universe().nodesTaggedWithAny(XCSG.CallSite).eval().nodes();
		Q cha = Common.universe().edgesTaggedWithAll("CHA").retainEdges();
		AtlasSet<GraphElement> callsitecha = Common.universe().nodesTaggedWithAny("CHA").retainEdges().eval().nodes();

		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		float average = 0;
		long sizes = Common.toQ(callsites).nodesTaggedWithAny(XCSG.DynamicDispatchCallSite).eval().nodes().size();
		Set Address_set = new HashSet();
		for (GraphElement Dcallsite : Common.toQ(callsites).nodesTaggedWithAny(XCSG.DynamicDispatchCallSite).eval()
				.nodes()) {
			Address_set.add(Dcallsite.getAttr(XCSG.sourceCorrespondence));

		}
		
		for (GraphElement mcallsite : cha.eval().nodes()) {
			long dispatches =0;
			dispatches=cha.successors(Common.toQ(mcallsite)).eval().nodes().size();
			if(mcallsite.taggedWith(XCSG.InstanceMethod)|| mcallsite.taggedWith(XCSG.Constructor))
				if(dispatches<1)
				dispatches=1;
			
			if (dispatches < min) {
				min = dispatches;
			}
			if ((int)dispatches > (int)max) {
				max = dispatches;
			}
			average += dispatches;
		}
		average = ((float) average / (float) cha.eval().nodes().size());
		PrintWriter pw = new PrintWriter(
				new FileWriter("/Users/zh/Desktop/ws/toolbox.analysis/src/CallGraph/output.txt"));

		pw.write(Common.universe().edgesTaggedWithAll("CHA").retainEdges().eval().nodes().size() + ","
				+ Common.universe().edgesTaggedWithAll("CHA").retainEdges().eval().edges().size() + ","
				+ callsites.size() + ","
				+ Common.toQ(callsites).nodesTaggedWithAny(XCSG.DynamicDispatchCallSite).eval().nodes().size()
				+ Address_set + "," + min + "," + max + "," + Float.toString(average) +"\n");

		pw.close();

	}

	public static void Run(boolean GoThrough) {
		AtlasSet<GraphElement> methods = Common.universe().nodesTaggedWithAll(XCSG.Method).eval().nodes();
		Q contains = Common.universe().edgesTaggedWithAll(XCSG.Contains);

		for (GraphElement method : methods) {
			// Q Temp = Common.toQ(method);
			Q contents = contains.forward(Common.toQ(method));
			AtlasSet<GraphElement> Callsites = contents.nodesTaggedWithAny(XCSG.CallSite).eval().nodes();

			for (GraphElement callsite : Callsites) {
				if (callsite.taggedWith(XCSG.StaticDispatchCallSite)) {

					String name = callsite.getAttr(XCSG.name).toString();
					for (GraphElement Targetmethod : methods) {
						String Targetname = Targetmethod.getAttr(XCSG.name).toString();

						if (name.contains(Targetname) && !(Targetmethod.taggedWith(XCSG.Constructor)
								&& method.taggedWith(XCSG.Constructor))) {
							Q Calledges = Common.universe().edgesTaggedWithAll("CHA");

							if (Calledges.betweenStep(Common.toQ(method), Common.toQ(Targetmethod)).eval().edges()
									.isEmpty()) {
								GraphElement Calledge = Graph.U.createEdge(method, Targetmethod);
								Calledge.tag("CHA");
							}
						}
					}

				} else if (callsite.taggedWith(XCSG.DynamicDispatchCallSite)) {
					Q IdentityEdges = Common.universe().edgesTaggedWithAll(XCSG.IdentityPassedTo);
					Q Thisnode = IdentityEdges.predecessors(Common.toQ(callsite));// send
																					// callsite
																					// to
																					// Identity
					Q Dataedges = Common.universe().edgesTaggedWithAll(XCSG.DataFlow_Edge);
					Q CallVariable = Dataedges.predecessors(Thisnode);// send
																		// callsite
																		// to
																		// Identity
					Q TypeEdges = Common.universe().edgesTaggedWithAll(XCSG.TypeOf);
					Q DeclaredType = TypeEdges.successors(CallVariable);// send
																		// callsite
																		// to
																		// Identity
					Q Hierachy = Common.universe().edgesTaggedWithAll(XCSG.Supertype);
					Q SuperDeclaredType = Hierachy.forward(DeclaredType);
					Q SubDeclaredType = Hierachy.reverse(DeclaredType);
					Q SubtypeMethods = contains
							.forward(SubDeclaredType.union(Common.toQ(calculateSuper(callsite, SubDeclaredType))))
							.nodesTaggedWithAll(XCSG.Method);

					String name = callsite.getAttr(XCSG.name).toString();
					if (name.indexOf("(") > -1)
						name = name.substring(0, name.indexOf("("));
					for (GraphElement Targetmethod : SubtypeMethods.eval().nodes()) {
						String Targetname = Targetmethod.getAttr(XCSG.name).toString();
						if (Targetname.indexOf("(") > -1)
							Targetname = Targetname.substring(0, Targetname.indexOf("("));
						if (!GoThrough) {
							if (name.equals(Targetname) && (!(Targetmethod.taggedWith(XCSG.InstanceMethod)
									&& method.taggedWith(XCSG.InstanceMethod)))) {
								Q Calledges = Common.universe().edgesTaggedWithAll("CHA");
								if (Calledges.betweenStep(Common.toQ(method), Common.toQ(Targetmethod)).eval().edges()
										.isEmpty()) {
									GraphElement Calledge = Graph.U.createEdge(method, Targetmethod);
									Calledge.tag("CHA");
								}
							}
						} else if (name.equals(Targetname)) {
							Q Calledges = Common.universe().edgesTaggedWithAll("CHA");
							if (Calledges.betweenStep(Common.toQ(method), Common.toQ(Targetmethod)).eval().edges()
									.isEmpty()) {
								GraphElement Calledge = Graph.U.createEdge(method, Targetmethod);
								Calledge.tag("CHA");
							}
						}
					}

				}
			}

		}

	}

}
