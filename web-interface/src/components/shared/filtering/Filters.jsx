import React, {useEffect, useState} from 'react';
import AppliedFilterList from "./AppliedFilterList";
import FilterValueInput from "./FilterValueInput";
import validateStringNotEmpty from "./validators/StringNotEmptyValidator";
import validateNumberNotNegative from "./validators/NumberNotNegativeValidator";

export const FILTER_TYPE = {
  STRING: {
    name: "string",
    validators: [validateStringNotEmpty]
  },
  NUMERIC: {
    name: "numeric",
    validators: [validateNumberNotNegative]
  },
  IP_ADDRESS: {
    name: "ip_address"
  },
  PORT_NUMBER: {
    name: "port_number"
  },
  DNS_TYPE: {
    name: "dns_type"
  }
}

export const FIELD_TYPE = {
  ANY_TEXT: "any_text",
  REGEX_TEXT: "regex_text",
  NUMBER: "any_number",
  CIDR: "cidr",
  NO_VALUE: "none"
}

export const OPERATORS = {
  EQUALS:             { name: "equals", sign: "==", placeholder: "", no_value: false, field_type: FIELD_TYPE.ANY_TEXT },
  NOT_EQUALS:         { name: "not_equals", sign: "!=", placeholder: "", no_value: false, field_type: FIELD_TYPE.ANY_TEXT },
  EQUALS_NUMERIC:     { name: "equals_numeric", sign: "==", placeholder: "", no_value: false, field_type: FIELD_TYPE.NUMBER },
  NOT_EQUALS_NUMERIC: { name: "not_equals_numeric", sign: "!=", placeholder: "", no_value: false, field_type: FIELD_TYPE.NUMBER },
  REGEX_MATCH:        { name: "regex_match", sign: "~=", placeholder: "", no_value: false, field_type: FIELD_TYPE.REGEX_TEXT },
  NOT_REGEX_MATCH:    { name: "not_regex_match", sign: "!~=", placeholder: "", no_value: false, field_type: FIELD_TYPE.REGEX_TEXT },
  GREATER_THAN:       { name: "greater_than", sign: ">", placeholder: "", no_value: false, field_type: FIELD_TYPE.NUMBER },
  SMALLER_THAN:       { name: "smaller_than", sign: "<", placeholder: "", no_value: false, field_type: FIELD_TYPE.NUMBER },
  IN_CIDR:            { name: "in_cidr", sign: "IN CIDR:", placeholder: "172.16.0.0/24", no_value: false, field_type: FIELD_TYPE.CIDR },
  NOT_IN_CIDR:        { name: "not_in_cidr", sign: "NOT IN CIDR:", placeholder: "172.16.0.0/24", no_value: false, field_type: FIELD_TYPE.CIDR },
  IS_PRIVATE:         { name: "is_private", sign: "IS PRIVATE", placeholder: "", no_value: true, field_type: FIELD_TYPE.NO_VALUE },
  IS_NOT_PRIVATE:     { name: "is_not_private", sign: "IS NOT PRIVATE", placeholder: "", no_value: true, field_type: FIELD_TYPE.NO_VALUE }
}

export default function Filters(props) {

  const fields = props.fields;
  const filters = props.filters;
  const setFilters = props.setFilters;

  const defaultOperator = OPERATORS.EQUALS;

  const [selectionMade, setSelectionMade] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState({ name: "", field: "0", type: FILTER_TYPE.STRING });
  const [allowedOperators, setAllowedOperators] = useState([defaultOperator]);
  const [selectedOperator, setSelectedOperator] = useState(defaultOperator);
  const [filterValue, setFilterValue] = useState("");

  const onFilterSelected = (e) => {
    e.preventDefault();

    setFilterValue("");

    const selectedOption = e.target.options[e.target.selectedIndex];
    setSelectedFilter({ name: selectedOption.text, field: selectedOption.value, type: selectedOption.type });

    if (selectedOption.value === "0") {
      // Reset.
      setSelectionMade(false);
      setSelectedOperator(defaultOperator);
    } else {
      setSelectionMade(true);
    }

    // Set options allowed by filter.
    if (fields[selectedOption.value]) {
      switch (fields[selectedOption.value].type) {
        case FILTER_TYPE.STRING:
        case FILTER_TYPE.DNS_TYPE:
          setAllowedOperators([
            OPERATORS.EQUALS,
            OPERATORS.NOT_EQUALS,
            OPERATORS.REGEX_MATCH,
            OPERATORS.NOT_REGEX_MATCH
          ]);
          break;
        case FILTER_TYPE.NUMERIC:
        case FILTER_TYPE.PORT_NUMBER:
          setAllowedOperators([
            OPERATORS.EQUALS_NUMERIC,
            OPERATORS.NOT_EQUALS_NUMERIC,
            OPERATORS.GREATER_THAN,
            OPERATORS.SMALLER_THAN
          ]);
          break;
        case FILTER_TYPE.IP_ADDRESS:
          setAllowedOperators([
            OPERATORS.EQUALS,
            OPERATORS.NOT_EQUALS,
            OPERATORS.REGEX_MATCH,
            OPERATORS.NOT_REGEX_MATCH,
            OPERATORS.IN_CIDR,
            OPERATORS.NOT_IN_CIDR,
            OPERATORS.IS_PRIVATE,
            OPERATORS.IS_NOT_PRIVATE
          ]);
          break;
      }
    }
  }

  useEffect(() => {
    // Select first allowed operator if allowed operators change (after filter selection)
    if (allowedOperators.length > 0) {
      setSelectedOperator(allowedOperators[0]);
    }
  }, [allowedOperators]);

  const onOperatorSelected = (e) => {
    e.preventDefault();

    const selectedOperator = OPERATORS[e.target.value.toUpperCase()];

    if (selectedOperator.no_value) {
      setFilterValue("");
    }

    setSelectedOperator(selectedOperator);
  }

  const onFilterValueChanged = (e) => {
    e.preventDefault();

    setFilterValue(e.target.value);
  }

  const onFilterAdded = (e) => {
    e.preventDefault();

    const f = {
      name: selectedFilter.name,
      field: selectedFilter.field,
      operator: selectedOperator.name,
      value: filterValue
    }

    setFilters(prev => [...prev, f]);
  }

  return (
      <div className="filters mt-3">
        <h4>Filters</h4>

        <div className="input-group mb-3">
          <select className="form-select form-select-sm" value={selectedFilter.field}
                  onChange={onFilterSelected}>
            <option value="0">Select a Field</option>

            {Object.keys(fields).map((field, i) => {
              return <option value={field} key={i}>{fields[field].title}</option>
            })}
          </select>

          <select className="form-select form-select-sm" value={selectedOperator.name}
                  onChange={onOperatorSelected} disabled={!selectionMade}>
            {Object.keys(allowedOperators).map((op, i) => {
              return <option value={allowedOperators[op].name} key={i}>{allowedOperators[op].sign}</option>
            })}
          </select>

          <FilterValueInput filterValue={filterValue}
                            fieldType={selectedOperator.field_type}
                            filterType={selectedFilter.type}
                            onChange={onFilterValueChanged}
                            disabled={!selectionMade || (selectionMade && selectedOperator.no_value)} />

          <button className="btn btn-primary" type="button" disabled={selectedFilter.field === "0"} onClick={onFilterAdded}>
            Add Filter
          </button>
        </div>

        <AppliedFilterList filters={filters} />
      </div>
  )

}