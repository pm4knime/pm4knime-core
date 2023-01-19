package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.processmining.dataawarecnetminer.model.DependencyRelation;
import org.processmining.dataawarecnetminer.model.DependencyRelationImpl;
import org.processmining.dataawarecnetminer.model.EventRelationStorage;
import org.processmining.log.utils.XUtils;
import org.processmining.models.causalgraph.Relation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;

public final class TableEventRelationStorageImpl implements EventRelationStorage {

	private final class EventClassCaptionFunction implements Function<Integer, String> {
		public String apply(Integer index) {
			return classByIndex.get(index).getId();
		}
	}

	private final ImmutableSet<XEventClass> classes;
	private final ImmutableMap<String, XEventClass> classById;
	private final ImmutableMap<Integer, XEventClass> classByIndex;
	private final Function<String, XEventClass> eventToClass = new Function<String, XEventClass>() {
		public XEventClass apply(String event) {
			return getEventClass(event);
		}
	};

	private final XEventClass startEventClass;
	private final XEventClass endEventClass;

	//private final Collection<TraceVariant> storedTraces = new ConcurrentLinkedQueue<>();
	private final AtomicInteger traceCount = new AtomicInteger();

	private final Map<XEventClass, Set<XEventClass>> directlyFollows = new HashMap<>();

	private final Multiset<XEventClass> occurence = ConcurrentHashMultiset.create();
	private final Multiset<Relation> directlyFollowsRelations = ConcurrentHashMultiset.create();
	private final Multiset<Relation> lengthTwoLoopRelations = ConcurrentHashMultiset.create();
	private final Multiset<Relation> eventuallyFollowsRelations = ConcurrentHashMultiset.create();

	private boolean cacheDirty = true;

	public TableEventRelationStorageImpl(XEventClasses originalEventClasses) {
		super();

		ImmutableSet.Builder<XEventClass> classesBuilder = ImmutableSet.<XEventClass>builder();

		Builder<String, XEventClass> classByIdBuilder = ImmutableMap.builder();
		Builder<Integer, XEventClass> classByIndexBuilder = ImmutableMap.builder();

		for (int i = 0; i < originalEventClasses.size(); i++) {
			XEventClass eventClass = new XEventClass(originalEventClasses.getByIndex(i).getId(), i);
			classesBuilder.add(eventClass);
		}
		
		startEventClass = new XEventClass(EventRelationStorage.ARTIFICIAL_START, originalEventClasses.size());
		endEventClass = new XEventClass(EventRelationStorage.ARTIFICIAL_END, originalEventClasses.size() + 1);

		classesBuilder.add(startEventClass);
		classesBuilder.add(endEventClass);
		classes = classesBuilder.build();

		for (XEventClass eventClass : classes) {
			classByIdBuilder.put(eventClass.getId(), eventClass);
			classByIndexBuilder.put(eventClass.getIndex(), eventClass);
		}
		classById = classByIdBuilder.build();
		classByIndex = classByIndexBuilder.build();
	}

	public String printRelations() {
		StringBuilder sb = new StringBuilder();
		sb.append("Directly-follows:\n");
		sb.append(
				stringifyTable(getRelationAsIntTable(getDirectlyFollowsRelations()), new EventClassCaptionFunction()));
		sb.append('\n');
		sb.append("L2-follows:");
		sb.append(stringifyTable(getRelationAsIntTable(getL2FollowsRelations()), new EventClassCaptionFunction()));
		sb.append('\n');
		sb.append("Eventually-follows:");
		sb.append(stringifyTable(getRelationAsIntTable(getEventuallyFollowsRelations()),
				new EventClassCaptionFunction()));
		return sb.toString();
	}

	private String stringifyTable(int[][] table, Function<Integer, String> caption) {
		int maxCount = Ints.max(Ints.concat(table));
		int maxDigits = digits(maxCount);
		int maxCaptionLength = -1;
		for (int row = 0; row < table.length; row++) {
			maxCaptionLength = Math.max(maxCaptionLength, caption.apply(row).length());
		}
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < table.length; row++) {
			sb.append(String.format("%" + maxCaptionLength + "s", caption.apply(row)));
			sb.append(' ');
			sb.append('|');
			sb.append(' ');
			for (int col = 0; col < table[row].length; col++) {
				sb.append(String.format("%" + maxDigits + "d", table[row][col]));
				sb.append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private int digits(int number) {
		if (number == 0) {
			return 1;
		}
		return (int) Math.floor(Math.log10(Math.abs(number))) + 1;
	}

	public int[][] getRelationAsIntTable(Multiset<Relation> relations) {
		int[][] table = new int[getEventClasses().size()][];
		// Updated all metrics
		for (XEventClass classA : getEventClasses()) {
			if (table[classA.getIndex()] == null) {
				table[classA.getIndex()] = new int[getEventClasses().size()];
			}
			for (XEventClass classB : getEventClasses()) {
				table[classA.getIndex()][classB.getIndex()] = relations.count(Relation.Factory.create(classA, classB));
			}
		}
		return table;
	}

	public void addTrace(ArrayList<String> trace) {

		//storedTraces.add(trace);
		traceCount.incrementAndGet();

		// Create an iterator with artificial start/end events
		PeekingIterator<XEventClass> eventIter = createEventIterator(trace);

		Multimap<XEventClass, XEventClass> eventuallyActivations = HashMultimap.create(getEventClasses().size() * 2,
				trace.size() * 2);
		XEventClass lastEventClass = null;

		while (eventIter.hasNext()) {
			XEventClass currentClass = eventIter.next();
			occurence.add(currentClass);

			for (XEventClass eventClass : getEventClasses()) {
				eventuallyActivations.put(eventClass, currentClass);
			}

			if (eventIter.hasNext()) {
				XEventClass followingClass = eventIter.peek();

				// ab -> b [directly follows] a
				addDirectlyFollows(currentClass, followingClass);

				for (Iterator<XEventClass> iterator = eventuallyActivations.get(followingClass).iterator(); iterator
						.hasNext();) {
					XEventClass activePredecessor = iterator.next();
					// Add eventually follows relation (a,b)
					addEventuallyFollows(followingClass, activePredecessor);
					// Remove activation of 'a' for event class 'b' so a second occurrence of 'b' is not counted unless another 'a' happens
					iterator.remove();
				}

				// aba -> b [in length two loop with] a
				if (followingClass.equals(lastEventClass)) {
					addLengthTwoFollows(lastEventClass, currentClass);
				}
			}

			lastEventClass = currentClass;
		}
	}
	
	public void addTrace(TraceVariant trace) {

		for (int i = 0; i<trace.getFrequency(); i++) {
			addTrace(trace.getActivities());
		}
	}

	public void addEventuallyFollows(XEventClass source, XEventClass target) {
		eventuallyFollowsRelations.add(Relation.Factory.create(target, source));
		cacheDirty = true;
	}

	public void addLengthTwoFollows(XEventClass source, XEventClass target) {
		lengthTwoLoopRelations.add(Relation.Factory.create(source, target));
		cacheDirty = true;
	}

	public void addDirectlyFollows(XEventClass source, XEventClass target) {
		directlyFollowsRelations.add(Relation.Factory.create(source, target));
		cacheDirty = true;
	}

	private void ensureUpdatedCache() {
		if (cacheDirty) {
			directlyFollows.clear();
			for (XEventClass eventClass: getEventClasses()) {
				directlyFollows.put(eventClass, Sets.<XEventClass>newHashSet());
			}
			for (Relation element : directlyFollowsRelations.elementSet()) {
				directlyFollows.get(element.getSource()).add(element.getTarget());
			}
			cacheDirty = false;
		}
	}

	private PeekingIterator<XEventClass> createEventIterator(ArrayList<String> trace) {
		// Add initial and last event classes
		return Iterators.peekingIterator(Iterators.concat(Iterators.singletonIterator(getStartEventClass()),
				Iterators.transform(trace.iterator(), eventToClass), Iterators.singletonIterator(getEndEventClass())));
	}

	public XEventClass getStartEventClass() {
		return startEventClass;
	}

	public XEventClass getEndEventClass() {
		return endEventClass;
	}

	public XEventClass getEventClass(String event) {
		return classById.get(event);
	}

	public ImmutableMap<String, XEventClass> getEventClassesById() {
		return classById;
	}


	public Multiset<XEventClass> getEventClassOccurence() {
		return occurence;
	}

	public Multiset<Relation> getDirectlyFollowsRelations() {
		return directlyFollowsRelations;
	}

	public Multiset<Relation> getL2FollowsRelations() {
		return lengthTwoLoopRelations;
	}

	public Multiset<Relation> getEventuallyFollowsRelations() {
		return eventuallyFollowsRelations;
	}

	public ImmutableSet<XEventClass> getEventClasses() {
		return classes;
	}

	public int getMaximumDirectlyFollowsCount() {
		return getMaxCount(getDirectlyFollowsRelations());
	}

	private <T> int getMaxCount(Multiset<T> multiset) {
		return Ordering.natural().onResultOf(new Function<Multiset.Entry<T>, Integer>() {
			public Integer apply(Multiset.Entry<T> entry) {
				return entry.getCount();
			}
		}).max(multiset.entrySet()).getCount();
	}

	public int countTraces() {
		return traceCount.get();
	}

	public int countEvents() {
		return occurence.size();
	}

	public int countOccurence(XEventClass eventClass) {
		return occurence.count(eventClass);
	}

	public int countDirectlyFollows(XEventClass source, XEventClass target) {
		return countDirectlyFollows(Relation.Factory.create(source, target));
	}

	public int countDirectlyFollows(Relation relation) {
		return directlyFollowsRelations.count(relation);
	}

	public int countLengthTwoFollows(XEventClass source, XEventClass target) {
		return lengthTwoLoopRelations.count(Relation.Factory.create(source, target));
	}

	public int countEventuallyFollows(XEventClass source, XEventClass target) {
		return eventuallyFollowsRelations.count(Relation.Factory.create(source, target));
	}

	public DependencyRelation getDependencyRelation(Relation followsRelation) {
		XEventClass source = followsRelation.getSource();
		XEventClass target = followsRelation.getTarget();
		int sourceToTarget = countDirectlyFollows(source, target);
		int targetToSource = countDirectlyFollows(target, source);
		final double dependencyValue = (sourceToTarget - targetToSource)
				/ (double) (sourceToTarget + targetToSource + 1);
		return new DependencyRelationImpl(dependencyValue);
	}

	public DependencyRelation getL1LoopRelation(Relation relation) {
		XEventClass source = relation.getSource();
		XEventClass target = relation.getTarget();
		final double dependencyValue = (countDirectlyFollows(source, target))
				/ (double) (countDirectlyFollows(source, target) + 1);
		return new DependencyRelationImpl(dependencyValue);
	}

	public DependencyRelation getL2LoopRelation(Relation relation) {
		XEventClass source = relation.getSource();
		XEventClass target = relation.getTarget();
		final double dependencyValue = (countLengthTwoFollows(source, target) + countLengthTwoFollows(target, source))
				/ (double) (countLengthTwoFollows(source, target) + countLengthTwoFollows(target, source) + 1);
		return new DependencyRelationImpl(dependencyValue);
	}

	public DependencyRelation getLongDistanceDependency(Relation relation) {
		XEventClass source = relation.getSource();
		XEventClass target = relation.getTarget();
		int occurenceSource = countOccurence(relation.getSource());
		int occurenceTarget = countOccurence(relation.getTarget());
		final double dependencyValue = ((2 * countEventuallyFollows(source, target))
				/ (double) (occurenceSource + occurenceTarget + 1))
				- ((2 * Math.abs(occurenceSource - occurenceTarget))
						/ (double) (occurenceSource + occurenceTarget + 1));
		return new DependencyRelationImpl(dependencyValue);
	}

	public Iterable<Relation> allCombinations() {
		return new Iterable<Relation>() {

			public Iterator<Relation> iterator() {
				return new Iterator<Relation>() {

					private UnmodifiableIterator<XEventClass> sourceIterator = classes.iterator();
					private UnmodifiableIterator<XEventClass> targetIterator = classes.iterator();
					private XEventClass currentSource = sourceIterator.next();

					public Relation next() {
						if (targetIterator.hasNext()) {
							return Relation.Factory.create(currentSource, targetIterator.next());
						} else {
							currentSource = sourceIterator.next();
							targetIterator = classes.iterator();
							return Relation.Factory.create(currentSource, targetIterator.next());
						}

					}

					public boolean hasNext() {
						return sourceIterator.hasNext() || targetIterator.hasNext();
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}

				};
			}
		};
	}

	public Iterable<Relation> allCombinations(final XEventClass source) {
		return Iterables.<Relation>filter(allCombinations(), new Predicate<Relation>() {

			public boolean apply(Relation relation) {
				return relation.getSource().equals(source);
			}
		});
	}

	public Iterable<Relation> allObservedCombinations() {
		return directlyFollowsRelations.elementSet();
		/*return new Iterable<Relation>() {

			public Iterator<Relation> iterator() {
				if (directlyFollows.keySet().isEmpty()) {
					return Iterators.emptyIterator();
				} else {
					return new AbstractIterator<Relation>() {

						private Iterator<XEventClass> sourceIterator = directlyFollows.keySet().iterator();
						private XEventClass currentSource = sourceIterator.next();
						private Iterator<XEventClass> targetIterator = directlyFollows.get(currentSource).iterator();

						protected Relation computeNext() {
							if (targetIterator.hasNext()) {
								return Relation.Factory.create(currentSource, targetIterator.next());
							} else
								while (sourceIterator.hasNext()) {
									currentSource = sourceIterator.next();
									targetIterator = directlyFollows.get(currentSource).iterator();
									if (targetIterator.hasNext()) {
										return Relation.Factory.create(currentSource, targetIterator.next());
									}
								}
							return endOfData();
						}
					};
				}
			}
		};*/
	}

	public Iterable<Relation> allNonEqualCombinations() {
		return Iterables.<Relation>filter(allCombinations(), new Predicate<Relation>() {

			public boolean apply(Relation relation) {
				return !relation.getSource().equals(relation.getTarget());
			}
		});
	}

	public Set<XEventClass> directlyFollows(final XEventClass source) {
		ensureUpdatedCache();
		return directlyFollows.get(source);
	}

	public boolean includesStartOrEnd(Relation relation) {
		return relation.getSource().equals(getStartEventClass()) 
				|| relation.getSource().equals(getEndEventClass())								
				|| relation.getTarget().equals(getStartEventClass())
				|| relation.getTarget().equals(getEndEventClass());
	}

	@Override
	public XEventClassifier getClassifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XEventClass getEventClass(XEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTrace(XTrace trace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<XTrace> getAllTraces() {
		// TODO Auto-generated method stub
		return null;
	}

}