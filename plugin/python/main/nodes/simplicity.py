import pm4py
import knime.extension as knext
from utils import knime_util
import pandas as pd
import logging


LOGGER = logging.getLogger(__name__)


@knext.node(name="Simplicity Evaluator",
            node_type=knext.NodeType.LEARNER,
            icon_path="category-conformance.png",
            category="/community/processmining/conformance/table")
@knime_util.create_node_description(
    short_description="Evaluate the simplicity of a Petri net.",
    description="This node evaluates the simplicity of the input Petri net. The criteria used for simplicity is the inverse arc degree (https://pm4py.fit.fraunhofer.de/documentation#item-8-4)."
)
@knext.input_table(name="Petri Net Table", description="A Petri Net Table.")
@knext.output_table(name="Metrics Table", description="A metrics table with a simplicity score. The computed score is a number between 0 and 1, where 0 stands for the lowest simplicity and 1 stands for the highest simplicity.")

class SimplicityChecker:

    def configure(self, configure_context: knext.ConfigurationContext, input_schema_2: knext.Schema):
        if not knime_util.is_petri_net(input_schema_2._select_columns(0)):
            raise IOError("The input table is not a Petri Net Table!")
        return None

    def execute(self, exec_context, input_2):
        net_table = input_2.to_pandas()
        # exec_context.set_warning("This is a warning")
        # exec_context.set_warning(net_table['Petri Net'].iloc[0].stringPN)
        pnCell = net_table.iloc[0,0]
        simplicity = pm4py.algo.evaluation.simplicity.algorithm.apply(petri_net=pnCell.net)
        res = pd.DataFrame.from_dict({"simplicity": simplicity}, orient='index', columns=['Value'])
        return knext.Table.from_pandas(res)
