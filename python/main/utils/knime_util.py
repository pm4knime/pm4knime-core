import logging
import knime.extension as knext

LOGGER = logging.getLogger(__name__)


def create_node_description(short_description: str, description: str, references: dict=None):
    """Generates a standardized node description."""

    def set_description(node_factory):
        s = f"{short_description}\n"
        s += f"{description}\n\n"
        # s += "___\n\n"  # separator line between description and general part
        if references is not None:
            s += "The node uses the following related information and function"
            if len(references) > 1:
                s += "s"
            s += ":"
            s += "\n\n"
            for key in references:
                s += f"- [{key}]({references[key]})\n"
        node_factory.__doc__ = s
        return node_factory

    return set_description


def is_numeric(column: knext.Column) -> bool:
    """
    Checks if column is numeric e.g. int, long or double.
    @return: True if Column is numeric
    """
    return (
            column.ktype == knext.double()
            or column.ktype == knext.int32()
            or column.ktype == knext.int64()
    )


def is_string(column: knext.Column) -> bool:
    """
    Checks if column is string
    @return: True if Column is string
    """
    return column.ktype == knext.string()


def is_boolean(column: knext.Column) -> bool:
    """
    Checks if column is boolean
    @return: True if Column is boolean
    """
    return column.ktype == knext.boolean()


def is_binary(column: knext.Column) -> bool:
    """
    Checks if column is binary
    @return: True if Column is binary
    """
    return column.ktype == knext.blob


def is_local_date(column: knext.Column) -> bool:
    return __is_type_x(column, "org.knime.core.data.v2.time.LocalDateTimeValueFactory")


def __is_type_x(column: knext.Column, type: str) -> bool:
    return (
        isinstance(column.ktype, knext.LogicalType)
        and type in column.ktype.logical_type
    )
