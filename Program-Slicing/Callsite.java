package dc.aap;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.G;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.options.Options;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.toolkits.scalar.UnitValueBoxPair;

/**
 *
 * @author Zahra Hosseini
 **/
public class Callsite {
	protected Map<Unit, List> unitToGuaranteedDefs;
	public Map<Unit, Set<Unit>> Gmap = new HashMap<Unit, Set<Unit>>();
	public FlowSet<Value> Wlist = new ArraySparseSet();

	public void putinMap(Unit s, Unit t) {
		if (Gmap.containsKey(s)) {
			Set<Unit> temp = Gmap.get(s);
			temp.add(t);
			Gmap.put(s, temp);
			System.out.println("LL" + Gmap);
		} else {
			Set<Unit> temp = new HashSet<Unit>();
			temp.add(t);
			Gmap.put(s, temp);
			System.out.println("LL" + Gmap);
		}

	}

	public void DD(UnitGraph graph) {
		SimpleLiveLocals s = new SimpleLiveLocals(graph);

		Iterator<Unit> gIt = graph.iterator();

		HashMap<Value, HashSet<DUPair>> dupairs = new HashMap<Value, HashSet<DUPair>>();
		// generate du-pairs
		while (gIt.hasNext()) {
			Unit defUnit = gIt.next();

			SmartLocalDefs des = new SmartLocalDefs(graph, s);

			SimpleLocalUses uses = new SimpleLocalUses(graph, des);

			List<UnitValueBoxPair> ul = uses.getUsesOf(defUnit);
			if (ul != null && ul.size() != 0) {
				for (UnitValueBoxPair vbp : ul) {
					Value defVariable = vbp.getValueBox().getValue();
					HashSet<DUPair> dupairList = dupairs.get(defVariable);
					if (dupairList == null) {
						dupairList = new HashSet<DUPair>();
					}
					Unit useUnit = vbp.getUnit();
					dupairList.add(new DUPair(defUnit, useUnit));
					dupairs.put(defVariable, dupairList);
					System.out.println(defUnit.toString() + useUnit.toString());
					putinMap(defUnit, useUnit);
				}
			}
		}
	}

	public Callsite(UnitGraph graph) {
		if (Options.v().verbose())
			G.v().out.println("[" + graph.getBody().getMethod().getName() + "]     Constructing GuaranteedDefs...");

		// GuaranteedDefsAnalysis analysis = new GuaranteedDefsAnalysis(graph);

		// build map
		{

			// unitToGuaranteedDefs = new HashMap<Unit, List>(graph.size() * 2 +
			// 1, 0.7f);
			Iterator unitIt = graph.iterator();
			boolean flag = false;
			Unit Root;
			DominatorsFinder df = new MHGPostDominatorsFinder<>(graph);
			// mhgPostDominatorsFinder.getImmediateDominator(unit)
			Unit root = null;
			while (unitIt.hasNext()) {
				Unit s = (Unit) unitIt.next();
				// Detect Control Flow Graph

				List<Unit> pred = graph.getPredsOf(s);
				List<Unit> succ = graph.getSuccsOf(s);

				if (pred.isEmpty()) {
					System.out.println("E" + s);
					root = s;
				} else

				//////
				{
					System.out.println("pred::" + pred + "State:" + s + "Succ:" + succ + "PRED(p)"
							+ df.getImmediateDominator(pred.get(0)));
					if (pred.size() > 1) {
						boolean f = false;
						System.out.println("HERE");
						for (int i = 0; i < pred.size(); i++) {
							System.out.println("CCC" + df.getImmediateDominator(pred.get(i)) + s.toString());

							if (!df.getImmediateDominator(pred.get(i)).toString().contains(s.toString()))
								f = true;
							// System.out.println("MContainE" + s);
							if (!f) {
								putinMap(root, s);
								System.out.println("E" + s);
							}
						}
					} else if (pred.size() == 1) {
						System.out.println("PRED" + df.getImmediateDominator(pred.get(0)));
						if (df.getImmediateDominator(pred.get(0)) != null) {
						} else {
							putinMap(root, s);
							System.out.println("nullE" + s);
							continue;
						}
						if (df.getImmediateDominator(pred.get(0)) != null) {
							System.out.println("CCC" + df.getImmediateDominator(pred.get(0)) + s.toString());
							if (df.getImmediateDominator(pred.get(0)).toString().contains(s.toString())) {
								putinMap(root, s);
								System.out.println("ContainE" + s);
							} else {
								putinMap(pred.get(0), s);
								System.out.println("ADD" + pred + "State:" + s);

							}
						}
					}
				}
				///////
				// if(graph.getPredsOf(s).size()>1)
				// flag=false;
				// if(flag)
				// System.out.println(s+"XX"+graph.getSuccsOf(s)+graph.getPredsOf(s).size());
				// if(graph.getSuccsOf(s).size()>1){
				// flag=true;
				//
				// }

				// FlowSet set = (FlowSet) analysis.getFlowBefore(s);
				// unitToGuaranteedDefs.put(s,
				// Collections.unmodifiableList(set.toList()));

			}
		}
	}

	public boolean iscontain(Set<Unit> myset, String s) {
		List<Unit> LWlist = new ArrayList<Unit>(myset);

		for (int i = 0; i < LWlist.size(); i++) {
			if (LWlist.get(i).toString().contains(s))
				return true;
		}
		return false;
	}

	public Set<String> Backward(String s) {
		Set<String> Wlist = new HashSet<String>();
		Set<String> Visited = new HashSet<String>();
		Wlist.add(s);
		Visited.add(s);
		while (!Wlist.isEmpty()) {

			List<String> LWlist = new ArrayList<String>(Wlist);
			String Should_propagated = LWlist.get(0);
			Wlist.remove(Should_propagated);
			Iterator iterator = Gmap.keySet().iterator();

			while (iterator.hasNext()) {

				Unit key = (Unit) iterator.next();
				Set<Unit> value = Gmap.get(key);
				// System.out.println(key+value.toString());

				if (iscontain(value, Should_propagated)) {
					// System.out.println(key + " " + value);
					if (!Visited.contains(key.toString())) {
						Wlist.add(key.toString());
						Visited.add(key.toString());
					}
				}
			}
		}
		System.out.println("VIS" + Visited);
		try {
			Writefile("backward.txt", Visited.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Visited;
	}

	public Set<String> Forward(String s) {

		Set<String> Wlist = new HashSet<String>();
		Set<String> Visited = new HashSet<String>();
		Wlist.add(s);
		Visited.add(s);
		while (!Wlist.isEmpty()) {
			//System.out.println("Wlist:"+Wlist);
			List<String> LWlist = new ArrayList<String>(Wlist);
			String Should_propagated = LWlist.get(0);
			Wlist.remove(Should_propagated);
			Iterator iterator = Gmap.keySet().iterator();
			while (iterator.hasNext()) {
				Unit key = (Unit) iterator.next();
				Set<Unit> value = Gmap.get(key);
				// System.out.println(key+value.toString());
				if (key.toString().contains(Should_propagated)) {
					// add two list
					Set<Unit> Set2 = Gmap.get(key);
					List<Unit> LWlist1 = new ArrayList<Unit>(Set2);

					for (int i = 0; i < LWlist1.size(); i++) {
						if (!Visited.contains(LWlist1.get(i).toString())) {
							Visited.add(LWlist1.get(i).toString());
							Wlist.add(LWlist1.get(i).toString());
							//System.out.println("HERE"+Wlist);
							
							
						}
					}

				}

			}
		}
		
		System.out.println("VIS" + Visited);
		try {
			Writefile("forward.txt", Visited.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Visited;
	}
	public Set<String> chopping(String x, String y){
		Set<String> fVisited = new HashSet<String>();
		Set<String> bVisited = new HashSet<String>();
		fVisited=Forward(x);
		bVisited=Backward(y);
		System.out.println(fVisited.toString()+"&&"+bVisited.toString());
		Set<String> intersection = new HashSet<String>(fVisited);
		intersection.retainAll(bVisited);
		try {
			Writefile("chopping.txt", intersection.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return intersection;
	}
	public void Writefile(String name, String con) throws Exception{
		PrintWriter writer = new PrintWriter(name, "UTF-8");
		writer.println(con);
		writer.close();
	}
	/**
	 * Returns a list of locals guaranteed to be defined at (just before)
	 * program point <tt>s</tt>.
	 **/
	public List getGuaranteedDefs(Unit s) {
		return unitToGuaranteedDefs.get(s);
	}
}

/**
 * Flow analysis to determine all locals guaranteed to be defined at a given
 * program point.
 **/
class MyUnit {
	public String def;
	public Set<String> use;
	public int lineno;
}

class MyCallsite extends ForwardFlowAnalysis {
	FlowSet emptySet = new ArraySparseSet();
	Map<Unit, FlowSet> unitToGenerateSet;
	public Map<Value, FlowSet<Value>> GTree = new HashMap<>();
	public Map<Value, Integer> Var_id = new HashMap();
	public Map<Value, FlowSet<Integer>> PointsTo;
	public Map<Value, Integer> LineMap = new HashMap();
	private Map<Value, FlowSet<Integer>> LineRed = new HashMap<>();
	public Map<MyUnit, Set<MyUnit>> resourcesByID = new HashMap<MyUnit, Set<MyUnit>>();

	MyCallsite(UnitGraph graph) {
		super(graph);
		DominatorsFinder df = new MHGDominatorsFinder(graph);
		unitToGenerateSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1, 0.7f);
		PointsTo = new HashMap<>();
		// pre-compute generate sets
		MyUnit Root = new MyUnit();
		Root.def = "Root";

		for (Iterator unitIt = graph.iterator(); unitIt.hasNext();) {
			Unit s = (Unit) unitIt.next();
			System.out.println(
					s + "XX" + s.getJavaSourceStartLineNumber() + "XX" + s.getDefBoxes() + "==" + s.getUseBoxes());

			FlowSet genSet = emptySet.clone();
			// System.out.println(s.getJavaSourceStartLineNumber()+"#"+s+"&&&"+s.branches()+"**"+
			// df.getImmediateDominator(s));
			// System.out.println(s.getDefBoxes()+"XX"+s.getUseBoxes());
			// if (df.getImmediateDominator(s)!=null)
			// System.out.println(df.getImmediateDominator(s).toString().indexOf("if"));
			// if(!resourcesByID.containsKey(s)&& !s.toString().contains("if"))
			// {
			// FlowSet<Unit> Tempset = new ArraySparseSet();
			// Tempset.add(s);
			// resourcesByID.put(E, Tempset);
			// System.out.println("000");
			// }
			// else
			// {
			// FlowSet<Unit> Tempset = new ArraySparseSet();
			// Tempset.union(resourcesByID.get(s));
			// resourcesByID.get(s).add(genSet);
			// }
			System.out.println(s + "XX" + s.getDefBoxes() + "&&" + s.getUseBoxes());
			for (Iterator domsIt = df.getDominators(s).iterator(); domsIt.hasNext();) {
				Unit dom = (Unit) domsIt.next();
				// System.out.println(s+"XX"+df.getImmediateDominator(s));
				// System.out.println("XXX"+dom.getJavaSourceStartLineNumber()+"XXX"+dom.toString());
				// System.out.println("###"+dom.getDefBoxes()+"###"+dom.getUseBoxes());
				if (dom instanceof IdentityStmt) {
					processParameterDeclarationStmt(dom);
				} else if (dom instanceof AssignStmt) {
					processAssignmentStmt(dom);

				}
				// else if (dom instanceof InvokeExpr) {
				// Body b = invoke.getMethod().retrieveActiveBody(); }
				//
				// for(Iterator boxIt = dom.getDefBoxes().iterator();
				// boxIt.hasNext();){
				// ValueBox box = (ValueBox) boxIt.next();
				// if(box.getValue() instanceof Local)
				// genSet.add(box.getValue(), genSet);
				// }
			}

			// unitToGenerateSet.put(s, genSet);
		}

		doAnalysis();
	}

	private void processParameterDeclarationStmt(Unit dom) {
		// i0 := @parameter0: int;
		// When he names the function parameters.
		// Def: JimpleLocalBox(i0), Use: IdentityRefBox(@parameter0: int)
		// I think only interested in Def.
		String formalParam = dom.getDefBoxes().get(0).getValue().toString();
		String paramNode = "parameter_" + formalParam;
		// out_flow.p_nodes.add(paramNode);
		Set<String> s = new HashSet<String>();
		s.add(paramNode);
		// out_flow.locals.put(formalParam, s);
		// System.out.println(d);
	}

	private void processAssignmentStmt(Unit dom) {
		if (dom.getUseBoxes().isEmpty() || dom.getDefBoxes().isEmpty())
			return;

		boolean rightField = false;
		boolean leftField = false;

		for (ValueBox i : dom.getUseBoxes()) {
			if (i.getValue() instanceof FieldRef) {
				rightField = true;
			}
		}

		for (ValueBox i : dom.getDefBoxes()) {
			if (i.getValue() instanceof FieldRef) {
				leftField = true;
			}
		}

		Value expr = dom.getUseBoxes().get(0).getValue();
		if (expr instanceof AnyNewExpr) {
			processNewAssignment(dom);
		} else if (expr instanceof InvokeExpr) {
			// processInvocationAssignment(d, (InvokeExpr) expr, out_flow);
		} else if (leftField && rightField) {
			processReferenceToReferenceAssignment(dom);
		} else if (leftField) {
			// System.out.println("F2RL"+dom.toString());
			processFieldToReferenceOrLiteralAssignment(dom);
			// System.out.println(GTree.toString());
		} else if (rightField) {
			// System.out.println("F2R"+dom.toString());

			processFieldToReferenceAssignment(dom);
			// System.out.println(GTree.toString());

		} else if (!leftField && !rightField) {
			processReferenceToReferenceAssignment(dom);
		}
		// System.out.println(dom+
		// Integer.toString(dom.getJavaSourceStartLineNumber()));
		// System.out.println(dom);
		// getPointto();

	}

	private void processFieldToReferenceAssignment(Unit dom) {

		// It is an instruction of the form: x = y.f
		Value def = dom.getDefBoxes().get(0).getValue();

		Value use = dom.getUseBoxes().get(0).getValue();
		// System.out.println("%%%%"+def.toString()+"==="+use.toString());

		String x = def.toString();
		// String y = use.getUseBoxes().get(0).getValue().toString();
		String f = getFieldNameForExpr(use);

		// Delete whatever you have x
		// out.locals.put(x, new HashSet<String>());

		// Ln order to "free"..
		// String ln = "l_" + IdGenerator.GenerateId() + "_" + y;
		// System.out.println("processFieldToReferenceAssignment*" + def + "*=*"
		// + use+"**"+y);
		// System.out.println("processFieldToReferenceAssignment" +
		// def.toString() + "=" + use.toString());

		// R' = R U { (n,f,ln) | n in L(y) }

		// L'(x) = { ln }

		CreateGtree(def, use);

		Add(def, use, false, dom.getJavaSourceStartLineNumber());
		// System.out.println("PointsTo"+PointsTo);

	}

	public float Get_AVG() {
		Iterator<Map.Entry<Value, FlowSet<Integer>>> entries = PointsTo.entrySet().iterator();
		int sum = 0;
		int k = 0;
		while (entries.hasNext()) {

			Map.Entry<Value, FlowSet<Integer>> entry = entries.next();
			if (entry.getValue().size() > 0) {
				sum += entry.getValue().size();
				k++;
			}
			// System.out.println("Key = " + entry.getKey() + ", Value = " +
			// entry.getValue());
		}
		return (float) sum / k;
	}

	public Value GiveWhichVar(int id) {
		// TODO
		Iterator<Map.Entry<Value, Integer>> entries = Var_id.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Value, Integer> entry = entries.next();
			if (entry.getValue().intValue() == id) {
				return entry.getKey();
			}
		}
		return null;

	}

	public String P2S(String FileName) {
		Iterator<Map.Entry<Value, Integer>> entries = LineMap.entrySet().iterator();
		// String Answer="";
		StringBuilder value = new StringBuilder();
		while (entries.hasNext()) {
			// Answer.concat(FileName);
			Map.Entry<Value, Integer> entry = entries.next();
			// Answer.concat(FileName+" " +entry.getValue().toString()+"
			// "+entry.getKey().toString()+" "+"(");
			value.append(FileName + " " + entry.getValue().toString() + " " + entry.getKey().toString() + " " + "(");
			// System.out.print(FileName+" " +entry.getValue().toString()+"
			// "+entry.getKey().toString()+" "+"(");
			// Answer.concat(entry.getValue().toString()+"
			// "+entry.getKey().toString()+" "+"(");
			String Temp = entry.getKey().toString();
			FlowSet<Integer> v = GetValueOF(entry.getKey());
			List<Integer> LV = v.toList();
			// System.out.print(LV.toString());
			// Map<String, Character> myNewHashMap = new HashMap<>();
			// for(Map.Entry<Character, String> entry : myHashMap.entrySet()){
			// myNewHashMap.put(entry.getValue(), entry.getKey());
			// }
			for (int i = 0; i < v.size(); i++) {
				// System.out.print(GiveWhichVar(LV.get(i).intValue())+" ");
				// Answer.concat(GiveWhichVar(LV.get(i).intValue())+" ");
				value.append(GiveWhichVar(LV.get(i).intValue()) + " ");
				// Answer.concat(GiveWhichVar(LV.get(i))+",");
			}
			// Answer.concat(")\n");
			value.append(")\n");
			// System.out.println(")\n");

		}
		return value.toString();
	}

	private void processFieldToReferenceOrLiteralAssignment(Unit dom) {
		// System.out.println(PointsTo.toString());

		// It is an instruction of the form: x = y
		Value def = dom.getDefBoxes().get(0).getValue();

		Value use = null;
		int count = dom.getDefBoxes().size();
		// System.out.println("&&"+count);
		if ((Integer) count >= 1) {
			use = dom.getUseBoxes().get(count - 1).getValue(); // OJO! here is
																// access to
																// the second
																// elem.
		} else if (count < 1)
			use = dom.getUseBoxes().get(0).getValue();

		// System.out.println(def.toString()+"=="+use);
		// Value Myuse=dom.getUseBoxes().get(0).getValue();
		// String x = def.getUseBoxes().get(0).getValue().toString();
		// String y = use.toString();

		String f = getFieldNameForExpr(def);

		// System.out.println("processFieldToReferenceOrLiteralAssignment*" +
		// def + "*=*" + use+"^^"+x+f);
		// System.out.println("^^^^"+def.toString()+"==="+use.toString());
		CreateGtree(def, use);
		// System.out.println(GTree.toString());
		// System.out.println("processFieldToReferenceOrLiteralAssignment" +
		// def.toString() + use.toString());
		Add(def, use, false, dom.getJavaSourceStartLineNumber());
		// System.out.println("**PointsTo"+PointsTo);
		// }

	}

	private String getFieldNameForExpr(Value fieldAccessExpr) {

		String[] f_splice = fieldAccessExpr.toString().split(" +");
		String f = f_splice[f_splice.length - 1];
		f = f.substring(0, f.length() - 1);
		return f;

	}

	private void processReferenceToReferenceAssignment(Unit dom) {
		// TODO Auto-generated method stub
		// We have to do L'(left) = L(right)
		// And as put () override and was left if this key, no need to delete
		Value def = dom.getDefBoxes().get(0).getValue();
		Value use = dom.getUseBoxes().get(0).getValue();
		// out.locals.put(def.toString(), out.locals.get(use.toString()));
		// System.out.println("processReferenceToReferenceAssignment*" + def +
		// "*=*" + use);
		// System.out.println("**" + def);
		// System.out.println("&&&"+def.toString()+"==="+use.toString());
		CreateGtree(def, use);
		Add(def, use, false, dom.getJavaSourceStartLineNumber());

		// FlowSet<Integer> r = GetValueOF(def);
		// FlowSet<Integer> s=null;
		//// if (r.isEmpty()) {
		//// int vid = (Integer) Var_id.get(use);
		//// r.add(Integer.toString(vid));
		//// PointsTo.put(def, r);
		//// } else {
		//
		// s= GetValueOF(use);
		//
		//// else
		//// {
		//// s.add(Var_id.get(use));
		//// }
		// //}
		// if (!s.isSubSet(r)) {
		//
		// r.union(s);
		// PointsTo.put(def, r);
		// /////
		// FlowSet<Value> temp = null;
		//
		// if (WorkList.get(def)!=null)
		// temp=WorkList.get(def);
		// temp.add(use);
		// WorkList.put(def, temp);
		// /////
		// System.out.println("NO");
		// }

		// System.out.println("PointsTo"+PointsTo);

	}

	private void CreateGtree(Value def, Value use) {
		// System.out.println("VVVV"+def.toString()+"=="+use.toString());
		FlowSet<Value> Tempset = new ArraySparseSet();
		Tempset.add(def);
		FlowSet<Value> prevSet = GTree.get(use);
		Tempset.union(prevSet);
		GTree.put(use, Tempset);
		if (!PointsTo.containsKey(def)) {
			FlowSet<Integer> t = new ArraySparseSet();
			PointsTo.put(def, t);
		}
		// System.out.println("INCGraph" + GTree);

	}

	private void processNewAssignment(Unit dom) {
		Value right = dom.getUseBoxes().get(0).getValue();
		Value left = dom.getDefBoxes().get(0).getValue();

		// The node name is of the form: A_p
		// String vName = right.getType() + "_" +
		// d.getJavaSourceStartLineNumber();
		// out.nodes.add(vName);
		// The instruction is of the type:
		// p: x = new A;
		// Then we put on the map of locals:
		// x --> {A_p}
		// Set<String> vNameSet = new HashSet<String>();
		// vNameSet.add(vName);

		// out.locals.put(left.toString(), vNameSet);
		// System.out.println("processNewAssignment*" + left + "*=*" + right);
		// System.out.println("*-*" + vName);
		// System.out.println("--" + left + "--");
		// if(dom.toString().contains("[")&&(PointsTo.get(left)!=null)){}
		Add(left, right, true, dom.getJavaSourceStartLineNumber());

	}

	private boolean GetValueofLine(int line, Value L) {
		boolean ret = true;
		Iterator<Map.Entry<Value, FlowSet<Integer>>> entries = LineRed.entrySet().iterator();
		// System.out.println("LOB"+LineRed.toString());

		while (entries.hasNext()) {
			Map.Entry<Value, FlowSet<Integer>> entry = entries.next();
			if ((entry.getKey() == L) && (entry.getValue().contains(line))) {
				return false;
			} else if ((entry.getKey() == L) && (!entry.getValue().contains(line))) {
				return true;
			}

		}
		return ret;

	}

	private void Add(Value L, Value R, boolean IsNew, int LineNumber) {
		// System.out.println("^0^"+L.toString()+"$$$"+R.toString());

		// System.out.println("^1^"+R.toString()+LineNumber);

		if (GetValueofLine(LineNumber, L)) {
			// System.out.println("^2^"+R.toString());

			// TODO
			// System.out.println("IN");
			FlowSet<Integer> R_F = new ArraySparseSet();
			if (IsNew) {
				// get new ID and add it to list of Var-id
				int idgen = IDGen.GID();
				// System.out.println(idgen);
				Var_id.put(L, idgen);
				Wlist.add(L);
				// we should integrate (the new id) with previous point-to and
				// add
				// that to list
				// of point-to .

				FlowSet<Integer> Tset = new ArraySparseSet();
				if (GetValueOF(L) != null)
					Tset.union(GetValueOF(L));
				FlowSet<Integer> GetFromVar_id = new ArraySparseSet();
				// System.out.println(Var_id.get(L));
				GetFromVar_id.add(Var_id.get(L));
				Tset.union(GetFromVar_id);
				PointsTo.put(L, Tset);
				if (LineRed.isEmpty())
					LineRed = new HashMap<Value, FlowSet<Integer>>();
				if (LineRed.get(L) != null) {
					LineRed.get(L).add(LineNumber);
					LineRed.put(L, LineRed.get(L));
				} else {
					FlowSet<Integer> T1 = new ArraySparseSet();
					T1.add(LineNumber);
					LineRed.put(L, T1);
				}

				LineMap.put(L, LineNumber);
				while (!Wlist.isEmpty()) {
					List<Value> LWlist = Wlist.toList();
					Value Should_propagated = LWlist.get(0);
					Wlist.remove(Should_propagated);
					Propagation(Should_propagated);
				}
			} else {
				// System.out.println("^3^"+L.toString()+"="+R.toString());

				// System.out.println("IN2");
				if (GetValueOF(R) != null)
					R_F = GetValueOF(R);
				// construct Gtree based on assignment.
				FlowSet TempGEtR = new ArraySparseSet();
				// System.out.println("R"+R);
				// System.out.println("R_F"+GetValueOF(R));
				// System.out.println("TEMPGR"+TempGEtR);
				if (GetValueOF(R) != null)
					TempGEtR.union(R_F);
				GTree.put(R, TempGEtR);
				// System.out.println("RHS:"+R_F);
				// System.out.println("LHS:"+GetValueOF(L).toString());
				// System.out.println("##"+L.toString()+R.toString());
				if (LineRed.isEmpty()) {
					LineRed = new HashMap<Value, FlowSet<Integer>>();
				}

				if (LineRed.get(L) != null) {
					LineRed.get(L).add(LineNumber);
					LineRed.put(L, LineRed.get(L));
				} else {
					FlowSet<Integer> T1 = new ArraySparseSet();
					T1.add(LineNumber);
					LineRed.put(L, T1);
				}
				// integrate Right-flow with LHS and insert it to point-to list.
				// System.out.println("RHS"+R_F);
				// System.out.println("LHS"+GetValueOF(L));
				// System.out.println("RRRRR_FFFF"+R_F.toString()+"XXXX"+R.toString()+"MMMM"+GetValueOF(R));
				if (R_F.size() > 0)
					if (!(GetValueOF(L)).isSubSet(R_F)) {
						// System.out.println("ADD");
						FlowSet<Integer> I = new ArraySparseSet<>();
						I.union(R_F);
						I.union(GetValueOF(L));

						PointsTo.put(L, I);

						LineMap.put(L, LineNumber);
						Wlist.add(L);
					}
				// System.out.println("WLIST" + Wlist.toString());
				while (Wlist.size() > 0) {
					// System.out.println("In Propagation");

					List<Value> LWlist = Wlist.toList();
					Value Should_propagated = LWlist.get(0);
					Wlist.remove(Should_propagated);
					Propagation(Should_propagated);
				}
			}
			// System.out.println("XXX");
			// // System.out.println("**__**");
			// if (!R_F.isEmpty()) {
			// //if right has something new for us
			// //
			// if (!R_F.isSubSet(GetValueOF(L))) {
			// // System.out.println("**__**");
			//
			// GetValueOF(L).union(R_F);
			// PointsTo.put(L, GetValueOF(L));
			// Wlist.add(L);
			// // System.out.println("**_1_**");
			//
			// // Vlist.add(v);
			// Propagation(L);
			// }
			// }
			// else
			// {
			// FlowSet<Integer> Tset = new ArraySparseSet();
			// Tset.union(GetValueOF(L));
			// FlowSet<Integer> GetFromVar_id = new ArraySparseSet();
			// GetFromVar_id.add(Var_id.get(L));
			// Tset.union(GetFromVar_id);
			// PointsTo.put(L, Tset);
			//
			// }

		}

	}

	private FlowSet<Integer> GetValueOF(Value v) {
		// System.out.println("PointsTo"+PointsTo);

		// System.out.println("GET:::"+v.toString()+PointsTo.get(v));
		Iterator<Map.Entry<Value, FlowSet<Integer>>> entries = PointsTo.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Value, FlowSet<Integer>> entry = entries.next();
			if (entry.getKey().toString().contains(v.toString()))
				return entry.getValue();
		}
		return null;
		// return PointsTo.get(v);
	}

	public void getPointto() {
		System.out.println(PointsTo);
	}

	FlowSet<Value> Wlist = new ArraySparseSet();

	private void Propagation(Value v) {
		// Vlist.remove(v);
		// Wlist.remove(v);

		FlowSet<Value> RelatedTo = GTree.get(v);

		// System.out.println("R2" + GTree.toString());
		if (RelatedTo != null) {
			for (Value i : RelatedTo) {
				if (!GetValueOF(v).isSubSet(GetValueOF(i))) {
					GetValueOF(i).union(GetValueOF(v));
					PointsTo.put(i, GetValueOF(i));
					Wlist.add(i);

				}

			}
		}
	}

	/**
	 * All INs are initialized to the empty set.
	 **/
	@Override
	protected Object newInitialFlow() {
		return emptySet.clone();
	}

	/**
	 * IN(Start) is the empty set
	 **/
	@Override
	protected Object entryInitialFlow() {
		return emptySet.clone();
	}

	/**
	 * OUT is the same as IN plus the genSet.
	 **/
	@Override
	protected void flowThrough(Object inValue, Object unit, Object outValue) {
		FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;

		// perform generation (kill set is empty)
		in.union(unitToGenerateSet.get(unit), out);
	}

	/**
	 * All paths == Intersection.
	 **/
	@Override
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;

		inSet1.intersection(inSet2, outSet);
	}

	@Override
	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

		sourceSet.copy(destSet);
	}
}