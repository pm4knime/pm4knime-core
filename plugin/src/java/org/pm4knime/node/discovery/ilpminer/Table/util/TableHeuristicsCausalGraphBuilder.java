package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.dataawarecnetminer.common.MinerContext;
import org.processmining.dataawarecnetminer.common.ProgressNoOpImpl;
import org.processmining.dataawarecnetminer.extension.dependencies.DependencyHeuristicConfig;
import org.processmining.dataawarecnetminer.model.DependencyAwareCausalGraph;
import org.processmining.dataawarecnetminer.model.DependencyAwareCausalGraphImpl;
import org.processmining.dataawarecnetminer.model.DependencyRelation;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.causalgraph.Relation;
import org.processmining.models.causalgraph.Relation.Factory;

public class TableHeuristicsCausalGraphBuilder {

	public static class HeuristicsConfig extends DependencyHeuristicConfig {
	}

	private final TableEventRelationStorageImpl relations;
	private DependencyHeuristicConfig config;

	private final Map<Relation, DependencyRelation> l1Loops = new HashMap<>();
	private final Map<Relation, DependencyRelation> l2Loops = new HashMap<>();

	private final Map<Relation, DependencyRelation> strongestFollowers = new HashMap<>();
	private final Map<Relation, DependencyRelation> strongestCauses = new HashMap<>();

	private final Map<Relation, DependencyRelation> causalRelations = new HashMap<>();

	public TableHeuristicsCausalGraphBuilder(TableEventRelationStorageImpl tableEventRelationStorageImpl) {
		this.relations = tableEventRelationStorageImpl;
		this.config = new DependencyHeuristicConfig();
	}

	public DependencyAwareCausalGraph build() {
		return build(new MinerContext() {

			public Progress getProgress() {
				return new ProgressNoOpImpl();
			}

			public ExecutorService getExecutor() {
				return null;
			}

			public PluginContext getPluginContext() {
				return null;
			}
		});
	}

	public DependencyAwareCausalGraph build(MinerContext context) {
		
		l1Loops.clear();
		l2Loops.clear();
		strongestFollowers.clear();
		strongestCauses.clear();
		causalRelations.clear();

		addLoopRelations();

		if (config.isAllTasksConnected()) {
			addStrongestFollowers();
			addStrongestCauses();
			removeWeakOutgoingL2();
			removeWeakIncomingL2();
		}

		addStrongDependencies();

		final Map<Relation, DependencyRelation> graphRelations = new HashMap<Relation, DependencyRelation>();
		graphRelations.putAll(l1Loops);
		graphRelations.putAll(l2Loops);
		graphRelations.putAll(strongestCauses);
		graphRelations.putAll(strongestFollowers);
		graphRelations.putAll(causalRelations);

		if (config.isAcceptedTasksConnected()) {
			TableAcceptedConnectedHeuristic.connectAcceptedTasks(relations, graphRelations,
					getConfig().getRelativeToBestThreshold());
		}

		return new DependencyAwareCausalGraphImpl(relations.getEventClasses(), graphRelations);
	}

	private void addLoopRelations() {
		// Check for loops of length 1 (l1 loop), i.e., self loops 
		for (XEventClass event : relations.getEventClasses()) {
			Relation relation = Relation.Factory.create(event, event);
			int observations = relations.getDirectlyFollowsRelations().count(relation);
			if (observations >= calcRequiredObservations(relation)) {
				DependencyRelation l1Dependency = relations.getL1LoopRelation(relation);
				if (l1Dependency.getDependencyValue() >= config.getL1Threshold()) {
					l1Loops.put(relation, l1Dependency);
				}
			}
		}

		// Need complete information on l1 loops before checking for loops of length 2 (l2 loops)
		for (Relation relation : relations.allNonEqualCombinations()) {
			boolean alreadyInL1Loop = (l1Loops
					.containsKey(Relation.Factory.create(relation.getSource(), relation.getSource())))
					|| (l1Loops.containsKey(Relation.Factory.create(relation.getTarget(), relation.getTarget())));
			if (!alreadyInL1Loop) {
				int observations = relations.countLengthTwoFollows(relation.getSource(), relation.getTarget());
				if (observations >= calcRequiredObservations(relation)) {
					DependencyRelation l2Dependency = relations.getL2LoopRelation(relation);
					// if any of the two activities is already involved in a l1 loop 
					if (l2Dependency.getDependencyValue() >= config.getL2Threshold()) {
						l2Loops.put(relation, l2Dependency);
					}
				}
			}
		}
	}

	private void addStrongestFollowers() {
		for (XEventClass sourceClass : relations.getEventClasses()) {
			// Don't try to find follower of end class
			if (!sameClass(sourceClass, relations.getEndEventClass())) {
				Relation bestFollower = findStrongestFollower(sourceClass);
				if (bestFollower != null) {
					//TODO avoid computing twice
					strongestFollowers.put(bestFollower, getDependencyRelation(bestFollower));
				} // no best found within observation threshold
			}
		}
	}

	private Relation findStrongestFollower(XEventClass sourceClass) {
		double bestDependency = -1.0;
		Relation bestOutgoing = null;
		for (XEventClass targetClass : relations.getEventClasses()) {
			if (!sameClass(sourceClass, targetClass) && !targetClass.equals(relations.getStartEventClass())) { // different from paper strongest follower might be end
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				double dependencyValue = getDependencyRelation(relation).getDependencyValue();
				if (dependencyValue >= bestDependency) {
					bestDependency = dependencyValue;
					bestOutgoing = relation;
				}
			}
		}
		return bestOutgoing;
	}

	private void addStrongestCauses() {
		for (XEventClass targetClass : relations.getEventClasses()) {
			if (!sameClass(targetClass, relations.getStartEventClass())) {
				Relation bestCause = findStrongestCause(targetClass);
				if (bestCause != null) {
					//TODO avoid computing twice
					strongestCauses.put(bestCause, getDependencyRelation(bestCause));
				} // no best found within observation threshold
			}
		}
	}

	private Relation findStrongestCause(XEventClass targetClass) {
		double bestDependency = -1.0;
		Relation bestIncoming = null;
		for (XEventClass sourceClass : relations.getEventClasses()) {
			if (!sameClass(sourceClass, targetClass) && !sourceClass.equals(relations.getEndEventClass())) { // different from paper strongest cause might be start
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				double dependencyValue = getDependencyRelation(relation).getDependencyValue();
				if (dependencyValue >= bestDependency) {
					bestDependency = dependencyValue;
					bestIncoming = relation;
				}
			}
		}
		return bestIncoming;
	}

	private void removeWeakOutgoingL2() {
		for (Iterator<Relation> iterator = strongestFollowers.keySet().iterator(); iterator.hasNext();) {
			Relation relation = iterator.next();
			double dependencyValue = getDependencyRelation(relation).getDependencyValue();
			if (dependencyValue < config.getDependencyThreshold() && isWeakOutgoingConnection(relations, l2Loops,
					strongestFollowers, relation, config.getRelativeToBestThreshold())) {
				iterator.remove();
			}
		}
	}

	private void removeWeakIncomingL2() {
		for (Iterator<Relation> iterator = strongestCauses.keySet().iterator(); iterator.hasNext();) {
			Relation relation = iterator.next();
			double dependencyValue = getDependencyRelation(relation).getDependencyValue();
			if (dependencyValue < config.getDependencyThreshold() && isWeakIncomingConnection(relations, l2Loops,
					strongestCauses, relation, config.getRelativeToBestThreshold())) {
				iterator.remove();
			}
		}
	}

	private void addStrongDependencies() {
		for (Relation ab : relations.allNonEqualCombinations()) {
			if (!(isTargetStart(ab) || isSourceEnd(ab))) {
				DependencyRelation abDep = getDependencyRelation(ab);
				int observations = relations.getDirectlyFollowsRelations().count(ab);
				if (observations >= calcRequiredObservations(ab)) {
					// check if dependency is frequently observed
					if (abDep.getDependencyValue() >= config.getDependencyThreshold()) {
						// dependency is a strong dependency 
						causalRelations.put(ab, abDep);
					} else if (config.isAllTasksConnected()) {
						// dependency is not strong enough, but might be included due to relative to best ratio
						for (XEventClass c : relations.getEventClasses()) {
							Relation ac = Relation.Factory.create(ab.getSource(), c);
							if (!sameClass(ac)
									&& (strongestFollowers.containsKey(ac) || strongestCauses.containsKey(ac))) {
								// check whether the relation 'ab' is within the same dependency values as 'ac' 
								DependencyRelation acDep = getDependencyRelation(ac);
								if ((acDep.getDependencyValue() - abDep.getDependencyValue()) <= config
										.getRelativeToBestThreshold()) {
									causalRelations.put(ab, abDep);
								}
							}
						}
					}
				}
			}
		}
	}

	private DependencyRelation getDependencyRelation(Relation relation) {
		return relations.getDependencyRelation(relation);
	}

	private static boolean sameClass(XEventClass sourceClass, XEventClass targetClass) {
		return sourceClass.equals(targetClass);
	}

	private static boolean sameClass(Relation relation) {
		return sameClass(relation.getSource(), relation.getTarget());
	}

	private boolean isSourceEnd(Relation relation) {
		return relation.getSource().equals(relations.getEndEventClass());
	}

	private boolean isTargetStart(Relation relation) {
		return relation.getTarget().equals(relations.getStartEventClass());
	}

	private int calcRequiredObservations(Relation relation) {
		if (config.getObservationThreshold() == 0.0d) {
			return 0;
		}
		int totalCount = relations.countTraces();
		return (int) Math.ceil(config.getObservationThreshold() * totalCount);
	}

	private boolean isWeakOutgoingConnection(TableEventRelationStorageImpl relations2,
			Map<Relation, DependencyRelation> causalL2Loop, Map<Relation, DependencyRelation> causalOut,
			Relation relation, double relativeToBest) {
		XEventClass a = relation.getSource();
		XEventClass x = relation.getTarget();
		for (Relation r1 : causalOut.keySet()) { //TODO optimize avoid computing dependency relation twice
			XEventClass b = r1.getSource();
			XEventClass y = r1.getTarget();
			for (Relation r2 : causalL2Loop.keySet()) {
				if (r2.getSource().equals(a) && r2.getTarget().equals(b)) {
					DependencyRelation by = relations2.getDependencyRelation(Factory.create(b, y));
					DependencyRelation ax = relations2.getDependencyRelation(Factory.create(a, x));
					return by.getDependencyValue() - ax.getDependencyValue() > relativeToBest;
				}
			}
		}
		return false;
	}

	private boolean isWeakIncomingConnection(TableEventRelationStorageImpl relations2,
			Map<Relation, DependencyRelation> causalL2Loop, Map<Relation, DependencyRelation> causalIn,
			Relation relation, double relativeToBest) {
		XEventClass x = relation.getSource();
		XEventClass a = relation.getTarget();
		for (Relation r1 : causalIn.keySet()) { //TODO optimize avoid computing dependency relation twice
			XEventClass y = r1.getSource();
			XEventClass b = r1.getTarget();
			for (Relation r2 : causalL2Loop.keySet()) {
				if (r2.getSource().equals(a) && r2.getTarget().equals(b)) {
					DependencyRelation yb = relations2.getDependencyRelation(Factory.create(y, b));
					DependencyRelation xa = relations2.getDependencyRelation(Factory.create(x, a));
					return yb.getDependencyValue() - xa.getDependencyValue() > relativeToBest;
				}
			}
		}
		return false;
	}

	public Set<Relation> getCausalRelations() {
		return causalRelations.keySet();
	}

	public Set<Relation> getL1Loops() {
		return l1Loops.keySet();
	}

	public Set<Relation> getL2Loops() {
		return l2Loops.keySet();
	}

	public Set<Relation> getStrongestFollowers() {
		return strongestFollowers.keySet();
	}

	public Set<Relation> getStrongestCauses() {
		return strongestCauses.keySet();
	}

	public DependencyHeuristicConfig getConfig() {
		return config;
	}

	public void setConfig(DependencyHeuristicConfig config) {
		this.config = config;
	}

}