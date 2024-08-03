import React, {useEffect, useState} from 'react';
import AppliedFilterList from "./AppliedFilterList";
import FilterValueInput from "./FilterValueInput";
import validateStringNotEmpty from "./validators/StringNotEmptyValidator";
import validateNumberNotNegative from "./validators/NumberNotNegativeValidator";
import validateIPAddressValid from "./validators/IPAddressValidator";
import validatePortNumberValid from "./validators/PortNumberValidator";
import validateDNSDataTypeValid from "./validators/DNSTypeValidator";
import validateCIDRValid from "./validators/CIDRValidator";

export const FILTER_TYPE = {
  STRING: {
    name: "string",
    validators: [validateStringNotEmpty],
    placeholder: null
  },
  NUMERIC: {
    name: "numeric",
    validators: [validateNumberNotNegative],
    placeholder: null
  },
  IP_ADDRESS: {
    name: "ip_address",
    validators: [validateIPAddressValid],
    placeholder: "172.16.0.1"
  },
  PORT_NUMBER: {
    name: "port_number",
    validators: [validatePortNumberValid],
    placeholder: null
  },
  DNS_TYPE: {
    name: "dns_type",
    validators: [validateDNSDataTypeValid],
    placeholder: "CNAME"
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
  EQUALS: {
    name: "equals",
    sign: "==",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.ANY_TEXT
  },
  NOT_EQUALS: {
    name: "not_equals",
    sign: "!=",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.ANY_TEXT
  },
  EQUALS_NUMERIC: {
    name: "equals_numeric",
    sign: "==",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.NUMBER
  },
  NOT_EQUALS_NUMERIC: {
    name: "not_equals_numeric",
    sign: "!=",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.NUMBER
  },
  REGEX_MATCH: {
    name: "regex_match",
    sign: "~=",
    placeholder: null,
    no_value: false,
    validators: [validateStringNotEmpty],
    field_type: FIELD_TYPE.REGEX_TEXT
  },
  NOT_REGEX_MATCH: {
    name: "not_regex_match",
    sign: "!~=",
    placeholder: null,
    no_value: false,
    validators: [validateStringNotEmpty],
    field_type: FIELD_TYPE.REGEX_TEXT
  },
  GREATER_THAN: {
    name: "greater_than",
    sign: ">",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.NUMBER
  },
  SMALLER_THAN: {
    name: "smaller_than",
    sign: "<",
    placeholder: null,
    no_value: false,
    validators: [],
    field_type: FIELD_TYPE.NUMBER
  },
  IN_CIDR: {
    name: "in_cidr",
    sign: "IN CIDR:",
    placeholder: "172.16.0.0/24",
    no_value: false,
    validators: [validateCIDRValid],
    field_type: FIELD_TYPE.CIDR
  },
  NOT_IN_CIDR: {
    name: "not_in_cidr",
    sign: "NOT IN CIDR:",
    placeholder: "172.16.0.0/24",
    no_value: false,
    validators: [validateCIDRValid],
    field_type: FIELD_TYPE.CIDR
  },
  IS_PRIVATE: {
    name: "is_private",
    sign: "IS PRIVATE",
    placeholder: null,
    no_value: true,
    validators: [() => { return true; }],
    field_type: FIELD_TYPE.NO_VALUE
  },
  IS_NOT_PRIVATE: {
    name: "is_not_private",
    sign: "IS NOT PRIVATE",
    placeholder: null,
    no_value: true,
    validators: [() => { return true; }],
    field_type: FIELD_TYPE.NO_VALUE
  }
}

export default function Filters(props) {

  const fields = props.fields;
  const filters = props.filters ? props.filters : {};
  const setFilters = props.setFilters;

  const hideTitle = props.hideTitle;
  const hideAppliedFilters = props.hideAppliedFilters === undefined || props.hideAppliedFilters === null ? false : props.hideAppliedFilters;
  const preSelectedField = props.preSelectedField;
  const preSelectedValue = props.preSelectedValue;

  const defaultOperator = OPERATORS.EQUALS;
  const defaultFilter = { name: "", field: "0", type: FILTER_TYPE.STRING };

  const [selectionMade, setSelectionMade] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState(defaultFilter);
  const [allowedOperators, setAllowedOperators] = useState([defaultOperator]);
  const [selectedOperator, setSelectedOperator] = useState(defaultOperator);
  const [filterValue, setFilterValue] = useState("");
  const [validatorsFailed, setValidatorsFailed] = useState(false);

  const onFilterSelected = (e) => {
    e.preventDefault();

    const selectedOption = e.target.options[e.target.selectedIndex];
    const filterOption = fields[selectedOption.value];

    if (filterOption) {
      changeFilter(filterOption.title, selectedOption.value, filterOption.type);
    } else {
      // The option dialog placeholder was selected again.
      changeFilter(defaultFilter.name, defaultFilter.field, defaultFilter.type);
    }
  }

  const changeFilter = (name, field, type) => {
    setFilterValue("");
    setSelectedFilter({ name: name, field: field, type: type });

    if (field === "0") {
      // Reset.
      setSelectionMade(false);
      setSelectedOperator(defaultOperator);
    } else {
      setSelectionMade(true);
    }

    // Set options allowed by filter.
    if (fields[field]) {
      switch (fields[field].type) {
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

    validate();
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

  useEffect(() => {
    validate();
  }, [filterValue, selectedOperator]);

  useEffect(() => {
    if (preSelectedField && preSelectedValue) {
      changeFilter(fields[preSelectedField].title, preSelectedField, fields[preSelectedField].type);
      setFilterValue(preSelectedValue);
    }
  }, [preSelectedField, preSelectedValue]);

  const validate = () => {
    // Potential operator validators have priority over filter validators.
    let validators;
    if (selectedOperator.validators.length > 0) {
      validators = selectedOperator.validators;
    } else if (selectedFilter.type.validators.length > 0) {
      validators = selectedFilter.type.validators;
    } else {
      validators = null;
    }

    let failed = false;
    for (const validator of validators) {
      if (!validator(filterValue)) {
        failed = true;
        break;
      }
    }

    setValidatorsFailed(failed);
  }

  const onFilterAdded = (e) => {
    e.preventDefault();

    const addedFilter = {
      name: selectedFilter.name,
      field: selectedFilter.field,
      operator: selectedOperator.name,
      sign: selectedOperator.sign,
      value: filterValue
    }

    // Check if this filter already exists and do not add again if so.
    for (const filterName of Object.keys(filters)) {
      const filterList = filters[filterName];

      for (const existingFilter of filterList) {
        if (existingFilter.field === addedFilter.field
            && existingFilter.operator === addedFilter.operator
            && existingFilter.value === addedFilter.value) {
          return;
        }
      }
    }

    const newFilters = JSON.parse(JSON.stringify(filters)); // TODO wft (this is a way to make sure we create a completely new object so useState notices the change. Refactor all of this goddamn)
    if (filters[selectedFilter.field]) {
      // We already have a filter for this field. Add to list of filter values. (Will be OR-connected in backend)
      newFilters[selectedFilter.field].push(addedFilter);
    } else {
      // First filter for this field.
      newFilters[selectedFilter.field] = [addedFilter];
    }

    setFilters(newFilters);
  }

  const onFilterRemoved = (e, deleteFilter) => {
    e.preventDefault();

    const newFilters = {};

    // Add all existing filters except the one to be removed.
    for (const filterName of Object.keys(filters)) {
      const filterList = filters[filterName];

      for (const existingFilter of filterList) {
        if (existingFilter.field !== deleteFilter.field
            || existingFilter.operator !== deleteFilter.operator
            || existingFilter.value !== deleteFilter.value) {

          if (newFilters[filterName]) {
            newFilters[filterName].push(existingFilter);
          } else {
            newFilters[filterName] = [existingFilter];
          }
        }
      }
    }

    setFilters(newFilters);
  }

  return (
      <div className="filters">
        {hideTitle ? null : <h4>Filters</h4> }

        <div className="input-group">
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
                            operator={selectedOperator}
                            filterType={selectedFilter.type}
                            onChange={onFilterValueChanged}
                            disabled={!selectionMade || (selectionMade && selectedOperator.no_value)} />

          <button className="btn btn-primary" type="button"
                  disabled={validatorsFailed || selectedFilter.field === "0"}
                  onClick={onFilterAdded}>
            Add Filter
          </button>
        </div>

        {!hideAppliedFilters && <AppliedFilterList filters={filters} onFilterRemoved={onFilterRemoved} />}
      </div>
  )

}