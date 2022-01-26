package org.pm4knime.node.io.log.reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.log.XContextMonitoredInputStream;

@Plugin(name = "Open XES Log File", parameterLabels = { "Filename" }, returnLabels = {
		"Log (single process)" }, returnTypes = { XLog.class })
public class StreamImport extends AbstractImportPlugin {

	public Object importFileStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes,
			File file) throws Exception {
		input = getInputStream(input, filename, file);
		return importFromStream(context, input, filename, fileSizeInBytes, new XFactoryNaiveImpl());
	}

	/**
	 * Holds zip file, if zip file is open.
	 */
	private ZipFile zipFile;
	/**
	 * Holds the name of the zipped file, if input is zip file.
	 */
	private String zipName;

	public StreamImport() {
		zipFile = null;
		zipName = null;
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes,
			XFactory factory) throws Exception {
		context.getFutureResult(0).setLabel(filename);
		// System.out.println("Open file");
		XParser parser;
		if (zipName != null) {
			/*
			 * Stream contains a zip file. Use the name of the zipped file, not of the zip
			 * file itself.
			 */
			filename = zipName;
		}
		/*
		 * Only use MXML parser if the file has th eproper extesnion. In all other
		 * cases, use the XES parser.
		 */
		if (filename.toLowerCase().endsWith(".mxml") || filename.toLowerCase().endsWith(".mxml.gz")) {
			parser = new XMxmlParser(factory);
		} else {
			parser = new XesXmlParser(factory);
		}
		Collection<XLog> logs = null;
		Exception firstException = null;
		String errorMessage = "";
		try {
			logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
		} catch (Exception e) {
			logs = null;
			firstException = e;
			errorMessage = errorMessage + e;
		}
//		if (logs == null || logs.isEmpty()) {
//			// try any other parser
//			for (XParser p : XParserRegistry.instance().getAvailable()) {
//				if (p == parser) {
//					continue;
//				}
//				try {
//					logs = p.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
//					if (logs.size() > 0) {
//						break;
//					}
//				} catch (Exception e1) {
//					// ignore and move on.
//					logs = null;
//					errorMessage = errorMessage + " [" + p.name() + ":" + e1 + "]";
//				}
//			}
//		}

		// Log file has been read from the stream. The zip file (if present) can now be
		// closed.
		if (zipFile != null) {
			zipFile.close();
			zipFile = null;
		}

		// log sanity checks;
		// notify user if the log is awkward / does miss crucial information
		if (logs == null) {
			// context.getFutureResult(0).cancel(false);
			throw new Exception("Could not open log file, possible cause: "/* + errorMessage, */ + firstException);
		}
		if (logs.size() == 0) {
			// context.getFutureResult(0).cancel(false);
			throw new Exception("No processes contained in log!");
		}

		XLog log = logs.iterator().next();
		if (XConceptExtension.instance().extractName(log) == null) {
			/*
			 * Log name not set. Create a default log name.
			 */
			XConceptExtension.instance().assignName(log, "Anonymous log imported from " + filename);
		}

		// if (log.isEmpty()) {
		// throw new Exception("No process instances contained in log!");
		// }

		/*
		 * Set the log name as the name of the provided object.
		 */
		if (context != null) {
			context.getFutureResult(0).setLabel(XConceptExtension.instance().extractName(log));
		}

		return log;

	}

	/**
	 * This method returns an inputStream for a file. Note that the default
	 * implementation returns "new FileInputStream(file);"
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	protected InputStream getInputStream(InputStream stream, String fileName, File file) throws Exception {
		if (fileName.endsWith(".gz") || fileName.endsWith(".xez")) {
			return new GZIPInputStream(stream);
		}
		if (fileName.endsWith(".zip")) {
			// Open zip file.
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry zipEntry = entries.nextElement();
			if (entries.hasMoreElements()) {
				throw new InvalidParameterException("Zipped log files should not contain more than one entry.");
			}
			/*
			 * Store the name of the zipped file. This will override the provided filename
			 * when importing.
			 */
			zipName = zipEntry.getName();
			// Return stream of only entry in zip file.
			// Do not yet close zip file, as the retruend stream still needs to be read.
			return zipFile.getInputStream(zipEntry);
		}
		return stream;
	}

	@Override
	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		// TODO Auto-generated method stub
		return importFromStream(context, input, filename, fileSizeInBytes,
				XFactoryRegistry.instance().currentDefault());
	}

}
