package org.pm4knime.node.jsvis;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 * @author Anastasia Zhukova, KNIME GmbH, Konstanz, Germany
 */
public class DataExplorerConfig {

    public static final String MIN = "Minimum";
    public static final String MAX = "Maximum";
    public static final String MEAN = "Mean";
    public static final String MEDIAN = "Median";
    public static final String STD_DEV = "Standard Deviation";
    public static final String VARIANCE = "Variance";
    public static final String SKEWNESS = "Skewness";
    public static final String KURTOSIS = "Kurtosis";
    public static final String SUM = "Overall Sum";
    public static final String MISSING = "No. missings";
    public static final String NAN = "No. NaN";
    public static final String P_INFINITY = "No. +∞";
    public static final String N_INFINITY = "No. -∞";
    public static final String ZEROS = "No. zeros";
    public static final String UNIQUE_NOMINAL = "Unique values";
    public static final String TOP_FREQ_VAL = "Top frequent values";
    public static final String TOP_INFREQ_VAL = "Top infrequent values";
    public static final String ALL_NOMINAL_VAL = "All nominal values";

    public static final String OTHER_VALUES_NOTATION = "otherValuesNotation";
    public static final String DEFAULT_OTHER_VALUES_NOTATION = "[...]";

    public static final String OTHER_ERROR_VALUES_NOTATION = "otherErrorValuesNotation";
    public static final String DEFAULT_OTHER_ERROR_VALUES_NOTATION = "[...]!";

    public static final String CFG_HIDE_IN_WIZARD = "hideInWizard";
    private static final boolean DEFAULT_HIDE_IN_WIZARD = false;
    private boolean m_hideInWizard = DEFAULT_HIDE_IN_WIZARD;

    public static final String CFG_SHOW_MEDIAN = "showMedian";
    private static final boolean DEFAULT_SHOW_MEDIAN = false;
    private boolean m_showMedian = DEFAULT_SHOW_MEDIAN;

    public final static String CFG_ENABLE_PAGING = "enablePaging";
    private final static boolean DEFAULT_ENABLE_PAGING = false;
    private boolean m_enablePaging = DEFAULT_ENABLE_PAGING;

    public final static String CFG_INITIAL_PAGE_SIZE = "initialPageSize";
    public final static int DEFAULT_INITIAL_PAGE_SIZE = 10;
    private int m_initialPageSize = DEFAULT_INITIAL_PAGE_SIZE;

    public final static String CFG_ENABLE_PAGE_SIZE_CHANGE = "enablePageSizeChange";
    private final static boolean DEFAULT_ENABLE_PAGE_SIZE_CHANGE = true;
    private boolean m_enablePageSizeChange = DEFAULT_ENABLE_PAGE_SIZE_CHANGE;

    public final static String CFG_PAGE_SIZES = "allowedPageSizes";
    private final static int[] DEFAULT_PAGE_SIZES = new int[]{10, 25, 50, 100};
    private int[] m_allowedPageSizes = DEFAULT_PAGE_SIZES;

    public final static String CFG_PAGE_SIZE_SHOW_ALL = "enableShowAll";
    private final static boolean DEFAULT_PAGE_SIZE_SHOW_ALL = false;
    private boolean m_pageSizeShowAll = DEFAULT_PAGE_SIZE_SHOW_ALL;

    public final static String CFG_ENABLE_JUMP_TO_PAGE = "enableJumpToPage";
    private final static boolean DEFAULT_ENABLE_JUMP_TO_PAGE = false;
    private boolean m_enableJumpToPage = DEFAULT_ENABLE_JUMP_TO_PAGE;

    public final static String CFG_DISPLAY_ROW_IDS = "displayRowIDs";
    private final static boolean DEFAULT_DISPLAY_ROW_IDS = true;
    private boolean m_displayRowIds = DEFAULT_DISPLAY_ROW_IDS;

    public final static String CFG_DISPLAY_COLUMN_HEADERS = "displayColumnHeaders";
    private final static boolean DEFAULT_DISPLAY_COLUMN_HEADERS = true;
    private boolean m_displayColumnHeaders = DEFAULT_DISPLAY_COLUMN_HEADERS;

    public final static String CFG_DISPLAY_FULLSCREEN_BUTTON = "displayFullscreenButton";
    public final static boolean DEFAULT_DISPLAY_FULLSCREEN_BUTTON = true;
    private boolean m_displayFullscreenButton = DEFAULT_DISPLAY_FULLSCREEN_BUTTON;

    public final static String CFG_FIXED_HEADERS = "fixedHeaders";
    private final static boolean DEFAULT_FIXED_HEADERS = false;
    private boolean m_fixedHeaders = DEFAULT_FIXED_HEADERS;

    public final static String CFG_TITLE = "title";
    private final static String DEFAULT_TITLE = "";
    private String m_title = DEFAULT_TITLE;

    public final static String CFG_SUBTITLE = "subtitle";
    private final static String DEFAULT_SUBTITLE = "";
    private String m_subtitle = DEFAULT_SUBTITLE;

    public final static String CFG_ENABLE_SELECTION = "enableSelection";
    public final static boolean DEFAULT_ENABLE_SELECTION = true;
    private boolean m_enableSelection = DEFAULT_ENABLE_SELECTION;

    public final static String CFG_ENABLE_SEARCHING = "enableSearching";
    private final static boolean DEFAULT_ENABLE_SEARCHING = true;
    private boolean m_enableSearching = DEFAULT_ENABLE_SEARCHING;

    public final static String CFG_ENABLE_SORTING = "enableSorting";
    private final static boolean DEFAULT_ENABLE_SORTING = true;
    private boolean m_enableSorting = DEFAULT_ENABLE_SORTING;

    public final static String CFG_ENABLE_CLEAR_SORT_BUTTON = "enableClearSortButton";
    private final static boolean DEFAULT_ENABLE_CLEAR_SORT_BUTTON = false;
    private boolean m_enableClearSortButton = DEFAULT_ENABLE_CLEAR_SORT_BUTTON;

    public final static String CFG_ENABLE_GLOBAL_NUMBER_FORMAT = "enableGlobalNumberFormat";
    private final static boolean DEFAULT_ENABLE_GLOBAL_NUMBER_FORMAT = true;
    private boolean m_enableGlobalNumberFormat = DEFAULT_ENABLE_GLOBAL_NUMBER_FORMAT;

    public final static String CFG_GLOBAL_NUMBER_FORMAT_DECIMALS = "globalNumberFormatDecimals";
    private final static int DEFAULT_GLOBAL_NUMBER_FORMAT_DECIMALS = 3;
    private int m_globalNumberFormatDecimals = DEFAULT_GLOBAL_NUMBER_FORMAT_DECIMALS;

    public final static String CFG_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK = "displayMissingValueAsQuestionMark";
    public final static boolean DEFAULT_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK = true;
    private boolean m_displayMissingValueAsQuestionMark = DEFAULT_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK;

    public final static String CFG_DISPLAY_ROW_NUMBER = "displayRowNumber";
    public final static int DEFAULT_DISPLAY_ROW_NUMBER = 100;
    private int m_displayRowNumber = DEFAULT_DISPLAY_ROW_NUMBER;

    public final static String CFG_MAX_NOMINAL_VALUES = "maxNominalValues";
    public final static int DEFAULT_MAX_NOMINAL_VALUES = 1000;
    private int m_maxNominalValues = DEFAULT_MAX_NOMINAL_VALUES;

    final static String CFG_ENABLE_FREQ_VAL_DISPLAY = "enableFreqValDisplay";
    final static boolean DEFAULT_ENABLE_FREQ_VAL_DISPLAY = true;
    private boolean m_enableFreqValDisplay = DEFAULT_ENABLE_FREQ_VAL_DISPLAY;

    final static String CFG_FREQ_VALUES_NUMBER = "freqValuesNumber";
    final static int DEFAULT_FREQ_VALUES_NUMBER = 5;
    private int m_freqValuesNumber = DEFAULT_FREQ_VALUES_NUMBER;

    final static String CFG_MISSING_VALUES_IN_HIST = "missingValuesInHist";
    final static boolean DEFAULT_MISSING_VALUES_IN_HIST = false;
    private boolean m_missingValuesInHist = DEFAULT_MISSING_VALUES_IN_HIST;

    final static String CFG_NUMBER_OF_HISTOGRAM_BARS = "numberOfHistogramBars";
    final static int DEFAULT_NUMBER_OF_HISTOGRAM_BARS = 10;
    private int m_numberOfHistogramBars = DEFAULT_NUMBER_OF_HISTOGRAM_BARS;

    final static String CFG_ADAPT_NUMBER_OF_HISTOGRAM_BARS = "adaptNumberOfHistogramBars";
    final static boolean DEFAULT_ADAPT_NUMBER_OF_HISTOGRAM_BARS = false;
    private boolean m_adaptNumberOfHistogramBars = DEFAULT_ADAPT_NUMBER_OF_HISTOGRAM_BARS;

    final static String CFG_MAX_NOMINAL_VALUE_REACHED = "maxNomValueReached";
    final static String[] DEFAULT_MAX_NOMINAL_VALUE_REACHED = new String[0];
    private String[] m_maxNomValueReached = DEFAULT_MAX_NOMINAL_VALUE_REACHED;

    final static String CFG_CUSTOM_CSS = "customCSS";
    private final static String DEFAULT_CUSTOM_CSS = "";
    private String m_customCSS = DEFAULT_CUSTOM_CSS;


    final static String CFG_SELECTION = "selection";
    /**
     * @return the hideInWizard
     */
    public boolean getHideInWizard() {
        return m_hideInWizard;
    }

    /**
     * @param hideInWizard the hideInWizard to set
     */
    public void setHideInWizard(final boolean hideInWizard) {
        m_hideInWizard = hideInWizard;
    }

    /**
     * @return the showMedian
     */
    public boolean getShowMedian() {
        return m_showMedian;
    }

    /**
     * @param showMedian the showMedian to set
     */
    public void setShowMedian(final boolean showMedian) {
        m_showMedian = showMedian;
    }

    /**
     * @return the enablePaging
     */
    public boolean getEnablePaging() {
        return m_enablePaging;
    }

    /**
     * @param enablePaging the enablePaging to set
     */
    public void setEnablePaging(final boolean enablePaging) {
        m_enablePaging = enablePaging;
    }

    /**
     * @return the initialPageSize
     */
    public int getInitialPageSize() {
        return m_initialPageSize;
    }

    /**
     * @param initialPageSize the initialPageSize to set
     */
    public void setInitialPageSize(final int initialPageSize) {
        m_initialPageSize = initialPageSize;
    }

    /**
     * @return the enablePageSizeChange
     */
    public boolean getEnablePageSizeChange() {
        return m_enablePageSizeChange;
    }

    /**
     * @param enablePageSizeChange the enablePageSizeChange to set
     */
    public void setEnablePageSizeChange(final boolean enablePageSizeChange) {
        m_enablePageSizeChange = enablePageSizeChange;
    }

    /**
     * @return the allowedPageSizes
     */
    public int[] getAllowedPageSizes() {
        return m_allowedPageSizes;
    }

    /**
     * @param allowedPageSizes the allowedPageSizes to set
     */
    public void setAllowedPageSizes(final int[] allowedPageSizes) {
        m_allowedPageSizes = allowedPageSizes;
    }

    /**
     * @return the pageSizeShowAll
     */
    public boolean getPageSizeShowAll() {
        return m_pageSizeShowAll;
    }

    /**
     * @param pageSizeShowAll the pageSizeShowAll to set
     */
    public void setPageSizeShowAll(final boolean pageSizeShowAll) {
        m_pageSizeShowAll = pageSizeShowAll;
    }

    /**
     * @return the enableJumpToPage
     */
    public boolean getEnableJumpToPage() {
        return m_enableJumpToPage;
    }

    /**
     * @param enableJumpToPage the enableJumpToPage to set
     */
    public void setEnableJumpToPage(final boolean enableJumpToPage) {
        m_enableJumpToPage = enableJumpToPage;
    }

    /**
     * @return the displayRowIds
     */
    public boolean getDisplayRowIds() {
        return m_displayRowIds;
    }

    /**
     * @param displayRowIds the displayRowIds to set
     */
    public void setDisplayRowIds(final boolean displayRowIds) {
        m_displayRowIds = displayRowIds;
    }

    /**
     * @return the displayColumnHeaders
     */
    public boolean getDisplayColumnHeaders() {
        return m_displayColumnHeaders;
    }

    /**
     * @param displayColumnHeaders the displayColumnHeaders to set
     */
    public void setDisplayColumnHeaders(final boolean displayColumnHeaders) {
        m_displayColumnHeaders = displayColumnHeaders;
    }

    /**
     * @return the fixedHeaders
     */
    public boolean getFixedHeaders() {
        return m_fixedHeaders;
    }

    /**
     * @param fixedHeaders the fixedHeaders to set
     */
    public void setFixedHeaders(final boolean fixedHeaders) {
        m_fixedHeaders = fixedHeaders;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return m_title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        m_title = title;
    }

    /**
     * @return the subtitle
     */
    public String getSubtitle() {
        return m_subtitle;
    }

    /**
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(final String subtitle) {
        m_subtitle = subtitle;
    }

    /**
     * @return the enableSelection
     */
    public boolean getEnableSelection() {
        return m_enableSelection;
    }

    /**
     * @param enableSelection the enableSelection to set
     */
    public void setEnableSelection(final boolean enableSelection) {
        m_enableSelection = enableSelection;
    }

    /**
     * @return the enableSearching
     */
    public boolean getEnableSearching() {
        return m_enableSearching;
    }

    /**
     * @param enableSearching the enableSearching to set
     */
    public void setEnableSearching(final boolean enableSearching) {
        m_enableSearching = enableSearching;
    }

    /**
     * @return the enableSorting
     */
    public boolean getEnableSorting() {
        return m_enableSorting;
    }

    /**
     * @param enableSorting the enableSorting to set
     */
    public void setEnableSorting(final boolean enableSorting) {
        m_enableSorting = enableSorting;
    }

    /**
     * @return the enableClearSortButton
     */
    public boolean getEnableClearSortButton() {
        return m_enableClearSortButton;
    }

    /**
     * @param enableClearSortButton the enableClearSortButton to set
     */
    public void setEnableClearSortButton(final boolean enableClearSortButton) {
        m_enableClearSortButton = enableClearSortButton;
    }

    /**
     * @return the enableGlobalNumberFormat
     */
    public boolean getEnableGlobalNumberFormat() {
        return m_enableGlobalNumberFormat;
    }

    /**
     * @param enableGlobalNumberFormat the enableGlobalNumberFormat to set
     */
    public void setEnableGlobalNumberFormat(final boolean enableGlobalNumberFormat) {
        m_enableGlobalNumberFormat = enableGlobalNumberFormat;
    }

    /**
     * @return the globalNumberFormatDecimals
     */
    public int getGlobalNumberFormatDecimals() {
        return m_globalNumberFormatDecimals;
    }

    /**
     * @param globalNumberFormatDecimals the globalNumberFormatDecimals to set
     */
    public void setGlobalNumberFormatDecimals(final int globalNumberFormatDecimals) {
        m_globalNumberFormatDecimals = globalNumberFormatDecimals;
    }

    /**
     * @return the displayFullscreenButton
     */
    public boolean getDisplayFullscreenButton() {
        return m_displayFullscreenButton;
    }

    /**
     * @param displayFullscreenButton the displayFullscreenButton to set
     */
    public void setDisplayFullscreenButton(final boolean displayFullscreenButton) {
        m_displayFullscreenButton = displayFullscreenButton;
    }

    /**
     * @return the displayMissingValueAsQuestionMark
     */
    public boolean getDisplayMissingValueAsQuestionMark() {
        return m_displayMissingValueAsQuestionMark;
    }

    /**
     * @param displayMissingValueAsQuestionMark the displayMissingValueAsQuestionMark to set
     */
    public void setDisplayMissingValueAsQuestionMark(final boolean displayMissingValueAsQuestionMark) {
        m_displayMissingValueAsQuestionMark = displayMissingValueAsQuestionMark;
    }

    /**
     * @return the m_displayRowNumber
     */
    public int getDisplayRowNumber() {
        return m_displayRowNumber;
    }

    /**
     * @param displayRowNumber the m_displayRowNumber to set
     */
    public void setDisplayRowNumber(final int displayRowNumber) {
        this.m_displayRowNumber = displayRowNumber;
    }

    /**
     * @return the m_maxNominalValues
     */
    public int getMaxNominalValues() {
        return m_maxNominalValues;
    }

    /**
     * @param maxNominalValues the m_maxNominalValues to set
     */
    public void setMaxNominalValues(final int maxNominalValues) {
        this.m_maxNominalValues = maxNominalValues;
    }

    /**
     * @return the m_enableFreqValDisplay
     */
    public boolean getEnableFreqValDisplay() {
        return m_enableFreqValDisplay;
    }

    /**
     * @param enableFreqValDisplay the m_enableFreqValDisplay to set
     */
    public void setEnableFreqValDisplay(final boolean enableFreqValDisplay) {
        this.m_enableFreqValDisplay = enableFreqValDisplay;
    }

    /**
     * @return the m_freqValues
     */
    public int getFreqValuesNumber() {
        return m_freqValuesNumber;
    }

    /**
     * @param freqValuesNumber the m_freqValues to set
     */
    public void setFreqValuesNumber(final int freqValuesNumber) {
        this.m_freqValuesNumber = freqValuesNumber;
    }

    /**
     * @return the m_missingValuesInHist
     */
    public boolean getMissingValuesInHist() {
        return m_missingValuesInHist;
    }

    /**
     * @param missingValuesInHist the m_missingValuesInHist to set
     */
    public void setMissingValuesInHist(final boolean missingValuesInHist) {
        this.m_missingValuesInHist = missingValuesInHist;
    }

    /**
     * @return the m_numberOfHistogramBars
     */
    public int getNumberOfHistogramBars() {
        return m_numberOfHistogramBars;
    }

    /**
     * @param numberOfHistogramBars the m_numberOfHistogramBars to set
     */
    public void setNumberOfHistogramBars(final int numberOfHistogramBars) {
        this.m_numberOfHistogramBars = numberOfHistogramBars;
    }

    /**
     * @return the m_adaptNumberOfHistogramBars
     */
    public boolean getAdaptNumberOfHistogramBars() {
        return m_adaptNumberOfHistogramBars;
    }

    /**
     * @param adaptNumberOfHistogramBars the m_adaptNumberOfHistogramBars to set
     */
    public void setAdaptNumberOfHistogramBars(final boolean adaptNumberOfHistogramBars) {
        this.m_adaptNumberOfHistogramBars = adaptNumberOfHistogramBars;
    }

    /**
     * @return the m_maxNomValueReached
     */
    public String[] getMaxNomValueReached() {
        return m_maxNomValueReached;
    }

    /**
     * @param maxNomValueReached the m_maxNomValueReached to set
     */
    public void setMaxNomValueReached(final String[] maxNomValueReached) {
        this.m_maxNomValueReached = maxNomValueReached;
    }

    /**
     * @return the customCSS
     */
    public String getCustomCSS() {
        return m_customCSS;
    }

    /**
     * @param customCSS the customCSS to set
     */
    public void setCustomCSS(final String customCSS) {
        m_customCSS = customCSS;
    }


    public void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addBoolean(CFG_HIDE_IN_WIZARD, m_hideInWizard);
        settings.addBoolean(CFG_SHOW_MEDIAN, m_showMedian);
        settings.addBoolean(CFG_ENABLE_PAGING, m_enablePaging);
        settings.addInt(CFG_INITIAL_PAGE_SIZE, m_initialPageSize);
        settings.addBoolean(CFG_ENABLE_PAGE_SIZE_CHANGE, m_enablePageSizeChange);
        settings.addIntArray(CFG_PAGE_SIZES, m_allowedPageSizes);
        settings.addBoolean(CFG_PAGE_SIZE_SHOW_ALL, m_pageSizeShowAll);
        settings.addBoolean(CFG_ENABLE_JUMP_TO_PAGE, m_enableJumpToPage);
        settings.addBoolean(CFG_DISPLAY_ROW_IDS, m_displayRowIds);
        settings.addBoolean(CFG_DISPLAY_COLUMN_HEADERS, m_displayColumnHeaders);
        settings.addBoolean(CFG_FIXED_HEADERS, m_fixedHeaders);
        settings.addString(CFG_TITLE, m_title);
        settings.addString(CFG_SUBTITLE, m_subtitle);
        settings.addBoolean(CFG_ENABLE_SELECTION, m_enableSelection);
        settings.addBoolean(CFG_ENABLE_SEARCHING, m_enableSearching);
        settings.addBoolean(CFG_ENABLE_SORTING, m_enableSorting);
        settings.addBoolean(CFG_ENABLE_CLEAR_SORT_BUTTON, m_enableClearSortButton);
        settings.addBoolean(CFG_ENABLE_GLOBAL_NUMBER_FORMAT, m_enableGlobalNumberFormat);
        settings.addInt(CFG_GLOBAL_NUMBER_FORMAT_DECIMALS, m_globalNumberFormatDecimals);
        settings.addBoolean(CFG_DISPLAY_FULLSCREEN_BUTTON, m_displayFullscreenButton);
        settings.addBoolean(CFG_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK, m_displayMissingValueAsQuestionMark);
        settings.addInt(CFG_DISPLAY_ROW_NUMBER, m_displayRowNumber);
        settings.addInt(CFG_MAX_NOMINAL_VALUES, m_maxNominalValues);
        settings.addBoolean(CFG_ENABLE_FREQ_VAL_DISPLAY, m_enableFreqValDisplay);
        settings.addInt(CFG_FREQ_VALUES_NUMBER, m_freqValuesNumber);
        settings.addBoolean(CFG_MISSING_VALUES_IN_HIST, m_missingValuesInHist);
        settings.addInt(CFG_NUMBER_OF_HISTOGRAM_BARS, m_numberOfHistogramBars);
        settings.addBoolean(CFG_ADAPT_NUMBER_OF_HISTOGRAM_BARS, m_adaptNumberOfHistogramBars);
        settings.addStringArray(CFG_MAX_NOMINAL_VALUE_REACHED, m_maxNomValueReached);

        //added with 3.6
        settings.addString(CFG_CUSTOM_CSS, m_customCSS);
    }

    public void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_hideInWizard = settings.getBoolean(CFG_HIDE_IN_WIZARD);
        m_showMedian = settings.getBoolean(CFG_SHOW_MEDIAN);
        m_enablePaging = settings.getBoolean(CFG_ENABLE_PAGING);
        m_initialPageSize = settings.getInt(CFG_INITIAL_PAGE_SIZE, DEFAULT_INITIAL_PAGE_SIZE);
        m_enablePageSizeChange = settings.getBoolean(CFG_ENABLE_PAGE_SIZE_CHANGE);
        m_allowedPageSizes = settings.getIntArray(CFG_PAGE_SIZES);
        m_pageSizeShowAll = settings.getBoolean(CFG_PAGE_SIZE_SHOW_ALL);
        m_enableJumpToPage = settings.getBoolean(CFG_ENABLE_JUMP_TO_PAGE);
        m_displayRowIds = settings.getBoolean(CFG_DISPLAY_ROW_IDS);
        m_displayColumnHeaders = settings.getBoolean(CFG_DISPLAY_COLUMN_HEADERS);
        m_fixedHeaders = settings.getBoolean(CFG_FIXED_HEADERS);
        m_title = settings.getString(CFG_TITLE);
        m_subtitle = settings.getString(CFG_SUBTITLE);
        m_enableSelection = settings.getBoolean(CFG_ENABLE_SELECTION);
        m_enableSearching = settings.getBoolean(CFG_ENABLE_SEARCHING);
        m_enableSorting = settings.getBoolean(CFG_ENABLE_SORTING);
        m_enableClearSortButton = settings.getBoolean(CFG_ENABLE_CLEAR_SORT_BUTTON);
        m_enableGlobalNumberFormat = settings.getBoolean(CFG_ENABLE_GLOBAL_NUMBER_FORMAT);
        m_globalNumberFormatDecimals = settings.getInt(CFG_GLOBAL_NUMBER_FORMAT_DECIMALS);
        m_displayFullscreenButton = settings.getBoolean(CFG_DISPLAY_FULLSCREEN_BUTTON, DEFAULT_DISPLAY_FULLSCREEN_BUTTON);
        m_displayMissingValueAsQuestionMark = settings.getBoolean(CFG_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK, DEFAULT_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK);
        m_displayRowNumber = settings.getInt(CFG_DISPLAY_ROW_NUMBER, DEFAULT_DISPLAY_ROW_NUMBER);
        m_maxNominalValues = settings.getInt(CFG_MAX_NOMINAL_VALUES, DEFAULT_MAX_NOMINAL_VALUES);
        m_enableFreqValDisplay = settings.getBoolean(CFG_ENABLE_FREQ_VAL_DISPLAY, DEFAULT_ENABLE_FREQ_VAL_DISPLAY);
        m_freqValuesNumber = settings.getInt(CFG_FREQ_VALUES_NUMBER, DEFAULT_FREQ_VALUES_NUMBER);
        m_missingValuesInHist = settings.getBoolean(CFG_MISSING_VALUES_IN_HIST, DEFAULT_MISSING_VALUES_IN_HIST);
        m_numberOfHistogramBars = settings.getInt(CFG_NUMBER_OF_HISTOGRAM_BARS, DEFAULT_NUMBER_OF_HISTOGRAM_BARS);
        m_adaptNumberOfHistogramBars = settings.getBoolean(CFG_ADAPT_NUMBER_OF_HISTOGRAM_BARS, DEFAULT_ADAPT_NUMBER_OF_HISTOGRAM_BARS);
        m_maxNomValueReached = settings.getStringArray(CFG_MAX_NOMINAL_VALUE_REACHED, DEFAULT_MAX_NOMINAL_VALUE_REACHED);

        //added with 3.6
        m_customCSS = settings.getString(CFG_CUSTOM_CSS, DEFAULT_CUSTOM_CSS);
    }

    void loadSettingsForDialog(final NodeSettingsRO settings, final DataTableSpec spec) {
        m_hideInWizard = settings.getBoolean(CFG_HIDE_IN_WIZARD, DEFAULT_HIDE_IN_WIZARD);
        m_showMedian = settings.getBoolean(CFG_SHOW_MEDIAN, DEFAULT_SHOW_MEDIAN);
        m_enablePaging = settings.getBoolean(CFG_ENABLE_PAGING, DEFAULT_ENABLE_PAGING);
        m_initialPageSize = settings.getInt(CFG_INITIAL_PAGE_SIZE, DEFAULT_INITIAL_PAGE_SIZE);
        m_enablePageSizeChange = settings.getBoolean(CFG_ENABLE_PAGE_SIZE_CHANGE, DEFAULT_ENABLE_PAGE_SIZE_CHANGE);
        m_allowedPageSizes = settings.getIntArray(CFG_PAGE_SIZES, DEFAULT_PAGE_SIZES);
        m_pageSizeShowAll = settings.getBoolean(CFG_PAGE_SIZE_SHOW_ALL, DEFAULT_PAGE_SIZE_SHOW_ALL);
        m_enableJumpToPage = settings.getBoolean(CFG_ENABLE_JUMP_TO_PAGE, DEFAULT_ENABLE_JUMP_TO_PAGE);
        m_displayRowIds = settings.getBoolean(CFG_DISPLAY_ROW_IDS, DEFAULT_DISPLAY_ROW_IDS);
        m_displayColumnHeaders = settings.getBoolean(CFG_DISPLAY_COLUMN_HEADERS, DEFAULT_DISPLAY_COLUMN_HEADERS);
        m_fixedHeaders = settings.getBoolean(CFG_FIXED_HEADERS, DEFAULT_FIXED_HEADERS);
        m_title = settings.getString(CFG_TITLE, DEFAULT_TITLE);
        m_subtitle = settings.getString(CFG_SUBTITLE, DEFAULT_SUBTITLE);
        m_enableSelection = settings.getBoolean(CFG_ENABLE_SELECTION, DEFAULT_ENABLE_SELECTION);
        m_enableSearching = settings.getBoolean(CFG_ENABLE_SEARCHING, DEFAULT_ENABLE_SEARCHING);
        m_enableSorting = settings.getBoolean(CFG_ENABLE_SORTING, DEFAULT_ENABLE_SORTING);
        m_enableClearSortButton = settings.getBoolean(CFG_ENABLE_CLEAR_SORT_BUTTON, DEFAULT_ENABLE_CLEAR_SORT_BUTTON);
        m_enableGlobalNumberFormat = settings.getBoolean(CFG_ENABLE_GLOBAL_NUMBER_FORMAT, DEFAULT_ENABLE_GLOBAL_NUMBER_FORMAT);
        m_globalNumberFormatDecimals = settings.getInt(CFG_GLOBAL_NUMBER_FORMAT_DECIMALS, DEFAULT_GLOBAL_NUMBER_FORMAT_DECIMALS);
        m_displayFullscreenButton = settings.getBoolean(CFG_DISPLAY_FULLSCREEN_BUTTON, DEFAULT_DISPLAY_FULLSCREEN_BUTTON);
        m_displayMissingValueAsQuestionMark = settings.getBoolean(CFG_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK, DEFAULT_DISPLAY_MISSING_VALUE_AS_QUESTION_MARK);
        m_displayRowNumber = settings.getInt(CFG_DISPLAY_ROW_NUMBER, DEFAULT_DISPLAY_ROW_NUMBER);
        m_maxNominalValues = settings.getInt(CFG_MAX_NOMINAL_VALUES, DEFAULT_MAX_NOMINAL_VALUES);
        m_enableFreqValDisplay = settings.getBoolean(CFG_ENABLE_FREQ_VAL_DISPLAY, DEFAULT_ENABLE_FREQ_VAL_DISPLAY);
        m_freqValuesNumber = settings.getInt(CFG_FREQ_VALUES_NUMBER, DEFAULT_FREQ_VALUES_NUMBER);
        m_missingValuesInHist = settings.getBoolean(CFG_MISSING_VALUES_IN_HIST, DEFAULT_MISSING_VALUES_IN_HIST);
        m_numberOfHistogramBars = settings.getInt(CFG_NUMBER_OF_HISTOGRAM_BARS, DEFAULT_NUMBER_OF_HISTOGRAM_BARS);
        m_adaptNumberOfHistogramBars = settings.getBoolean(CFG_ADAPT_NUMBER_OF_HISTOGRAM_BARS, DEFAULT_ADAPT_NUMBER_OF_HISTOGRAM_BARS);
        m_maxNomValueReached = settings.getStringArray(CFG_MAX_NOMINAL_VALUE_REACHED, DEFAULT_MAX_NOMINAL_VALUE_REACHED);

        //added with 3.6
        m_customCSS = settings.getString(CFG_CUSTOM_CSS, DEFAULT_CUSTOM_CSS);
    }



}
