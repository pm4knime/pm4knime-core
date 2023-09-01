import knime.api.types as kt
from pm4py.objects.petri_net.importer.variants.pnml import import_net_from_string


class PetriNetPythonCell:
    def __init__(self, stringPN):
        self.stringPN = stringPN
        self.net, self.initial_marking, self.final_marking = import_net_from_string(stringPN)

    # def to_dict(self):
    #     return {
    #         "stringPN": self.stringPN,
    #     }

    # def to_shapely(self):
    #     """Return the Shapely geometry, but ignore the crs"""
    #     from shapely import wkb
    #
    #     return wkb.loads(self.wkb)
    #
    # def __str__(self):
    #     from shapely import wkb
    #
    #     return f"GeoValue(wkb={wkb.loads(self.wkb)}, crs={self.crs})"


class PetriNetPythonFactory(kt.PythonValueFactory):
    def __init__(self):
        kt.PythonValueFactory.__init__(self, PetriNetPythonCell)

    def decode(self, storage):
        if storage is None:
            return None
        return PetriNetPythonCell(storage)

    def encode(self, value):
        if value is None:
            return None
        return value.stringPN


def _knime_value_factory(name):
    return '{"value_factory_class":"' + name + '"}'


_knime_value_factory = "org.pm4knime.node.conversion.pn2table.PetriNetCellFactory"


class FromPetriNetPandasColumnConverter(kt.FromPandasColumnConverter):
    # these warnings will be suppressed by the warning manager
    # warnings_to_suppress = ["Geometry column does not contain geometry."]

    def can_convert(self, dtype) -> bool:
        # return hasattr(dtype, "name") and dtype.name == "geometry"
        return True
    #
    def convert_column(
        self, data_frame: "pandas.dataframe", column_name: str
    ) -> str:
    
        # column = data_frame[column_name]
        # first_cell_value = str(column.iloc[0])
        # return PetriNetPythonCell(first_cell_value)
        return None


class ToPetriNetPandasColumnConverter(kt.ToPandasColumnConverter):
    def can_convert(self, dtype):
        import knime._arrow._types as kat
        exec_context.set_warning("TEST TE$St")

        return (
            isinstance(dtype, kat.LogicalTypeExtensionType)
            and dtype.logical_type == _knime_value_factory
        )

    def convert_column(
        self, data_frame: "pandas.dataframe", column_name: str
    ) -> str:

        column = data_frame[column_name]
        first_cell_value = column.iloc[0]

        return first_cell_value