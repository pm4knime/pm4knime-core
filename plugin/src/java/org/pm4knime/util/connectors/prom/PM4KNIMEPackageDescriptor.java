package org.pm4knime.util.connectors.prom;

import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.packages.PackageDescriptor;

/**
 * Placeholder class for pm4knime. This class defines RapidProM as a "prom"
 * package. This is in fact not the case, though to properly load plugins from
 * the source code, the corresponding PluginManager class needs a
 * PackageDescriptor to register the plugin to.
 * 
 * @author svzelst
 *
 */
public class PM4KNIMEPackageDescriptor extends PackageDescriptor {

	private static final String NAME = "pm4knime";
	// TODO: read this from some properties file
	private static final String VERSION = "0.0.1";
	// TODO: use a util function that returns the appropriate value here
	private static final OS OS_VAR = OS.ALL;
	private static final String DESCRIPTION = "Placeholder package for pm4knime";
	private static final String ORGANISATION = "RWTH / Fraunhofer FIT";
	private static final String AUTHOR = "S.J. van Zelst";
	private static final String MAINTAINER = "S.J. van Zelst";
	private static final String LICENSE = "Apache 2.0";
	private static final String URL = "http://pm4knime.github.io";
	private static final String LOGO_URL = "http://www.promtools.org/lib/exe/fetch.php?w=300&tok=d1d68b&media=rapidprom:logo.png";
	private static final String KEYWORDS = "";
	private static final boolean AUTO_INSTALLED = true;
	private static final boolean HAS_PLUGINS = true;
	private static final List<String> DEPENDENCIES = new ArrayList<>();
	private static final List<String> CONFLICTS = new ArrayList<>();

	public PM4KNIMEPackageDescriptor() {
		super(NAME, VERSION, OS_VAR, DESCRIPTION, ORGANISATION, AUTHOR, MAINTAINER, LICENSE, URL, LOGO_URL, KEYWORDS,
				AUTO_INSTALLED, HAS_PLUGINS, DEPENDENCIES, CONFLICTS);
	}

}
