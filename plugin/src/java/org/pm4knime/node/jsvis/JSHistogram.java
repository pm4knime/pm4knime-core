package org.pm4knime.node.jsvis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.knime.base.data.statistics.HistogramColumn;
//import org.knime.base.data.statistics.HistogramColumn.BinNumberSelectionStrategy;
//import org.knime.base.data.statistics.HistogramModel;
//import org.knime.base.data.statistics.HistogramModel.Bin;
import org.pm4knime.node.jsvis.JSHistogram;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.util.Pair;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Represents a js-histogram (jackson de-/serializable).
 *
 * @author Anastasia Zhukova, KNIME GmbH, Konstanz, Germany
 * @author Martin Horn, KNIME GmbH, Konstanz, Germany
 * @param <B> the type of the bin in the histogram (need to be serializable with jackson!)
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class JSHistogram<B> {

    private final String m_colName;

    private final int m_colIndex;

    private final List<B> m_bins;

    private final int m_maxCount;

    /**
     * Public constructor (and class) for testing only.
     *
     * @param colName column name the histogram is associated with
     * @param colIndex columns index
     * @param bins the actual bins of the histogram
     * @param maxCount count of the largest bin
     */
    @JsonCreator
    public JSHistogram(@JsonProperty("colName") final String colName, @JsonProperty("colIndex") final int colIndex,
        @JsonProperty("bins") final List<B> bins, @JsonProperty("maxCount") final int maxCount) {
        m_colIndex = colIndex;
        m_colName = colName;
        m_bins = bins;
        m_maxCount = maxCount;
    }

    /**
     * @return column index
     */
    public int getColIndex() {
        return m_colIndex;
    }

    /**
     * @return column name
     */
    public String getColumnName() {
        return m_colName;
    }

    /**
     * @return bins
     */
    public List<B> getBins() {
        return m_bins;
    }

    /**
     * @return max value in bins
     */
    public int getMaxCount() {
        return m_maxCount;
    }


    /**
     * Creates a new nominal js-histogram from a column.
     *
     * @param colName Name of the column.
     * @param colIndex Index of the column.
     */
    public static JSHistogram<NominalBin> createNominalHistogram(final String colName, final int colIndex,
        final Map<DataValue, Integer> nomValue) {
//        HistogramColumn hcol = HistogramColumn.getDefaultInstance();
//        HistogramModel<?> hist = hcol.fromNominalModel(nomValue, colIndex, colName);
//        return createNominalHistogram(hist);
    		return null;
    }

    /**
     * Creates a new nominal js-histogram from a {@link HistogramModel}.
     *
     * @param javaHistogram HistogramNominalModel histogram to convert into JSNominalHistogram.
     */
//    static JSHistogram<NominalBin> createNominalHistogram(final HistogramModel<?> hist) {
//        return new JSHistogram<>(hist.getColName(), hist.getColIndex(), convertToNominalBins(hist), hist.getMaxCount());
//    }

//    private static List<NominalBin> convertToNominalBins(final HistogramModel<?> hist) {
//        return hist.getBins().stream().map(b -> {
//            if (b.getDef() instanceof StringValue) {
//                return new NominalBin(b.getCount(), ((StringValue)b.getDef()).getStringValue());
//            } else {
//                return new NominalBin(b.getCount(), "?");
//            }
//        }).collect(Collectors.toList());
//    }

    /**
     * Creates a new numeric js-histogram from a column.
     *
     * @param colName Name of the column.
     * @param colIndex Index of the column.
     * @throws IllegalArgumentException if the histogram model cannot be converted to the javascript histogram
     */
    public static JSHistogram<NumericBin> createNumericHistogram(final String colName, final int colIndex,
        final BufferedDataTable table, final double min, final double max, final double mean, final int numberOfBins,
        final boolean adaptBarsNumber) {
        //HistogramColumn hCol = HistogramColumn.getDefaultInstance().withNumberOfBins(numberOfBins);
//        if (adaptBarsNumber) {
//            hCol =
//                HistogramColumn.getDefaultInstance().withBinSelectionStrategy(BinNumberSelectionStrategy.DecimalRange);
//        }
        //.withNumberOfBins(10).withBinSelectionStrategy(BinNumberSelectionStrategy.DecimalRange);
//        HistogramModel<?> histogram = hCol
//            .histograms(table, new HiLiteHandler(), new double[]{min}, new double[]{max}, new double[]{mean}, colName)
//            .get(0);
        //return createNumericHistogram(histogram, colIndex);
    	return null;
    }

    /**
     * Creates a new numeric js-histogram from a {@link HistogramModel}.
     *
     * @param javaHistogram HistogramNumericModel histogram to convert into JSNumericHistogram.
     * @throws IllegalArgumentException if the histogram model cannot be converted to the javascript histogram
     */
//    static JSHistogram<NumericBin> createNumericHistogram(final HistogramModel<?> hist) {
//        return createNumericHistogram(hist, hist.getColIndex());
//    }
//
//    private static JSHistogram<NumericBin> createNumericHistogram(final HistogramModel<?> hist, final int colIndex) {
//        if (!checkNumericHistogramModel(hist)) {
//            throw new IllegalArgumentException("Histogram at column " + hist.getColName()
//                + " cannot be converted to a JavasScript-Numeric-Histogram.");
//        }
//        return new JSHistogram<NumericBin>(hist.getColName(), colIndex,
//            ((HistogramModel<Pair<Double, Double>>)hist).getBins().stream()
//                .map(b -> new NumericBin(b.getCount(), b.getDef().getFirst(), b.getDef().getSecond()))
//                .collect(Collectors.toList()),
//            hist.getMaxCount());
//    }

    /*
     * Checks whether the histogram model can be converted to a numeric js-histogram.
     * Bins must be of type Pair<Double, Double>.
     */
//    private static <B> boolean checkNumericHistogramModel(final HistogramModel<B> histogram) {
//        List<Bin<B>> bins = histogram.getBins();
//        if (bins.size() > 0 && bins.get(0).getDef() instanceof Pair) {
//            Pair<?, ?> firstPair = (Pair<?, ?>)bins.get(0).getDef();
//            return firstPair.getFirst() instanceof Double && firstPair.getSecond() instanceof Double;
//        }
//        return false;
//    }

    /**
     * Public for testing only.
     */
    @JsonAutoDetect
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public static class NominalBin {
        private final int m_count;

        private final String m_value;

        /**
         * Public for testing only.
         *
         * @param count number of objs in this bin
         * @param value the bin value
         */
        @JsonCreator
        public NominalBin(@JsonProperty("count") final int count, final @JsonProperty("value") String value) {
            m_count = count;
            m_value = value;
        }

        /**
         * @return number of objs in this bin
         */
        public int getCount() {
            return m_count;
        }

        /**
         * @return the bin value
         */
        public String getValue() {
            return m_value;
        }
    }

    /**
     * Public for testing only.
     */
    @JsonAutoDetect
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public static class NumericBin {
        private final int m_count;

        private final double m_min;

        private final double m_max;

        /**
         * Public for testing only.
         *
         * @param count number of objs in this bin
         * @param min lower bound
         * @param max upper bound
         */
        @JsonCreator
        public NumericBin(@JsonProperty("count") final int count, @JsonProperty("min") final double min,
            @JsonProperty("max") final double max) {
            m_count = count;
            m_min = min;
            m_max = max;
        }

        /**
         * @return number of objs in this bin
         */
        public int getCount() {
            return m_count;
        }

        /**
         * @return lower bound
         */
        public double getMin() {
            return m_min;
        }

        /**
         * @return upper bound
         */
        public double getMax() {
            return m_max;
        }
    }

}
