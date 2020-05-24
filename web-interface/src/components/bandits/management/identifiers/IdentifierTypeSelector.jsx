import React from 'react';
import Reflux from 'reflux';
import {startCase} from "lodash/string";

class IdentifierTypeSelector extends Reflux.Component {

    constructor(props) {
        super(props);

        this.selector = React.createRef();
    }

    render() {
        const types = this.props.types;

        return (
            <select ref={this.selector} onChange={() => this.props.onChange(this.selector.current.value)}>
                <option key="default-empty" />
                {Object.keys(types).map(function (key,i) {
                    return <option value={types[key]} key={types[key]}>{startCase(types[key])}</option>
                })}
            </select>
        )
    }

}

export default IdentifierTypeSelector;