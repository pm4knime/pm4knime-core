package org.pm4knime.node.io.petrinet.writer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;





/**
 * this class is usede to write a Marking for Petri net in a simple way, by using only files
 *  and separated by tab??
 * @author dkf
 *
 */
public class MarkingReaderWriter {
	
	// we need post precoess it with the net and build the markings on it
	public List<String[]> readMarking(String fileName) throws IOException {
		// the first is init and then final marking
		File mFile = new File(fileName);
		if(!mFile.exists()) {
			throw new IOException("file not exist!");
		}
		
		FileReader reader = new FileReader(mFile);
		BufferedReader bReader = new BufferedReader(reader);
		String line = bReader.readLine();
		// we can only get the id for one place
		List<String[]> mList = new ArrayList<>();
		int idx = 0;
		while(line != null) {
			
			// get the init marking
			String[] mPlaceId = line.split(";");
			// split the string with : and then 
			
			mList.add(idx++, mPlaceId);
			line = bReader.readLine();
		}
		reader.close();
		bReader.close();
		return mList;
	}
	
	public void writeMarking(List<Marking> markings, String fileName) throws IOException {
		// when store in the form of bytes and check if we can recover it later?
		
			File mFile = new File(fileName);
			FileWriter writer = new FileWriter(mFile);
			if(!mFile.exists()) {
				mFile.createNewFile();
			}
			
			BufferedWriter bwriter  = new BufferedWriter(writer);
		
		Marking initMarking = markings.get(0);
		// just to output ids but connect to the net before we create the inital marking
		for(Place p: initMarking) {
			bwriter.write(p.getId().toString());
			bwriter.write(";");
		}
		bwriter.newLine();
		
		Marking finalMarking = markings.get(1);
		// just to output ids but connect to the net before we create the inital marking
		for(Place p: finalMarking) {
			bwriter.write(p.getId().toString());
			bwriter.write(";");
		}
		bwriter.newLine();
		
		bwriter.flush();
		writer.close();
		bwriter.close();
		
	}
	
	public Marking connect2Net(String[] placeId, Petrinet net){
		Marking marking = new Marking();
		
		for(Place place : net.getPlaces()) {
			for(String pID : placeId) {
				if(place.getId().toString().equals(pID))
					marking.add(place);
			}
		}
		return marking;
		
	}
	
	public Object[] init() {
		
		Petrinet net = PetrinetFactory.newPetrinet("net");
		Place pStart = net.addPlace("start");
		Place p1 = net.addPlace("p1");
		Place p2 = net.addPlace("p2");
		Place pEnd = net.addPlace("end");
		Transition tA = net.addTransition("A");
		Transition tB = net.addTransition("B");
		Transition tC = net.addTransition("C");
		net.addArc(pStart, tA);
		net.addArc(tA, p1);
		net.addArc(p1, tB);
		net.addArc(tB, p2);
		net.addArc(p2, tC);
		net.addArc(tC, pEnd);
		
		Marking initMarking = new Marking();
		initMarking.add(pStart);
		initMarking.add(pStart);
		Marking finalMarking = new Marking();
		finalMarking.add(pEnd);
		finalMarking.add(p2);
		return new Object[] {net, initMarking, finalMarking};
		
	}
	
	public static void main(String[] args) {
		// build a Petri net and then write and read the data in it 
		MarkingReaderWriter rw = new MarkingReaderWriter();
		Object[] result =rw.init();
		Petrinet net = (Petrinet) result[0];
		Marking iMarking = (Marking) result[1];
		Marking fMarking = (Marking) result[2];
		
		String fileName = "test-marking-rw.txt";
		List<Marking> mList = new ArrayList<>();
		mList.add(iMarking);
		mList.add(fMarking);
		
		try {
			rw.writeMarking(mList, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("write marking to file");
		
		System.out.println("begin reading marking from file");
		try {
			List<String[]> mId = rw.readMarking(fileName);
			
			Marking riMarking = rw.connect2Net(mId.get(0), net);
			Marking wiMarking = rw.connect2Net(mId.get(1), net);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end reading");
	}
	
}
