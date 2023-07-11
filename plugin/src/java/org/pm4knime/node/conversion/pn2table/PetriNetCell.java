package org.pm4knime.node.conversion.pn2table;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

//import org.knime.base.data.xml.Element;
//import org.knime.base.data.xml.Node;
//import org.knime.base.data.xml.PetriNetCell;
//import org.knime.base.data.xml.PetriNetCellFactory;
//import org.knime.base.data.xml.SvgImageContent;
//import org.knime.base.data.xml.PetriNetValue;
//import org.knime.base.data.xml.PetriNetCell.SvgSerializer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.image.png.PNGImageCell;

@SuppressWarnings("serial")
public class PetriNetCell extends DataCell {
	
	
    String pnString;
    public static final DataType TYPE = DataType.getType(PetriNetCell.class);

    public static final class PetriNetSerializer implements DataCellSerializer<PetriNetCell> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final PetriNetCell cell, final DataCellDataOutput output) throws IOException {
            try {
                output.writeUTF(cell.getStringValue());
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException("Could not serialize SVG", ex);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public PetriNetCell deserialize(final DataCellDataInput input) throws IOException {
            String s = input.readUTF();
            return new PetriNetCell(s);
        }
    }
    
    PNGImageCell ic;

    private static final Collection<String> SVG_TEXT_CONTENT_NOT_IGNORED_TAGS =
            Arrays.asList("text", "tspan", "textPath");

//    static final XmlDomComparerCustomizer SVG_XML_CUSTOMIZER = new XmlDomComparerCustomizer(
//        ChildrenCompareStrategy.ORDERED) {
//
//        @Override
//        public boolean include(final Node node) {
//            switch (node.getNodeType()) {
//                case Node.TEXT_NODE:
//                    //ignore all text nodes, except the ones in the defined set
//                    return SVG_TEXT_CONTENT_NOT_IGNORED_TAGS.contains(node.getParentNode().getLocalName());
//                case Node.ELEMENT_NODE:
//                    //ignore metadata elements
//                    Element element = (Element)node;
//                    return !"metadata".equals(element.getLocalName());
//                case Node.COMMENT_NODE:
//                    //ignore comments
//                    return false;
//                default:
//                    return true;
//            }
//        }
//    };

//    private SoftReference<String> m_xmlString;

//    private final ReentrantLock m_lock = new ReentrantLock();

//    private final SvgImageContent m_content;


    public static DataCellSerializer<PetriNetCell> getCellSerializer() {
        return new PetriNetSerializer();
    }

    /**
     * Creates a new PetriNetCell by parsing the passed string. It must contain a valid SVG document, including all XML
     * headers.
     *
     * Please consider using {@link PetriNetCellFactory#create(String)} instead of this constructor as the latter dynamically
     * decides if a in-table cell or a blob cell is created (depending on the size).
     *
     * @param xmlString an SVG document
     * @throws IOException if an error occurs while reading the XML string.
     */
    public PetriNetCell(final String xmlString) throws IOException {
    	pnString = xmlString;
    }


    public String toString() {
        return getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        PetriNetCell cell = (PetriNetCell)dc;

        return pnString.equals(cell.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
        return pnString.equals(otherValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
    	return pnString.hashCode();
    }

    public String getStringValue() {
        return pnString;
    }
    
}
