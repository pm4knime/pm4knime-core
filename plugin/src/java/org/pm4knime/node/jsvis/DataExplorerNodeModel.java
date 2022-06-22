package org.pm4knime.node.jsvis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.knime.base.data.statistics.HistogramColumn;
//import org.knime.base.data.statistics.HistogramColumn.BinNumberSelectionStrategy;
//import org.knime.base.data.statistics.HistogramModel;
import org.knime.base.data.statistics.Statistic;
import org.knime.base.data.statistics.StatisticCalculator;
import org.knime.base.data.statistics.calculation.DoubleMinMax;
import org.knime.base.data.statistics.calculation.Kurtosis;
import org.knime.base.data.statistics.calculation.Mean;
import org.knime.base.data.statistics.calculation.Median;
import org.knime.base.data.statistics.calculation.MissingValue;
import org.knime.base.data.statistics.calculation.NominalValue;
import org.knime.base.data.statistics.calculation.Skewness;
import org.knime.base.data.statistics.calculation.SpecialDoubleCells;
import org.knime.base.data.statistics.calculation.StandardDeviation;
import org.knime.base.data.statistics.calculation.Sum;
import org.knime.base.data.statistics.calculation.Variance;
import org.knime.base.data.statistics.calculation.ZeroNumber;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.ExtensibleUtilityFactory;
import org.knime.core.data.LongValue;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.wizard.CSSModifiable;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.JSONDataTable.JSONDataTableRow;
import org.knime.js.core.JSONDataTableSpec;
import org.knime.js.core.JSONDataTableSpec.JSTypes;
import org.knime.js.core.node.AbstractWizardNodeModel;
import org.pm4knime.node.jsvis.JSHistogram.NominalBin;
import org.pm4knime.node.jsvis.JSHistogram.NumericBin;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 * @author Anastasia Zhukova, KNIME GmbH, Konstanz, Germany
 */
public class DataExplorerNodeModel extends AbstractWizardNodeModel<DataExplorerNodeRepresentation,
        DataExplorerNodeValue> implements CSSModifiable {

    private DataExplorerConfig m_config;

//    private List<HistogramModel<?>> m_javaNumericHistograms;
//    private List<HistogramModel<?>> m_javaNominalHistograms;

    private String MISSING_VALUE_STRING = "?";

    public enum TableId {
        NUMERIC ("numeric"),
        NOMINAL ("nominal"),
        PREVIEW ("preview");

        private final String name;

        private TableId(final String s) {
            name = s;
        }

        @Override
        public String toString() {
            return this.name;
         }
    }

    /**
     * @param viewName
     */
    protected DataExplorerNodeModel(final String viewName) {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE}, viewName);
        m_config = new DataExplorerConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataExplorerNodeRepresentation createEmptyViewRepresentation() {
        return new DataExplorerNodeRepresentation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataExplorerNodeValue createEmptyViewValue() {
        return new DataExplorerNodeValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec inputSpec = (DataTableSpec)inSpecs[0];

        if (m_config.getDisplayRowNumber() < 1) {
            int def = DataExplorerConfig.DEFAULT_DISPLAY_ROW_NUMBER;
            setWarningMessage("Number of rows for data preview must be greater than 0. Using default of " + def + ".");
            m_config.setDisplayRowNumber(def);
        }

        if (m_config.getInitialPageSize() == 0) {
            int def = DataExplorerConfig.DEFAULT_INITIAL_PAGE_SIZE;
            setWarningMessage("Initial page size must be greater than 0. Using default of " + def + ".");
            m_config.setInitialPageSize(def);
        }

        PortObjectSpec[] out = new PortObjectSpec[]{inputSpec};
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "org_knime_base_node_stats_dataexplorer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHideInWizard() {
        return m_config.getHideInWizard();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHideInWizard(final boolean hide) {
        m_config.setHideInWizard(hide);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCssStyles() {
        return m_config.getCustomCSS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCssStyles(final String styles) {
        m_config.setCustomCSS(styles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationError validateViewValue(final DataExplorerNodeValue viewContent) {
        /*always valid */
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCurrentValue(final NodeSettingsWO content) { /* not used */ }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] performExecute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataTable table = (BufferedDataTable)inObjects[0];
        DataExplorerNodeRepresentation rep = getViewRepresentation();
        double subProgress = 0.8;
        ColumnRearranger rearranger = new ColumnRearranger(table.getDataTableSpec());
        if (rep.getStatistics() == null) {
            subProgress = 0.1;
            rep.setStatistics(calculateStatistics((BufferedDataTable)inObjects[0], exec.createSubExecutionContext(0.8)));
            rep.setNominal(calculateNominal((BufferedDataTable)inObjects[0], exec.createSubExecutionContext(0.2)));
            rep.setDataPreview(calculatePreview((BufferedDataTable)inObjects[0]));
            copyConfigToRepresentation();
        }
        DataExplorerNodeValue val = getViewValue();
        BufferedDataTable result = table;
        String[] filterCols = val.getSelection();
        if (filterCols != null && filterCols.length > 0) {
            rearranger.remove(filterCols);
            result = exec.createColumnRearrangeTable(table, rearranger, exec.createSubProgress(subProgress));
        }

        return new PortObject[]{result};
    }

    private JSONDataTable createJSONTable(final TableId tID, final JSONDataTableRow[] tableRows, final BufferedDataTable table){
        JSONDataTable jTable = new JSONDataTable();

        switch(tID) {
            case NUMERIC:
                jTable.setSpec(createStatsJSONSpecNumeric(tableRows.length));
                if (tableRows.length == 0) {
                    getViewRepresentation().setJsNumericHistograms(new ArrayList<>(0));
                }
                break;
            case NOMINAL:
                jTable.setSpec(createStatsJSONSpecNominal(tableRows.length));
                if (tableRows.length == 0) {
                    getViewRepresentation().setMaxNomValueReached(new String[0]);
                    getViewRepresentation().setJsNominalHistograms(new ArrayList<>(0));
                }
                break;
            case PREVIEW:
                jTable.setSpec(createStatsJSONSpecPreview(table));
                //jTable.getSpec().
                break;
        }
        jTable.setId(tID.toString());
        jTable.setRows(tableRows);

        return jTable;
    }

    /**
     * @param bufferedDataTable
     * @return
     */
    private JSONDataTable calculateNominal(final BufferedDataTable table, final ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException {
        DataTableSpec spec = table.getSpec();
        List<String> nominalCols = new ArrayList<String>();
        for (DataColumnSpec columnSpec : spec) {
            if (columnSpec.getType().isCompatible(org.knime.core.data.NominalValue.class)) {
                nominalCols.add(columnSpec.getName());
            }
        }

        String[] includeColumns = nominalCols.toArray(new String[0]);
        if (includeColumns.length == 0) {
            return createJSONTable(TableId.NOMINAL, new JSONDataTableRow[includeColumns.length],  null);
        }
        List<Statistic> statistics = new ArrayList<Statistic>();
        NominalValue nominal = new NominalValue(m_config.getMaxNominalValues(), nominalCols.toArray(new String[0]));
        statistics.add(nominal);
        MissingValue missing = new MissingValue(includeColumns);
        statistics.add(missing);
        StatisticCalculator calc = new StatisticCalculator(spec, statistics.toArray(new Statistic[0]));

        //if some columns exceeded the set number of max unique values, prepare an array of such columns
        List<String> errorSet = new ArrayList<String>();
        String nominalEvaluationResults = calc.evaluate(table, exec.createSubExecutionContext(0.5));
        if (nominalEvaluationResults != null) {
            String[] errors = nominalEvaluationResults.split(":");
            String[] errorsClean = errors[errors.length - 1].replaceAll("('\"|\"'|\"|\\n)", "").split(",");
            //getViewRepresentation().setMaxNomValueReached(errorsClean);
            for (int i = 0; i < errorsClean.length; i++) {
                String a = errorsClean[i].substring(0, 1);
                if (a.chars().allMatch(Character::isWhitespace)) {
                    errorsClean[i] = errorsClean[i].substring(1, errorsClean[i].length());
                }
            }
            errorSet.addAll(Arrays.asList(errorsClean));
        } else {
            getViewRepresentation().setMaxNomValueReached(new String[0]);
        }


        List<JSHistogram<NominalBin>> jsHistograms = new ArrayList<>();
        //m_javaNominalHistograms = new ArrayList<HistogramModel<?>>();
        JSONDataTableRow[] rows = new JSONDataTableRow[includeColumns.length];

        //if values in nom column don't have 2*freqNumber values, then put them in all

        DataValue[] freq = null;
        DataValue[] infreq = null;
        DataValue[] all = null;
        List<String> outputAllValues = null;



        for (int i = 0; i < includeColumns.length; i++) {
            String col = includeColumns[i];

            List<Object> rowValues = new ArrayList<Object>();
            int missingNum = missing.getNumberMissingValues(col);
            if (table.size() == 0 && missingNum == 0) {
                rowValues.add(null);
            } else {
                rowValues.add(missingNum);
            }


            Map<DataValue, Integer> nomValue = nominal.getNominalValues(i);

            //artificial add of missing value, when it is identified but not added to all values
            if (missingNum != 0 && errorSet.contains(col) && nomValue.size() == m_config.getMaxNominalValues()) {
                nomValue.put(new MissingCell(MISSING_VALUE_STRING), missingNum);
            }
            //rowValues.add(nomValue.size());

            //exclude missing values for most freq values calculation
            Map<DataValue, Integer> nomValueMissingExcl = new HashMap<DataValue, Integer>(nomValue);
            Set<DataValue> keySet = nomValueMissingExcl.keySet();
            if (keySet.contains(new MissingCell(MISSING_VALUE_STRING))) {
                nomValueMissingExcl.remove(new MissingCell(MISSING_VALUE_STRING));
            }



            //number of unique values excludes missing value
            if (table.size() == 0 && nomValueMissingExcl.size() == 0) {
                rowValues.add(null);
            } else {
                rowValues.add(nomValueMissingExcl.size());
            }


            //now sort values by freq
            Map<DataValue, Integer> sortedNomValuesMissingExcl = sortByValue(nomValueMissingExcl);

            all = new DataValue[Math.min(2 * m_config.getFreqValuesNumber() - 1, sortedNomValuesMissingExcl.size())];
            freq = new DataValue[m_config.getFreqValuesNumber()];
            infreq = new DataValue[m_config.getFreqValuesNumber()];

            if (nomValueMissingExcl.keySet().size() > all.length) {
                System.arraycopy(sortedNomValuesMissingExcl.keySet().toArray(new DataValue[0]), 0, freq, 0, freq.length);
                System.arraycopy(sortedNomValuesMissingExcl.keySet().toArray(new DataValue[0]), sortedNomValuesMissingExcl.keySet().size() - freq.length, infreq, 0, infreq.length);
            } else {
                System.arraycopy(sortedNomValuesMissingExcl.keySet().toArray(new DataValue[0]), 0, all, 0, all.length);
            }

            //create an output list
            outputAllValues = new ArrayList<String>();
            if (errorSet.contains(col)) {
                //abnormal case
                outputAllValues = formAllValuesColumn(freq, infreq, all, DataExplorerConfig.DEFAULT_OTHER_ERROR_VALUES_NOTATION, null);
            } else {
                //normal case
                outputAllValues = formAllValuesColumn(freq, infreq, all, DataExplorerConfig.DEFAULT_OTHER_VALUES_NOTATION, col);
            }
            if (table.size() == 0) {
                rowValues.add(null);
            } else {
                rowValues.add(outputAllValues.toArray());
            }


            //if we want to include missing values, than do it on the whole set of nominals
            if (m_config.getMissingValuesInHist()) {
                //m_javaNominalHistograms.add(calculateNominalHistograms(nomValue, i, col));
                jsHistograms.add(JSHistogram.createNominalHistogram(col, i, nomValue));
            } else {
                //m_javaNominalHistograms.add(calculateNominalHistograms(nomValueMissingExcl, i, col));
                jsHistograms.add(JSHistogram.createNominalHistogram(col, i, nomValueMissingExcl));
            }

            rows[i] = new JSONDataTableRow(col, rowValues.toArray(new Object[0]));
        }

        getViewRepresentation().setMaxNomValueReached(errorSet.toArray(new String[0]));

        getViewRepresentation().setJsNominalHistograms(table.size() == 0? null : jsHistograms);

        return createJSONTable(TableId.NOMINAL, rows, null);
    }

    private List<String> formAllValuesColumn (final DataValue[] freq, final DataValue[] infreq, final DataValue[] all, final String infoMessage, final String col) {
        List<String> output = new ArrayList<String>();
        if (unwrapDataValueArray(all).length != 0) {
            output.addAll(Arrays.asList(unwrapDataValueArray(all)));
            if (infoMessage == DataExplorerConfig.DEFAULT_OTHER_ERROR_VALUES_NOTATION) {
                output.add(infoMessage);
            }
        } else {
            output.addAll(Arrays.asList(unwrapDataValueArray(freq)));
            output.add(infoMessage);
            output.addAll(Arrays.asList(unwrapDataValueArray(infreq)));
        }
        return output;
    }

    //adopted from https://www.mkyong.com/java/how-to-sort-a-map-in-java/
    private static Map<DataValue, Integer> sortByValue(final Map<DataValue, Integer> unsortMap) {
        List<Map.Entry<DataValue, Integer>> list =
                new LinkedList<Map.Entry<DataValue, Integer>>(unsortMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<DataValue, Integer>>() {
            @Override
            public int compare(final Map.Entry<DataValue, Integer> o1,
                               final Map.Entry<DataValue, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        Map<DataValue, Integer> sortedMap = new LinkedHashMap<DataValue, Integer>();
        for (Map.Entry<DataValue, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private String[] unwrapDataValueArray (final DataValue[] dataValueArray) {
        String[] output = new String[dataValueArray.length];
        for (int i = 0; i < dataValueArray.length; i++) {
            if (dataValueArray[i] == null) {
                return new String[0];
            }
            output[i] = dataValueToString(dataValueArray[i]);
        }
        return output;
    }

    private String dataValueToString (final DataValue dataValue) {
        if (dataValue instanceof StringCell) {
            return ((StringCell)dataValue).getStringValue();
        }
        return MISSING_VALUE_STRING;
    }

    private JSONDataTable calculateStatistics(final BufferedDataTable table, final ExecutionContext exec) throws InvalidSettingsException, CanceledExecutionException {
        DataTableSpec spec = table.getSpec();
        List<String> doubleCols = new ArrayList<String>();
        for (DataColumnSpec columnSpec : spec) {
            if (columnSpec.getType().isCompatible(DoubleValue.class)) {
                doubleCols.add(columnSpec.getName());
            }
        }
        String[] includeColumns = doubleCols.toArray(new String[0]);
        if (includeColumns.length == 0) {
            return createJSONTable(TableId.NUMERIC, new JSONDataTableRow[includeColumns.length],  null);
        }
        List<Statistic> statistics = new ArrayList<Statistic>();
        DoubleMinMax minMax = new DoubleMinMax(true, includeColumns);
        statistics.add(minMax);
        Mean mean = new Mean(includeColumns);
        statistics.add(mean);
        StandardDeviation stdDev = new StandardDeviation(includeColumns);
        statistics.add(stdDev);
        Variance variance = new Variance(includeColumns);
        statistics.add(variance);
        Skewness skewness = new Skewness(includeColumns);
        statistics.add(skewness);
        Kurtosis kurtosis = new Kurtosis(includeColumns);
        statistics.add(kurtosis);
        Sum sum = new Sum(includeColumns);
        statistics.add(sum);
        ZeroNumber zeros = new ZeroNumber(includeColumns);
        statistics.add(zeros);
        MissingValue missing = new MissingValue(includeColumns);
        statistics.add(missing);
        SpecialDoubleCells spDouble = new SpecialDoubleCells(includeColumns);
        statistics.add(spDouble);
        Median median = new Median(includeColumns);
        if (m_config.getShowMedian()) {
            statistics.add(median);
        }

        StatisticCalculator calc = new StatisticCalculator(spec, statistics.toArray(new Statistic[0]));
        calc.evaluate(table, exec.createSubExecutionContext(0.5));

        JSONDataTableRow[] rows = new JSONDataTableRow[includeColumns.length];
        //JSONDataTableSpec jSpec = createStatsJSONSpecNumeric(includeColumns.length);
        //JSONDataTable jTable = new JSONDataTable();
        //jTable.setSpec(jSpec);

        for (int i = 0; i < includeColumns.length; i++) {
            String col = includeColumns[i];
            List<Object> rowValues = new ArrayList<Object>();
            double min = minMax.getMin(col);
            double max = minMax.getMax(col);
            rowValues.add(min);
            rowValues.add(max);
            Double dMean = mean.getResult(col);
            rowValues.add(dMean.isNaN() ? null : dMean);
            if (m_config.getShowMedian()) {
                DataCell med = median.getMedian(col);
                rowValues.add(med.isMissing() ? null : ((DoubleValue)med).getDoubleValue());
            }
            Double dDev = stdDev.getResult(col);
            rowValues.add(dDev.isNaN() ? null : dDev);
            Double dVar = variance.getResult(col);
            rowValues.add(dVar.isNaN() ? null : dVar);
            Double dSkew = skewness.getResult(col);
            rowValues.add(dSkew.isNaN() ? null : dSkew);
            Double dKurt = kurtosis.getResult(col);
            rowValues.add(dKurt.isNaN() ? null : dKurt);
            Double dSum = sum.getResult(col);
            rowValues.add(dSum.isNaN() ? null : dSum);
            rowValues.add(zeros.getNumberZeroValues(col));
            rowValues.add(missing.getNumberMissingValues(col));
            rowValues.add(spDouble.getNumberNaNValues(col));
            rowValues.add(spDouble.getNumberPositiveInfiniteValues(col));
            rowValues.add(spDouble.getNumberNegativeInfiniteValues(col));
            rows[i] = new JSONDataTableRow(col, rowValues.toArray(new Object[0]));
        }

        //jTable.setRows(rows);
        //jTable.setId("numeric");

//        Map<Integer, ? extends HistogramModel<?>> javaHistograms = calculateNumericHistograms(table, exec.createSubExecutionContext(0.5), minMax, mean, includeColumns);
//        m_javaNumericHistograms = new ArrayList<HistogramModel<?>>();
//        m_javaNumericHistograms.addAll(javaHistograms.values());

        List<JSHistogram<NumericBin>> jsHistograms = new ArrayList<>();

        //TODO is it an optimal solution to use just one boolean for estimation of empty table treatment?
        boolean missingMinMax = false;
        for (int i = 0; i < includeColumns.length; i++) {
            if (Double.isNaN(minMax.getMin(includeColumns[i]))) {
                missingMinMax = true;
            } else {
                JSHistogram histTest =
                    JSHistogram.createNumericHistogram(includeColumns[i], i, table, minMax.getMin(includeColumns[i]),
                        minMax.getMax(includeColumns[i]), mean.getResult(includeColumns[i]),
                        m_config.getNumberOfHistogramBars(), m_config.getAdaptNumberOfHistogramBars());
                jsHistograms.add(histTest);
            }
        }
        if (missingMinMax) {
            getViewRepresentation().setJsNumericHistograms(null);
        } else {
            getViewRepresentation().setJsNumericHistograms(jsHistograms);
        }
        return createJSONTable(TableId.NUMERIC, rows,  null);
    }

    private JSONDataTable calculatePreview (final BufferedDataTable table) {

        JSONDataTableRow[] rows = new JSONDataTableRow[m_config.getDisplayRowNumber()];
        Set<String> warningMessage = new HashSet<String>();
        int numCol = table.getDataTableSpec().getNumColumns();
        int i = 0;
        for (DataRow row : table) {
            if (i == m_config.getDisplayRowNumber()) {
                break;
            }
            List<Object> rowValues = new ArrayList<Object>();
            for (int j = 0; j < numCol; j++) {
                DataCell cell = row.getCell(j);
                if (cell instanceof DoubleValue) {
                    rowValues.add(((DoubleValue)cell).getDoubleValue());
                } else if (cell instanceof org.knime.core.data.NominalValue) {
                    rowValues.add(((StringValue)cell).getStringValue());
                } else {
                    if (cell instanceof MissingCell) {
                        rowValues.add(null);
                    } else {
                        rowValues.add("Non-generic");
                        warningMessage.add(table.getDataTableSpec().getColumnSpec(j).getName());
                    }
                }
            }

            rows[i] = new JSONDataTableRow(row.getKey().getString(), rowValues.toArray(new Object[0]));
            i++;
        }
        if (!warningMessage.isEmpty()) {
            setWarningMessage("The following columns have non-generic type and will be excluded from  calculation: \n"+ warningMessage.toString());
            //TODO figure out if we need to see this warning also in the view
        }

//        JSONDataTable jTable = new JSONDataTable();
//        JSONDataTableSpec jSpec = createStatsJSONSpecPreview(table);
//        jTable.setSpec(jSpec);
//        jTable.setRows(rows);
//        jTable.setId("preview");
        return createJSONTable(TableId.PREVIEW, rows, table);
    }

    private JSONDataTableSpec createStatsJSONSpecPreview (final BufferedDataTable table) throws IllegalArgumentException {
        if (table == null) {
            throw new IllegalArgumentException("DataTable is null: can't extract spec for JS Preview table.");
        }
        return new JSONDataTableSpec(table.getDataTableSpec(), m_config.getDisplayRowNumber());
    }

    private JSONDataTableSpec createStatsJSONSpecNominal(final int numColumns) {
        String knimeInt = ((ExtensibleUtilityFactory)LongValue.UTILITY).getName();
        String knimeString = ((ExtensibleUtilityFactory)StringValue.UTILITY).getName();
        JSONDataTableSpec spec = new JSONDataTableSpec();
        List<String> colNames = new ArrayList<String>();
        List<String> knimeTypes = new ArrayList<String>();
        colNames.add(DataExplorerConfig.MISSING);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.UNIQUE_NOMINAL);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.ALL_NOMINAL_VAL);
        knimeTypes.add(knimeString);

        JSTypes[] colTypes = new JSTypes[knimeTypes.size()];
        for (int i = 0; i < knimeTypes.size(); i++) {
            if (knimeTypes.get(i) == knimeInt) {
                colTypes[i] = JSTypes.NUMBER;
            } else {
                colTypes[i] = JSTypes.STRING;
            }
        }
        spec.setNumColumns(colNames.size());
        spec.setColNames(colNames.toArray(new String[0]));
        spec.setColTypes(colTypes);
        spec.setKnimeTypes(knimeTypes.toArray(new String[0]));
        spec.setNumRows(numColumns);
        return spec;
    }

    private JSONDataTableSpec createStatsJSONSpecNumeric(final int numColumns) {
        String knimeDouble = ((ExtensibleUtilityFactory)DoubleValue.UTILITY).getName();
        String knimeInt = ((ExtensibleUtilityFactory)LongValue.UTILITY).getName();
        JSONDataTableSpec spec = new JSONDataTableSpec();
        List<String> colNames = new ArrayList<String>();
        List<String> knimeTypes = new ArrayList<String>();
        colNames.add(DataExplorerConfig.MIN);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.MAX);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.MEAN);
        knimeTypes.add(knimeDouble);
        if (m_config.getShowMedian()) {
            colNames.add(DataExplorerConfig.MEDIAN);
            knimeTypes.add(knimeDouble);
        }
        colNames.add(DataExplorerConfig.STD_DEV);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.VARIANCE);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.SKEWNESS);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.KURTOSIS);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.SUM);
        knimeTypes.add(knimeDouble);
        colNames.add(DataExplorerConfig.ZEROS);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.MISSING);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.NAN);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.P_INFINITY);
        knimeTypes.add(knimeInt);
        colNames.add(DataExplorerConfig.N_INFINITY);
        knimeTypes.add(knimeInt);

        JSTypes[] colTypes = new JSTypes[colNames.size()];
        Arrays.fill(colTypes, JSTypes.NUMBER);

        spec.setNumColumns(colNames.size());
        spec.setColNames(colNames.toArray(new String[0]));
        spec.setColTypes(colTypes);
        spec.setKnimeTypes(knimeTypes.toArray(new String[0]));
        spec.setNumRows(numColumns);
        return spec;
    }

//   private HistogramModel<?> calculateNominalHistograms(final Map<? extends DataValue, Integer> counts, final int colIndex,
//       final String colName) {
//       HistogramColumn hCol = HistogramColumn.getDefaultInstance();
//       return hCol.fromNominalModel(counts, colIndex, colName);
//   }
//
//    private Map<Integer, ? extends HistogramModel<?>> calculateNumericHistograms(final BufferedDataTable table,
//        final ExecutionContext exec, final DoubleMinMax minMax, final Mean mean, final String[] includeColumns) {
//        HistogramColumn hCol = HistogramColumn.getDefaultInstance()
//               .withNumberOfBins(m_config.getNumberOfHistogramBars());
//       if (m_config.getAdaptNumberOfHistogramBars()) {
//           hCol = HistogramColumn.getDefaultInstance().withBinSelectionStrategy(BinNumberSelectionStrategy.DecimalRange);
//       }
//       int noCols = includeColumns.length;
//       double[] mins = new double[noCols];
//       double[] maxs = new double[noCols];
//       double[] means = new double[noCols];
//       for (int i = 0; i < noCols; i++) {
//           mins[i] = minMax.getMin(includeColumns[i]);
//           maxs[i] = minMax.getMax(includeColumns[i]);
//           means[i] = mean.getResult(includeColumns[i]);
//       }
//       return hCol.histograms(table, new HiLiteHandler(), mins, maxs, means, includeColumns);
//   }

    private void copyConfigToRepresentation() {
        synchronized(getLock()) {
            DataExplorerNodeRepresentation viewRepresentation = getViewRepresentation();
            viewRepresentation.setEnablePaging(m_config.getEnablePaging());
            viewRepresentation.setInitialPageSize(m_config.getInitialPageSize());
            viewRepresentation.setEnablePageSizeChange(m_config.getEnablePageSizeChange());
            viewRepresentation.setAllowedPageSizes(m_config.getAllowedPageSizes());
            viewRepresentation.setPageSizeShowAll(m_config.getPageSizeShowAll());
            viewRepresentation.setEnableJumpToPage(m_config.getEnableJumpToPage());
            viewRepresentation.setDisplayRowIds(m_config.getDisplayRowIds());
            viewRepresentation.setDisplayColumnHeaders(m_config.getDisplayColumnHeaders());
            viewRepresentation.setFixedHeaders(m_config.getFixedHeaders());
            viewRepresentation.setTitle(m_config.getTitle());
            viewRepresentation.setSubtitle(m_config.getSubtitle());
            viewRepresentation.setEnableSelection(m_config.getEnableSelection());
            viewRepresentation.setEnableSearching(m_config.getEnableSearching());
            viewRepresentation.setEnableSorting(m_config.getEnableSorting());
            viewRepresentation.setEnableClearSortButton(m_config.getEnableClearSortButton());
            viewRepresentation.setEnableGlobalNumberFormat(m_config.getEnableGlobalNumberFormat());
            viewRepresentation.setGlobalNumberFormatDecimals(m_config.getGlobalNumberFormatDecimals());
            viewRepresentation.setDisplayFullscreenButton(m_config.getDisplayFullscreenButton());
            viewRepresentation.setDisplayMissingValueAsQuestionMark(m_config.getDisplayMissingValueAsQuestionMark());
            viewRepresentation.setDisplayRowNumber(m_config.getDisplayRowNumber());
            viewRepresentation.setEnableFreqValDisplay(m_config.getEnableFreqValDisplay());
            viewRepresentation.setFreqValues(m_config.getFreqValuesNumber());
            viewRepresentation.setMissingValuesInHist(m_config.getMissingValuesInHist());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performReset() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void useCurrentValueAsDefault() {
        DataExplorerNodeValue value = getViewValue();
        m_config.setInitialPageSize(value.getPageSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_config.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        (new DataExplorerConfig()).loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_config.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        super.saveInternals(nodeInternDir, exec);
        File hNumFile = new File(nodeInternDir, "numericHistograms.xml.gz");
        File hNomFile = new File(nodeInternDir, "nominalHistograms.xml.gz");

//        if (m_javaNumericHistograms != null) {
//            Map<Integer, HistogramModel<?>> hNumMap = new HashMap<Integer, HistogramModel<?>>();
//            for (HistogramModel<?> histogramModel : m_javaNumericHistograms) {
//                hNumMap.put(histogramModel.getColIndex(), histogramModel);
//            }
//            HistogramColumn.saveHistogramData(hNumMap, hNumFile);
//        }
//
//        if (m_javaNominalHistograms != null) {
//            Map<Integer, HistogramModel<?>> hNomMap = new HashMap<Integer, HistogramModel<?>>();
//            for (HistogramModel<?> histogramModel : m_javaNominalHistograms) {
//                hNomMap.put(histogramModel.getColIndex(), histogramModel);
//            }
//            HistogramColumn.saveNominalHistogramData(hNomMap, hNomFile);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        super.loadInternals(nodeInternDir, exec);
        File hNumFile = new File(nodeInternDir, "numericHistograms.xml.gz");
        File hNomFile = new File(nodeInternDir, "nominalHistograms.xml.gz");
        DataExplorerNodeRepresentation rep = getViewRepresentation();
        if (hNumFile.exists()) {
            Double[] meansCheck = rep.getMeans();
            double[] means = new double[meansCheck.length];
            if (Arrays.asList(meansCheck).contains(null)) {
                throw new IOException("Means could not be retrieved from representation: null values were saved as mean values.");
            } else {
                for (int i = 0; i < meansCheck.length; i++) {
                    means[i]= meansCheck[i];
                }
            }
//            try {
//                Map<Integer, ? extends HistogramModel<?>> numHistograms = HistogramColumn.loadHistograms(hNumFile,
//                    new HashMap<Integer, Map<Integer, Set<RowKey>>>(), BinNumberSelectionStrategy.DecimalRange, means);
//                List<HistogramModel<?>> hList = new ArrayList<HistogramModel<?>>();
//                hList.addAll(numHistograms.values());
//                //rep.setJavaNumericHistograms(hList);
//
//                List<JSHistogram<NumericBin>> jsNumHist = new ArrayList<>();
//                for (int i = 0; i < hList.size(); i++) {
//                    jsNumHist.add(JSHistogram.createNumericHistogram(hList.get(i)));
//                }
//                rep.setJsNumericHistograms(jsNumHist);
//
//            } catch (InvalidSettingsException e) {
//                throw new IOException(e);
//            }
//        }
//        if (hNomFile.exists()) {
//            try {
//                Map<Integer, ? extends HistogramModel<?>> nomHistograms = HistogramColumn.loadNominalHistograms(hNomFile, rep.getNominalValuesSize());
//
//                List<JSHistogram<NominalBin>> jsNomHist = new ArrayList<>();
//                for (HistogramModel<?> hist : nomHistograms.values()) {
//                    jsNomHist.add(JSHistogram.createNominalHistogram(hist));
//                }
//                rep.setJsNominalHistograms(jsNomHist);
//
//            } catch (InvalidSettingsException e) {
//                throw new IOException(e);
//            }
        }

    }

}
