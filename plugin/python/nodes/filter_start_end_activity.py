import pm4py
import knime.extension as knext
import pandas as pd
import pytz
import logging
from pathlib import Path
import os


LOGGER = logging.getLogger(__name__)

script_dir = os.path.dirname(os.path.abspath(__file__))
path_to_icon = os.path.abspath(os.path.join(script_dir, "..", "..", "icon", "category-manipulation.png"))

@knext.node(name="Second Node",
            node_type=knext.NodeType.MANIPULATOR,
            icon_path= path_to_icon,
            category="/community/processmining/manipulation")
@knext.input_table(name="Input Data", description="We read data from here")
@knext.output_table(name="Output Data", description="Whatever the node has produced")
class OtherNode:
    # def is_numeric(column):  # Filter columns visible in the column_param for numeric ones
    #     return (
    #             column.ktype == knext.double()
    #             or column.ktype == knext.int32()
    #             or column.ktype == knext.int64()
    #     )

    column_param_case = knext.ColumnParameter(label="Case Column",
                                              description="The column that contains the case identifiers.",
                                              port_index=0)
    column_param_time = knext.ColumnParameter(label="Time Column",
                                              description="The column that contains the timestamps.",
                                              port_index=0)

    def configure(self, configure_context, input_schema_1):
        return input_schema_1

    def execute(self, exec_context, input_1):
        event_log = input_1.to_pandas()

        return knext.Table.from_pandas(event_log)
