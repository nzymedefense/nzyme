use crate::outputs::output_data::OutputDataType;

pub struct OutputConfiguration {
    pub data_type: OutputDataType,
    pub name: String,
    pub filters: Vec<FilterConfiguration>
}

pub struct FilterConfiguration {
    pub field: String,
    pub condition: FilterCondition,
    pub value: String,
    pub modifier: Option<FilterModifier>
}

pub enum FilterCondition {
    ExactStringMatch
}

pub enum FilterModifier {
    Invert
}