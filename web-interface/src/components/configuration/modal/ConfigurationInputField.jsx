import React from "react";

function ConfigurationInputField(props) {

    function updateValue(value) {
        if (!props.disabled) {
            props.setValue(value);
        }
    }

    switch(props.type) {
        case "STRING":
            return <input type="text"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ""} onChange={(e) => updateValue(e.target.value) }/>;
        case "NUMBER":
            return <input type="number"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ""} onChange={(e) => updateValue(parseInt(e.target.value, 10))} />;
        default:
            return <div>Unknown field type.</div>;
    }

}

export default ConfigurationInputField;2