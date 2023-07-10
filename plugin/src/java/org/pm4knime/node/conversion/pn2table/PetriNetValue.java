package org.pm4knime.node.conversion.pn2table;

import javax.swing.Icon;

import org.knime.core.data.DataValue;
import org.knime.core.data.ExtensibleUtilityFactory;

public interface PetriNetValue extends DataValue {

    
    class PetriNetValueUtilityFactory extends ExtensibleUtilityFactory {
        private static final Icon ICON = loadIcon(PetriNetValue.class, "/svg.png");

        PetriNetValueUtilityFactory() {
            super(PetriNetValue.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "Petri Net (Serialized)";
        }
    }
    UtilityFactory UTILITY = new PetriNetUtilityFactory();

//    /**
//     * Returns whether the two data values have the same content.
//     *
//     * @param v1 the first data value
//     * @param v2 the second data value
//     * @return <code>true</code> if both values are equal, <code>false</code> otherwise
//     * @since 3.0
//     */
//    static boolean equalContent(final SvgValue v1, final SvgValue v2) {
//        try (LockedSupplier<SVGDocument> s1 = v1.getDocumentSupplier();
//                LockedSupplier<SVGDocument> s2 = v2.getDocumentSupplier()) {
//            return XmlDomComparer.equals(s1.get(), s2.get(), PetriNetCell.SVG_XML_CUSTOMIZER);
//        }
//    }

    /** Implementations of the meta information of this value class. */
    class PetriNetUtilityFactory extends ExtensibleUtilityFactory {
        /** Singleton icon to be used to display this cell type. */
        private static final Icon ICON = loadIcon(PetriNetValue.class, "/svg.png");

        /** Only subclasses are allowed to instantiate this class. */
        protected PetriNetUtilityFactory() {
            super(PetriNetValue.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Icon getIcon() {
            return ICON;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
            return "Petri Net (Serialized)";
        }
    }
}

