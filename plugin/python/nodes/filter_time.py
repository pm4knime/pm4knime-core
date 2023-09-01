import pm4py
import knime.extension as knext
from utils import knime_util
import pandas as pd
import pytz
import logging
import os


LOGGER = logging.getLogger(__name__)

script_dir = os.path.dirname(os.path.abspath(__file__))
path_to_icon = os.path.abspath(os.path.join(script_dir, "..", "..", "icon", "category-manipulation.png"))


class FilteringModes(knext.EnumParameterOptions):
    CONTAINED = ("Contained", "Keep the traces that are contained in the defined interval.")
    INTERSECTING = ("Intersecting", "Keep the traces that are intersecting with the defined time interval.")
    EVENTS = ("Events",
              "Keep the events that are contained in the defined time interval."
              " This option might lead to incomplete traces.")


@knext.node(name="Filter Event Table by Time",
            node_type=knext.NodeType.MANIPULATOR,
            icon_path=path_to_icon,
            category="/community/processmining/manipulation")
@knime_util.create_node_description(
    short_description="Filter an event table on a time interval.",
    description="This node is used to filter an event table on a time interval. Several filtering modes are supported."
)
@knext.input_table(name="Event Table", description="Input Event Table")
@knext.output_table(name="Event Table", description="Filtered Event Table")
class TimeFilter:
    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps."
                                                          " This column must have the type 'Local Date Time'.",
                                              port_index=0,
                                              column_filter=knime_util.is_local_date)

    logging_verbosity = knext.EnumParameter(
        label="Filtering Mode",
        description=None,
        default_value=FilteringModes.CONTAINED.name,
        enum=FilteringModes,
    )
    start_time_field = knext.StringParameter(label="Start Time (in the format YYYY-MM-DD HH:MM:SS)",
                                             description="Start Timestamp for the filtering time interval.")
    end_time_field = knext.StringParameter(label="End Time (in the format YYYY-MM-DD HH:MM:SS)",
                                           description="End Timestamp for the filtering time interval.")
    # param_select_activities.default_value = FilteringModes.CONTAINED.name,
    # param_select_activities.enum = FilteringModes
    start_time = None
    end_time = None

    def configure(self, configure_context: knext.ConfigurationContext, input_schema_1: knext.Schema):
        for par in [self.column_param_case, self.column_param_time, self.start_time_field, self.end_time_field]:
            if par is None or par == "":
                raise ValueError("Parameters not set!")

        try:
            self.start_time = pd.Timestamp(self.start_time_field).tz_localize(None)
        except Exception:
            raise ValueError("Invalid start timestamp!")
        try:
            self.end_time = pd.Timestamp(self.end_time_field).tz_localize(None)
        except Exception:
            raise ValueError("Invalid end timestamp!")

        return input_schema_1

    def execute(self, exec_context, input_1):
        event_log = input_1.to_pandas()

        # exec_context.set_warning("This is a warning")
        # LOGGER.warning(event_log.dtypes)
        event_log[self.column_param_time + "UTC"] = pd.to_datetime(event_log[self.column_param_time],
                                                                   format='%Y-%m-%d %H:%M:%S').dt.tz_localize(pytz.utc)
        # LOGGER.warning(event_log[self.column_param_time + "UTC"])
        event_log = event_log.sort_values(by=[self.column_param_case, self.column_param_time + "UTC"])

        mode = 'traces_contained'
        if self.logging_verbosity == FilteringModes.INTERSECTING.name:
            mode = 'traces_intersecting'
        elif self.logging_verbosity == FilteringModes.EVENTS.name:
            mode = 'events'
        elif self.logging_verbosity != FilteringModes.CONTAINED.name:
            raise ValueError("Unknown filtering mode: " + self.logging_verbosity)

        filtered_log = pm4py.filter_time_range(event_log,
                                               self.start_time_field,
                                               self.end_time_field,
                                               mode=mode,
                                               case_id_key=self.column_param_case,
                                               timestamp_key=self.column_param_time + "UTC")
        filtered_log.drop(axis=1, columns=self.column_param_time + "UTC", inplace=True)
        return knext.Table.from_pandas(filtered_log)
