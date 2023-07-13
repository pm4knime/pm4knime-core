package org.pm4knime.node.conversion.pn2table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory.FromComplexString;
import org.knime.core.data.DataCellFactory.FromInputStream;
import org.knime.core.data.DataType;
import org.knime.core.data.convert.DataCellFactoryMethod;
import org.pm4knime.util.PetriNetUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;

public final class PetriNetCellFactory implements FromComplexString, FromInputStream {

    public static final DataType TYPE = DataType.getType(PetriNetCell.class);

    @Override
    @DataCellFactoryMethod(name = "String (Petri Net)")
    public DataCell createCell(final String input) {
    	InputStream stream = new ByteArrayInputStream(input.getBytes());
        try {
        	return createCell(stream);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public DataType getDataType() {
        return TYPE;
    }

    @Override
    @DataCellFactoryMethod(name = "InputStream (SVG)")
    public DataCell createCell(final InputStream input) throws IOException {
    	AcceptingPetriNet anet = PetriNetUtil.importFromStream(input);
    	return new PetriNetCell(anet);
    }

}
