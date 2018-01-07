package dc.aap;

import java.util.*;

import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * The Class Anderson_Alg.
 */

class Anderson_Alg extends ForwardFlowAnalysis {
	/** Define empty set. */

	FlowSet emptySet = new ArraySparseSet();
	/**
	 * The G tree. GTree Shows how variable are connected to eachother Ex: a=b
	 * a=c b=c Gtree b a c a,b
	 */

	public Map<Value, FlowSet<Value>> GTree = new HashMap<>();
	/**
	 * The Var_id. Shows the id related to each variable
	 */

	public Map<Value, Integer> Var_id = new HashMap();
	/**
	 * The Points to. Create a Map of PointsTO set based on Anderson Alg
	 */

	public Map<Value, FlowSet<Integer>> PointsTo;
	/**
	 * The Line map. Create a Map between Variable and Line related to that.
	 * (Store "the final Line" which the variable has changed)
	 */

	public Map<Value, Integer> LineMap = new HashMap();
	/**
	 * The Line red. Create a Map between Variable and Line related to that.
	 * (Store "All Lines" which the variable has changed)
	 */

	private Map<Value, FlowSet<Integer>> LineRed = new HashMap<>();

	/**
	 * Instantiates a new anderson_ alg. The main Algorithm : First Step find
	 * Assignments
	 * 
	 * @param graph
	 *            the graph
	 */

	Anderson_Alg(UnitGraph graph) {
		super(graph);
		DominatorsFinder df = new MHGDominatorsFinder(graph);
		PointsTo = new HashMap<>();
		// pre-compute generate sets
		for (Iterator unitIt = graph.iterator(); unitIt.hasNext();) {
			Unit s = (Unit) unitIt.next();
			// Loop on every Unit

			FlowSet genSet = emptySet.clone();

			for (Iterator domsIt = df.getDominators(s).iterator(); domsIt.hasNext();) {
				Unit dom = (Unit) domsIt.next();
				// Find if the Unit is Identity or Assignment

				if (dom instanceof IdentityStmt) {
					processParameterDeclarationStmt(dom);
				} else if (dom instanceof AssignStmt) {
					processAssignmentStmt(dom);

				}

			}

		}

		doAnalysis();
	}

	/**
	 * Process parameter declaration stmt. if i0=@parameter_0 Def
	 * =i0,Use=@parameter0 DO NOTHING because its not a new assignment.
	 * 
	 * @param Unit
	 *            the dom
	 */

	private void processParameterDeclarationStmt(Unit dom) {

		String formalParam = dom.getDefBoxes().get(0).getValue().toString();
		String paramNode = "parameter_" + formalParam;

	}

	/**
	 * Process assignment stmt.
	 *
	 * @param Unit
	 *            the dom
	 */

	private void processAssignmentStmt(Unit dom) {
		if (dom.getUseBoxes().isEmpty() || dom.getDefBoxes().isEmpty())
			return;

		boolean rightField = false;
		boolean leftField = false;
		// Check If we have RHS

		for (ValueBox i : dom.getUseBoxes()) {
			if (i.getValue() instanceof FieldRef) {
				rightField = true;
			}
		}
		// Check If we have LHS

		for (ValueBox i : dom.getDefBoxes()) {
			if (i.getValue() instanceof FieldRef) {
				leftField = true;
			}
		}

		Value expr = dom.getUseBoxes().get(0).getValue();
		// Find Type of Statement

		if (expr instanceof AnyNewExpr) {
			// NEW Statement

			processNewAssignment(dom);
		} else if (expr instanceof InvokeExpr) {
			// NOT handle invocation

			// processInvocationAssignment(d, (InvokeExpr) expr, out_flow);
		} else if (leftField && rightField) {
			// example X = Y

			processReferenceToReferenceAssignment(dom);
		} else if (leftField) {
			// Example X.f=Y
			processFieldToReferenceOrLiteralAssignment(dom);
		} else if (rightField) {
			// Example X=Y.F

			processFieldToReferenceAssignment(dom);

		} else if (!leftField && !rightField) {
			// Example X=Y

			processReferenceToReferenceAssignment(dom);
		}

	}

	/**
	 * Process field to reference assignment.
	 *
	 * @param dom
	 *            the dom
	 */

	private void processFieldToReferenceAssignment(Unit dom) {

		Value def = dom.getDefBoxes().get(0).getValue();

		Value use = dom.getUseBoxes().get(0).getValue();

		String x = def.toString();
		String f = getFieldNameForExpr(use);

		CreateGtree(def, use);

		Add(def, use, false, dom.getJavaSourceStartLineNumber());

	}

	/**
	 * Get_ avg. Return Average of PointsTo
	 * 
	 * @return the float
	 */

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
		}
		return (float) sum / k;
	}

	/**
	 * Give which var. Give an id and return this id related to which variable
	 * 
	 * @param id
	 *            the id
	 * @return the value
	 */

	public Value GiveWhichVar(int id) {
		Iterator<Map.Entry<Value, Integer>> entries = Var_id.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Value, Integer> entry = entries.next();
			if (entry.getValue().intValue() == id) {
				return entry.getKey();
			}
		}
		return null;

	}

	/**
	 * P2S Give a File name and create the points-to-set report File Name, line
	 * number, variable, points-to-set
	 * 
	 * @param FileName
	 *            the file name
	 * @return the string
	 */

	public String P2S(String FileName) {
	Iterator<Map.Entry<Value, Integer>> entries = LineMap.entrySet().iterator();
		StringBuilder value = new StringBuilder();
		while (entries.hasNext()) {
			Map.Entry<Value, Integer> entry = entries.next();
			value.append(FileName + " " + entry.getValue().toString() + " " + entry.getKey().toString() + " " + "(");
			String Temp = entry.getKey().toString();
			if (Var_id.containsKey(entry.getKey())) {
				value.append(Var_id.get(entry.getKey()) + " ");

			} else {
				FlowSet<Integer> v = GetValueOF(entry.getKey());
				List<Integer> LV = v.toList();
				for (int i = 0; i < v.size(); i++) {
					value.append(GiveWhichVar(LV.get(i).intValue()) + " ");
				}
			}
			value.append(")\n");

		}
		return value.toString();
	}

	/**
	 * Process field to reference or literal assignment. Handle X=Y.f
	 * 
	 * @param dom
	 *            the dom
	 */

	private void processFieldToReferenceOrLiteralAssignment(Unit dom) {

		Value def = dom.getDefBoxes().get(0).getValue();

		Value use = null;
		int count = dom.getDefBoxes().size();
		if ((Integer) count >= 1) {
			use = dom.getUseBoxes().get(count - 1).getValue();
		} else if (count < 1)
			use = dom.getUseBoxes().get(0).getValue();

		String f = getFieldNameForExpr(def);

		CreateGtree(def, use);
		Add(def, use, false, dom.getJavaSourceStartLineNumber());

	}

	/**
	 * Gets the field name for expr.
	 *
	 * @param fieldAccessExpr
	 *            the field access expr
	 * @return the field name for expr
	 */

	private String getFieldNameForExpr(Value fieldAccessExpr) {

		String[] f_splice = fieldAccessExpr.toString().split(" +");
		String f = f_splice[f_splice.length - 1];
		f = f.substring(0, f.length() - 1);
		return f;

	}

	/**
	 * Process reference to reference assignment. Process the case x=y Def=x,
	 * Use=y Complete our GTree Pass parameter to Add func
	 * 
	 * @param Unit
	 *            the dom
	 */

	private void processReferenceToReferenceAssignment(Unit dom) {
		Value def = dom.getDefBoxes().get(0).getValue();
		Value use = dom.getUseBoxes().get(0).getValue();
		CreateGtree(def, use);
		Add(def, use, false, dom.getJavaSourceStartLineNumber());

	}

	/**
	 * Creates the gtree.
	 *
	 * @param def
	 *            the def
	 * @param use
	 *            the use
	 */

	private void CreateGtree(Value def, Value use) {
		FlowSet<Value> Tempset = new ArraySparseSet();
		Tempset.add(def);
		FlowSet<Value> prevSet = GTree.get(use);
		Tempset.union(prevSet);
		GTree.put(use, Tempset);
		if (!PointsTo.containsKey(def)) {
			FlowSet<Integer> t = new ArraySparseSet();
			PointsTo.put(def, t);
		}

	}

	/**
	 * Process new assignment. process the case x= new A; Dont need to call
	 * CreateGTree Call Add to add this new Assignment to our PointsTo
	 * 
	 * @param dom
	 *            the dom
	 */

	private void processNewAssignment(Unit dom) {
		Value right = dom.getUseBoxes().get(0).getValue();
		Value left = dom.getDefBoxes().get(0).getValue();

		Add(left, right, true, dom.getJavaSourceStartLineNumber());

	}

	/**
	 * Gets the valueof line. Check if we proceed this Line? Get LHS and
	 * Line_number and check it in LineRed
	 * 
	 * @param line
	 *            the line
	 * @param L
	 *            the l
	 * @return true, if find L,Line_num
	 * @return false, O/W
	 */

	private boolean GetValueofLine(int line, Value L) {
		boolean ret = true;
		Iterator<Map.Entry<Value, FlowSet<Integer>>> entries = LineRed.entrySet().iterator();

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

	/**
	 * Adds the.
	 *
	 * @param L
	 *            the l
	 * @param R
	 *            the r
	 * @param IsNew
	 *            the is new assignment ?
	 * @param LineNumber
	 *            the line number
	 */

	private void Add(Value L, Value R, boolean IsNew, int LineNumber) {

		if (GetValueofLine(LineNumber, L)) {

			FlowSet<Integer> R_F = new ArraySparseSet();
			if (IsNew) {
				// get new ID and add it to list of Var-id

				int idgen = IDGen.GID();
				Var_id.put(L, idgen);
				// Add it to WorkList

				Wlist.add(L);
				// we should integrate (the new id) with previous point-to and
				// add that to list of point-to .

				FlowSet<Integer> Tset = new ArraySparseSet();
				if (GetValueOF(L) != null)
					Tset.union(GetValueOF(L));
				FlowSet<Integer> GetFromVar_id = new ArraySparseSet();
				GetFromVar_id.add(Var_id.get(L));
				Tset.union(GetFromVar_id);
				PointsTo.put(L, Tset);
				// Related How to Update LineRed

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
				// Add it to LineMap

				LineMap.put(L, LineNumber);
				// Check we need Propagation?

				while (!Wlist.isEmpty()) {
					List<Value> LWlist = Wlist.toList();
					Value Should_propagated = LWlist.get(0);
					Wlist.remove(Should_propagated);
					Propagation(Should_propagated);
				}
			} else {
				// Not new R2R F2R R2F
				// Complete GTree

				if (GetValueOF(R) != null)
					R_F = GetValueOF(R);
				FlowSet TempGEtR = new ArraySparseSet();

				if (GetValueOF(R) != null)
					TempGEtR.union(R_F);
				GTree.put(R, TempGEtR);
				// Related How to Update LineRed
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
				// Need Propagation?
				while (Wlist.size() > 0) {

					List<Value> LWlist = Wlist.toList();
					Value Should_propagated = LWlist.get(0);
					Wlist.remove(Should_propagated);
					Propagation(Should_propagated);
				}
			}

		}

	}

	/**
	 * Return the FlowSet list of PointsTo related to specific Value
	 * 
	 * @param v
	 *            the v
	 * @return the flow set
	 */

	private FlowSet<Integer> GetValueOF(Value v) {

		Iterator<Map.Entry<Value, FlowSet<Integer>>> entries = PointsTo.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Value, FlowSet<Integer>> entry = entries.next();
			if (entry.getKey().toString().contains(v.toString()))
				return entry.getValue();
		}
		return null;
	}

	/**
	 * Gets the pointto.
	 *
	 * @return the pointto
	 */

	public void getPointto() {
		System.out.println(PointsTo);
	}

	FlowSet<Value> Wlist = new ArraySparseSet();

	/**
	 * 
	 * Propagation. Propagate information based on GTree
	 * 
	 * @param v
	 *            the v
	 */

	private void Propagation(Value v) {

		FlowSet<Value> RelatedTo = GTree.get(v);

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
		in.union(PointsTo.get(unit), out);
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