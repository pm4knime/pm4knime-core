package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.dataawarecnetminer.model.DependencyRelation;
import org.processmining.models.causalgraph.Relation;

public class TableAcceptedConnectedHeuristic {

	public static void connectAcceptedTasks(TableEventRelationStorageImpl relations,
			final Map<Relation, DependencyRelation> graphRelations, double relativeToBestRatio) {
		boolean hasUnconnected = true;
		while (hasUnconnected) {
			hasUnconnected = false;
			for (Relation relation : graphRelations.keySet()) {
				if (!hasSource(relations, graphRelations.keySet(), relation.getSource())) {
					// add strongest incoming
					Relation strongestCause = TableStrongestRelationsHeuristic.findStrongestCause(relations,
							relation.getSource());
					if (strongestCause != null) {
						hasUnconnected = true;
						graphRelations.put(strongestCause, relations.getDependencyRelation(strongestCause));
						graphRelations.putAll(TableStrongestRelationsHeuristic.findRelativeToBestCauses(relations,
								strongestCause, relativeToBestRatio));
					}
				}
				if (!hasTarget(relations, graphRelations.keySet(), relation.getTarget())) {
					// add strongest outgoing
					Relation strongestFollower = TableStrongestRelationsHeuristic.findStrongestFollower(relations,
							relation.getTarget());
					if (strongestFollower != null) {
						hasUnconnected = true;
						graphRelations.put(strongestFollower, relations.getDependencyRelation(strongestFollower));
						graphRelations.putAll(TableStrongestRelationsHeuristic.findRelativeToBestFollowers(relations,
								strongestFollower, relativeToBestRatio));
					}
				}
				if (hasUnconnected) {
					break;
				}
			}
		}
	}

	private static boolean hasTarget(TableEventRelationStorageImpl relations, Set<Relation> graph, XEventClass node) {
		if (node.equals(relations.getEndEventClass())) {
			return true; // end does not need a target
		}
		for (Relation relation : graph) {
			if (!relation.getSource().equals(relation.getTarget()) && relation.getSource().equals(node)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasSource(TableEventRelationStorageImpl relations, Set<Relation> graph, XEventClass node) {
		if (node.equals(relations.getStartEventClass())) {
			return true; // end does not need a target
		}
		for (Relation relation : graph) {
			if (!relation.getSource().equals(relation.getTarget()) && relation.getTarget().equals(node)) {
				return true;
			}
		}
		return false;
	}

}
