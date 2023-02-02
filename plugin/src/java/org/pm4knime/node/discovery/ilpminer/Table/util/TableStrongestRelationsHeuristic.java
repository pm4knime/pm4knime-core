package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.dataawarecnetminer.model.DependencyRelation;
import org.processmining.models.causalgraph.Relation;

public class TableStrongestRelationsHeuristic {

	static Relation findStrongestFrequentFollower(TableEventRelationStorageImpl eventStore, XEventClass sourceClass) {
		double bestScore = 0;
		Relation bestOutgoing = null;
		for (XEventClass targetClass : eventStore.getEventClasses()) {
			if (!sourceClass.equals(targetClass) && !targetClass.equals(eventStore.getStartEventClass())) { // different from paper strongest follower might be end
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				if (eventStore.countDirectlyFollows(relation) > 0) {
					double score = scoreRelation(eventStore, relation, relation.getSource());
					if (score > bestScore) {
						bestScore = score;
						bestOutgoing = relation;
					}
				}
			}
		}

		return bestOutgoing != null ? bestOutgoing
				: Relation.Factory.create(sourceClass, eventStore.getEndEventClass());
	}

	private static double scoreRelation(TableEventRelationStorageImpl eventStore, Relation relation, XEventClass referenceTask) {
		double dependencyValue = eventStore.getDependencyRelation(relation).getDependencyValue();
		if (dependencyValue > 0) {
			int count = eventStore.countDirectlyFollows(relation);
			double frequencyRatio = count / (double) eventStore.countOccurence(referenceTask);
			double score = (2 * dependencyValue * frequencyRatio) / (dependencyValue + frequencyRatio);
			return score;
		} else {
			return 0.0;
		}
	}

	static Relation findStrongestFrequentCause(TableEventRelationStorageImpl eventStore, XEventClass targetClass) {
		double bestScore = 0;
		Relation bestIncoming = null;
		for (XEventClass sourceClass : eventStore.getEventClasses()) {
			if (!sourceClass.equals(targetClass) && !sourceClass.equals(eventStore.getEndEventClass())) { // different from paper strongest cause might be start
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				if (eventStore.countDirectlyFollows(relation) > 0) {
					double score = scoreRelation(eventStore, relation, relation.getTarget());
					if (score > bestScore) {
						bestScore = score;
						bestIncoming = relation;
					}
				}
			}
		}

		return bestIncoming != null ? bestIncoming
				: Relation.Factory.create(eventStore.getStartEventClass(), targetClass);

	}

	static Relation findStrongestFollower(TableEventRelationStorageImpl eventStore, XEventClass sourceClass) {
		double bestDependency = 0;
		Relation bestOutgoing = null;
		for (XEventClass targetClass : eventStore.getEventClasses()) {
			if (!sourceClass.equals(targetClass) && !targetClass.equals(eventStore.getStartEventClass())) { // different from paper strongest follower might be end
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				if (eventStore.countDirectlyFollows(relation) > 0) {
					double dependencyValue = eventStore.getDependencyRelation(relation).getDependencyValue();
					if (dependencyValue > bestDependency) {
						bestDependency = dependencyValue;
						bestOutgoing = relation;
					}
				}
			}
		}

		return bestOutgoing != null ? bestOutgoing
				: Relation.Factory.create(sourceClass, eventStore.getEndEventClass());
	}

	static Relation findStrongestCause(TableEventRelationStorageImpl relations, XEventClass targetClass) {
		double bestDependency = 0.0;
		Relation bestIncoming = null;
		for (XEventClass sourceClass : relations.getEventClasses()) {
			if (!sourceClass.equals(targetClass) && !sourceClass.equals(relations.getEndEventClass())) { // different from paper strongest cause might be start
				Relation relation = Relation.Factory.create(sourceClass, targetClass);
				if (relations.countDirectlyFollows(relation) > 0) {
					double dependencyValue = relations.getDependencyRelation(relation).getDependencyValue();
					if (dependencyValue > bestDependency) {
						bestDependency = dependencyValue;
						bestIncoming = relation;
					}
				}
			}
		}

		return bestIncoming != null ? bestIncoming
				: Relation.Factory.create(relations.getStartEventClass(), targetClass);

	}

	static Map<Relation, DependencyRelation> findRelativeToBestCauses(TableEventRelationStorageImpl eventStore,
			Relation bestRelation, double relativeToBestRatio) {

		DependencyRelation bestDependency = eventStore.getDependencyRelation(bestRelation);
		Map<Relation, DependencyRelation> relativeToBestSet = new HashMap<>();

		for (XEventClass nextBestSrc : eventStore.getEventClasses()) {
			if (!nextBestSrc.equals(bestRelation.getSource())) {
			Relation nextBestRelation = Relation.Factory.create(nextBestSrc, bestRelation.getTarget());
			if (!sameClass(nextBestRelation) && eventStore.countDirectlyFollows(nextBestRelation) > 0) {
				DependencyRelation nextBestDependency = eventStore.getDependencyRelation(nextBestRelation);
				if ((bestDependency.getDependencyValue()
						- nextBestDependency.getDependencyValue()) <= relativeToBestRatio) {
					relativeToBestSet.put(nextBestRelation, nextBestDependency);
				}
			}
			}
		}

		return relativeToBestSet;
	}

	static Map<Relation, DependencyRelation> findRelativeToBestFollowers(TableEventRelationStorageImpl eventStore,
			Relation bestRelation, double relativeToBestRatio) {

		DependencyRelation bestDependency = eventStore.getDependencyRelation(bestRelation);
		Map<Relation, DependencyRelation> relativeToBestSet = new HashMap<>();

		for (XEventClass nextBestTarget : eventStore.getEventClasses()) {
			if (!nextBestTarget.equals(bestRelation.getTarget())) {
				Relation nextBestRelation = Relation.Factory.create(bestRelation.getSource(), nextBestTarget);
				if (!sameClass(nextBestRelation) && eventStore.countDirectlyFollows(nextBestRelation) > 0) {
					DependencyRelation nextBestDependency = eventStore.getDependencyRelation(nextBestRelation);
					if ((bestDependency.getDependencyValue()
							- nextBestDependency.getDependencyValue()) <= relativeToBestRatio) {
						relativeToBestSet.put(nextBestRelation, nextBestDependency);
					}
				}
			}
		}

		return relativeToBestSet;
	}

	private static boolean sameClass(Relation relation) {
		return sameClass(relation.getSource(), relation.getTarget());
	}

	private static boolean sameClass(XEventClass sourceClass, XEventClass targetClass) {
		return sourceClass.equals(targetClass);
	}

}
