package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.io.IOException;
import java.io.InputStream;

import org.processmining.plugins.astar.petrinet.impl.PILPDelegate;

import nl.tue.astar.AStarThread;
import nl.tue.astar.Delegate;
import nl.tue.astar.FastLowerBoundTail;
import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.astar.util.LPResult;
import nl.tue.storage.CompressedStore;

public class PILPTailTable implements FastLowerBoundTail {
	// The maximum size of this.super:    16
		protected int estimate; //      8
		protected final short[] variables; //   24 + 2 * length
		protected boolean isExact;

		public static int getSizeFor(int variables) {
			return 8 * (1 + (56 + 2 * variables - 1) / 8);
		}

		public PILPTailTable(AbstractPILPDelegateTable<?> delegate, PHeadTable h, int minCost) {
			variables = new short[delegate.numTransitions() * 2 + delegate.numEventClasses() + delegate.numFinalMarkings()];
			if (delegate.useFastLowerbounds()) {
				this.estimate = minCost;
				this.isExact = false;
			} else {
				computeEstimate(delegate, h, minCost);
			}
		}

		protected PILPTailTable(int estimate, short[] variables, boolean isExact) {
			if (estimate < 0) {
				this.estimate = 0;
			} else {
				this.estimate = estimate;
			}
			this.variables = variables;
			this.isExact = isExact;
		}

		public PILPTailTable getNextTail(Delegate<? extends Head, ? extends Tail> d, Head newHead, int modelMove, int logMove,
				int activity) {

			PILPDelegateTable delegate = ((PILPDelegateTable) d);

			int newEst = estimate - delegate.getCostFor(modelMove, activity);

			// check if the is move was allowed according to the LP:
			if (modelMove == AStarThread.NOMOVE) {
				// logMove only. The variable is at location 2*delegate.numTransitions() + activity
				int var = 2 * delegate.numTransitions() + activity;
				if (variables[var] >= 1) {
					// move was allowed according to LP.
					short[] newVars = new short[variables.length];
					System.arraycopy(variables, 0, newVars, 0, variables.length);
					newVars[var] -= 1;
					return new PILPTailTable(newEst, newVars, true);
				}
			} else if (logMove == AStarThread.NOMOVE) {
				// there was a modelMove only, determine the newly enabled moves.
				int var = modelMove;

				if (variables[var] >= 1 && !delegate.hasResetArc(modelMove)) {
					// move was allowed according to LP.
					short[] newVars = new short[variables.length];
					System.arraycopy(variables, 0, newVars, 0, variables.length);
					newVars[var] -= 1;
					return new PILPTailTable(newEst, newVars, true);
				}

			} else {
				// Synchronous move. The variable is at location delegate.numTransitions() + modelMove
				int var = delegate.numTransitions() + modelMove;
				if (variables[var] >= 1 && !delegate.hasResetArc(modelMove)) {
					// move was allowed according to LP.
					short[] newVars = new short[variables.length];
					System.arraycopy(variables, 0, newVars, 0, variables.length);
					newVars[var] -= 1;
					return new PILPTailTable(newEst, newVars, true);
				}
			}
			//BVD: DO NOT use newEst maximized with size of Parikh Vector as this violates the monotonicity
			return new PILPTailTable(delegate, (PHeadTable) newHead, newEst);//Math.max(newEst, ((PHead) newHead).getParikhVector().getNumElts()));
		}

		public <S> Tail getNextTailFromStorage(Delegate<? extends Head, ? extends Tail> d, CompressedStore<S> store,
				long index, int modelMove, int logMove, int activity) throws IOException {
			InputStream in = store.getStreamForObject(index);
			((PILPDelegate) d).getTailDeflater().skipHead(in);
			return ((PILPDelegate) d).getTailInflater().inflate(in);
		}

		public int getEstimatedCosts(Delegate<? extends Head, ? extends Tail> d, Head head) {
			// AA : this line means that any moves are assumed to have a cost (after added with epsilon)
			// if this is not the case, this line is incorrect. It has to be replaced by the line afterward.
			// Simply put, the line afterward says that movements without cost is also allowed
			// BVD: Simply return the estimate. This is not the place to tweak it.
			return estimate;
			//Math.max(estimate, ((PHead) head).getParikhVector().getNumElts());
			//return Math.max(estimate, 0);
		}

		public boolean canComplete() {
			return estimate != AbstractPILPDelegateTable.INFEASIBLE_INT;
		}

		public int getEstimate() {
			return estimate;
		}

		public short[] getVariables() {
			return variables;
		}

		public void computeEstimate(Delegate<? extends Head, ? extends Tail> d, Head head, int lastEstimate) {
			if (isExact) {
				return;
			}

			PHeadTable h = (PHeadTable) head;
			AbstractPILPDelegateTable<?> delegate = (AbstractPILPDelegateTable<?>) d;

			LPResult res = delegate.estimate(h.getMarking(), h.getParikhVector());

			int solvedEstimate;
			if (res == null) {
				solvedEstimate = lastEstimate;
			} else if (res.getResult() == AbstractPILPDelegateTable.INFEASIBLE) {
				solvedEstimate = AbstractPILPDelegateTable.INFEASIBLE_INT;
			} else {
				solvedEstimate = (int) (res.getResult() + 0.5);

				if (delegate.isUseInts()) {
					for (int i = 0; i < res.getVariables().length; i++) {
						// if we use integer variables, we can round
						variables[i] = (short) (res.getVariable(i) + 0.5);
					}
				} else {
					for (int i = 0; i < res.getVariables().length; i++) {
						// else round down
						variables[i] = (short) (res.getVariable(i));
					}
				}
			}
			if (solvedEstimate == AbstractPILPDelegateTable.INFEASIBLE_INT || solvedEstimate > lastEstimate) {
				this.estimate = solvedEstimate;
			} else {
				this.estimate = lastEstimate;
			}
			this.isExact = true;
		}

		public boolean isExactEstimateKnown() {
			return isExact;
		}

}
