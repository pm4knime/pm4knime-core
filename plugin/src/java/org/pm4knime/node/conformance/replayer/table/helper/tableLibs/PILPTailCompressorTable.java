package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import nl.tue.astar.Head;
import nl.tue.astar.impl.AbstractCompressor;
import nl.tue.astar.impl.memefficient.StorageAwareDelegate;
import nl.tue.astar.impl.memefficient.TailInflater;
import nl.tue.storage.compressor.BitMask;

public class PILPTailCompressorTable extends AbstractCompressor<PILPTailTable> implements TailInflater<PILPTailTable> {
	
	protected final short places;
	protected final short activities;
	//	private final int columns;
	//	private final int bytesColumns;
	protected final int bytesPlaces;
	protected final int bytesActivities;
	//	private final int maxBytes;

	private int variables;

	public PILPTailCompressorTable(int columns, short places, short activities) {
		//		this.columns = columns;
		this.places = places;
		this.activities = activities;
		//		this.bytesColumns = BitMask.getNumBytes(columns);
		this.bytesPlaces = BitMask.getNumBytes(places);
		this.bytesActivities = BitMask.getNumBytes(activities);
		//		this.maxBytes = 8 + bytesColumns + columns * 2;

		this.variables = columns;

	}

	public void deflate(PILPTailTable tail, OutputStream stream) throws IOException {

		short[] vars = tail.getVariables();
		byte[] toWrite = new byte[variables];

		writeBooleanToByteArray(stream, tail.isExactEstimateKnown());
		writeIntToByteArray(stream, tail.getEstimate());

		if (tail.isExactEstimateKnown()) {

			for (int i = 0; i < variables; i++) {
				int val = vars[i] & 0xFFFF;
				byte b = (byte) ((val >> 8) & 0xFF);
				stream.write(b);
				b = (byte) (val & 0xFF);
				stream.write(b);
			}
			//			writeTo(toWrite, vars, 16);

			//			BitMask mask = makeShortListBitMask(columns, tail.getVariables());
			//			stream.write(mask.getBytes());
			//			for (int i = 0; i < columns; i++) {
			//				if (tail.getVariables()[i] > 0) {
			//					writeShortToByteArray(stream, tail.getVariables()[i]);
			//				}
			//			}
		} else {
			stream.write(new byte[2 * variables]);
		}
	}

	public PILPTailTable inflate(InputStream stream) throws IOException {
		//		int est = readIntFromStream(stream);
		//
		//		// read the marking
		//		BitMask mask = readMask(stream, columns, bytesColumns);
		//		short[] variables = new short[columns];
		//		for (int i : BitMask.getIndices(mask)) {
		//			//variables[i] = readDoubleFromStream(stream);
		//			variables[i] = readShortFromStream(stream);
		//		}
		//		return new PILPTail(est, variables);

		boolean exact = readBooleanFromStream(stream);
		int est = readIntFromStream(stream);
		short[] vars = new short[variables];
		if (exact) {

			byte[] toRead = new byte[2 * variables];
			stream.read(toRead);
			for (int i = 0, j = 0; i < variables; i++) {
				vars[i] = (short) (toRead[j++] << 8 + toRead[j++]);

			}
		}

		return new PILPTailTable(est, vars, exact);
	}

	public void skipHead(InputStream stream) throws IOException {
		stream.skip(4);
		BitMask mask = readMask(stream, places, bytesPlaces);
		stream.skip(2 * mask.getOnes());
		mask = readMask(stream, activities, bytesActivities);
		stream.skip(2 * mask.getOnes());

	}

	public int getMaxByteCount() {
		//		return maxBytes;
		return 1 + 4 + 2 * variables;
	}

	public <H extends Head> int inflateEstimate(StorageAwareDelegate<H, PILPTailTable> delegate, H head, InputStream stream)
			throws IOException {
		skipHead(stream);
		stream.skip(1);
		return readIntFromStream(stream);
	}
}
