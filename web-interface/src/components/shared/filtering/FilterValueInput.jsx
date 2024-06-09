import React from "react";
import {FIELD_TYPE} from "./Filters";

export default function FilterValueInput(props) {

  const filterValue = props.filterValue;
  const onChange = props.onChange;
  const fieldType = props.fieldType;
  const filterType = props.filterType;
  const disabled = props.disabled;

  const fieldTypeAsInputType = () => {
    switch (fieldType) {
      case FIELD_TYPE.ANY_TEXT:
      case FIELD_TYPE.REGEX_TEXT:
      case FIELD_TYPE.CIDR:
      case FIELD_TYPE.NO_VALUE:
        return "text"
      case FIELD_TYPE.NUMBER:
        return "number"
      default:
        return "text"
    }
  }

  return (
      <input className="form-control" type={fieldTypeAsInputType()}
             disabled={disabled}
             value={filterValue}
             onChange={onChange} />
  )

}