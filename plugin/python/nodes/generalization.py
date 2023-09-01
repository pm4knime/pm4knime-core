import pm4py
from pm4py.util import constants
import knime.extension as knext
from utils import knime_util
import pandas as pd
import pytz
import logging
import os


LOGGER = logging.getLogger(__name__)

script_dir = os.path.dirname(os.path.abspath(__file__))
path_to_icon = os.path.abspath(os.path.join(script_dir, "..", "..", "icon", "category-conformance.png"))


@knext.node(name="Generalization Evaluator",
            node_type=knext.NodeType.OTHER,
            icon_path=path_to_icon,
            category="/community/processmining/conformance")
@knime_util.create_node_description(
    short_description="Evaluate the generalization of a Petri net with respect to an event log.",
    description="This node evaluates the generalization of the input Petri net with respect to the input event log. A model is considered to be general if the elements of the model are visited enough often during replaying the log on the model (https://pm4py.fit.fraunhofer.de/documentation#item-8-3)."
)
@knext.input_table(name="Event Table", description="An Event Table.")
@knext.input_table(name="Petri Net Table", description="A Petri Net Table.")
@knext.output_table(name="Metrics Table", description="A metrics table with a generalization score. The computed score is a number between 0 and 1, where 0 stands for the lowest generalization and 1 stands for the highest generalization.")

class GeneralizationChecker:
    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_activity = knext.ColumnParameter(label="Activity Column",
                                              description="The column that contains the activities.",
                                              port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps."
                                                          "This column must have the type 'Local Date Time'.",
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
        pnCell = net_table.iloc[0,0]

        event_log[self.column_param_time + "UTC"] = pd.to_datetime(event_log[self.column_param_time],
                                                                   format='%Y-%m-%d %H:%M:%S').dt.tz_localize(pytz.utc)
        event_log = event_log.sort_values(by=[self.column_param_case, self.column_param_time + "UTC"])

       

        reply_results = pm4py.algo.conformance.tokenreplay.algorithm.apply(log=event_log,
                                                                            net=pnCell.net,
                                                                            initial_marking=pnCell.initial_marking,
                                                                            final_marking=pnCell.final_marking,
                                                                            parameters={
                                                                            constants.PARAMETER_CONSTANT_ACTIVITY_KEY : self.column_param_activity,
                                                                            constants.PARAMETER_CONSTANT_ATTRIBUTE_KEY : self.column_param_activity,
                                                                            constants.PARAMETER_CONSTANT_CASEID_KEY : self.column_param_case,
                                                                            constants.PARAMETER_CONSTANT_TIMESTAMP_KEY : self.column_param_time + "UTC"})
        generalization = pm4py.algo.evaluation.generalization.variants.token_based.get_generalization(pnCell.net, reply_results)
        res = pd.DataFrame.from_dict({"generalization": generalization}, orient='index', columns=['Value'])
        return knext.Table.from_pandas(res)
