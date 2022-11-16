import React from "react";

function ConfigurationInputField(props) {

    switch(props.type) {
        case "STRING":
            return <input type="text" className="form-control" id="config-value" value={props.value ? props.value : ""} onChange={(e) => props.setValue(e.target.value) } />;
        case "NUMBER":
            return <input type="number" className="form-control" id="config-value" value={props.value ? props.value : ""} onChange={(e) => props.setValue(e.target.value)} />;
            break;
        default:
            return <div>Unknown field type.</div>;
    }

}

export default ConfigurationInputField;2