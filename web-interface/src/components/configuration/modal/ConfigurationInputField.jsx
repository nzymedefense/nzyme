import React from "react";

function ConfigurationInputField(props) {

    switch(props.type) {
        case "STRING":
            return <input type="text"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ""} onChange={(e) => props.setValue(e.target.value) }/>;
        case "NUMBER":
            return <input type="number"
                          className="form-control"
                          autoComplete="off"
                          value={props.value ? props.value : ""} onChange={(e) => props.setValue(parseInt(e.target.value, 10))} />;
            break;
        default:
            return <div>Unknown field type.</div>;
    }

}

export default ConfigurationInputField;2