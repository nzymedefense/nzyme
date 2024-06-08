import React, {useState} from 'react';
import AppliedFilterList from "./AppliedFilterList";

export const FILTER_TYPE = {
  STRING: "string",
  NUMERIC: "numeric",
  IP_ADDRESS: "ip_address"
}

export default function Filters(props) {

  const fields = props.fields;
  const filters = props.filters;
  const setFilters = props.setFilters;

  const defaultOperator = "==";

  const [selectionMade, setSelectionMade] = useState(false);
  const [selectedFilter, setSelectedFilter] = useState({ name: "", field: "0" });
  const [allowedOperators, setAllowedOperators] = useState([defaultOperator]);
  const [selectedOperator, setSelectedOperator] = useState(defaultOperator);
  const [filterValue, setFilterValue] = useState("");

  const onFilterSelected = (e) => {
    e.preventDefault();

    const selectedOption = e.target.options[e.target.selectedIndex];
    setSelectedFilter({ name: selectedOption.text, field: selectedOption.value });

    if (selectedOption.value === "0") {
      // Reset.
      setSelectionMade(false);
      setSelectedOperator(defaultOperator);
      setFilterValue("");
    } else {
      setSelectionMade(true);
    }

    // Set options allowed by filter.
    switch (fields[selectedOption.value].type) {
      case FILTER_TYPE.STRING:
        setAllowedOperators([
            "==",
            "~="
        ]);
        break;
      case FILTER_TYPE.NUMERIC:
        setAllowedOperators([
            "==",
            ">",
            "<"
        ]);
        break;
      case FILTER_TYPE.IP_ADDRESS:
        setAllowedOperators([
            "==",
            "~=",
            "IN CIDR",
            "IS INTERNAL"
        ]);
        break;
    }
  }

  const onOperatorSelected = (e) => {
    e.preventDefault();

    setSelectedOperator(e.target.value);
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
      operator: selectedOperator,
      value: filterValue
    }

    setFilters(prev => [...prev, f]);
  }

  return (
      <div className="filters mt-3">
        <h4>Filters</h4>

        <div className="input-group mb-3">
          <select className="form-select form-select-sm" value={selectedFilter.field} style={{width: 160}}
                  onChange={onFilterSelected}>
            <option value="0">Select a Field</option>

            {Object.keys(fields).map((field, i) => {
              return <option value={field} key={i}>{fields[field].title}</option>
            })}
          </select>

          <select className="form-select form-select-sm" value={selectedOperator}
                  onChange={onOperatorSelected} disabled={!selectionMade}>
            {allowedOperators.map((operator, i) => {
              return <option value={operator} key={i}>{operator}</option>
            })}
          </select>

          <input className="form-control" type="text" style={{width: 250}} disabled={!selectionMade}
                 value={filterValue} onChange={onFilterValueChanged} />

          <button className="btn btn-primary" type="button" disabled={selectedFilter.field === "0"} onClick={onFilterAdded}>
            Add Filter
          </button>
        </div>

        <AppliedFilterList filters={filters} />
      </div>
  )

}