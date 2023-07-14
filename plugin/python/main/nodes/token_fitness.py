import pm4py
import knime.extension as knext
from utils import knime_util
import pandas as pd
import pytz
import logging


LOGGER = logging.getLogger(__name__)


@knext.node(name="Token-Based Fitness Checker",
            node_type=knext.NodeType.LEARNER,
            icon_path="category-conformance.png",
            category="/community/processmining/conformance/table")
@knime_util.create_node_description(
    short_description="Compute token-Based fitness statistics.",
    description="This compute token-Based fitness statistics.... ADD MORE EXPLAINATIONS"
)
@knext.input_table(name="Event Table", description="Input Event Table")
@knext.input_table(name="Petri Net Table", description="Petri Net Table")
@knext.output_table(name="Event Table", description="Fitness Statistics")
class TemplateNode:
    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_activity = knext.ColumnParameter(label="Activity Column",
                                              description="The column that contains the activities.",
                                              port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps."
                                                          " This column must have the type 'Local Date Time'.",
                                              port_index=0,
                                              column_filter=knime_util.is_local_date)

    
    def configure(self, configure_context: knext.ConfigurationContext, input_schema_1: knext.Schema, input_schema_2: knext.Schema):
        if not knime_util.is_petri_net(input_schema_2._select_columns(0)):
            raise IOError("The second table is not a Petri Net Table!")
        for par in [self.column_param_case, self.column_param_time, self.column_param_activity]:
            if par is None or par == "":
                raise ValueError("Parameters not set!")
        return None

    def execute(self, exec_context, input_1, input_2):
        event_log = input_1.to_pandas()
        net_table = input_2.to_pandas()
        # exec_context.set_warning("This is a warning")
        # exec_context.set_warning(net_table['Petri Net'].iloc[0].stringPN)
        pnCell = net_table['Petri Net'].iloc[0]

        event_log[self.column_param_time + "UTC"] = pd.to_datetime(event_log[self.column_param_time],
                                                                   format='%Y-%m-%d %H:%M:%S').dt.tz_localize(pytz.utc)
        event_log = event_log.sort_values(by=[self.column_param_case, self.column_param_time + "UTC"])

       

        fitness = pm4py.fitness_token_based_replay(log=event_log,
                                                   petri_net=pnCell.net,
                                                   initial_marking=pnCell.initial_marking,
                                                   final_marking=pnCell.final_marking,
                                                   activity_key=self.column_param_activity,
                                                   case_id_key=self.column_param_case,
                                                   timestamp_key=self.column_param_time + "UTC")
        res = df = pd.DataFrame.from_dict(fitness, orient='index', columns=['Value'])
        return knext.Table.from_pandas(res)
