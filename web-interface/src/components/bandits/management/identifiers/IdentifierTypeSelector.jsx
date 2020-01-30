import React from 'react';
import Reflux from 'reflux';

class IdentifierTypeSelector extends Reflux.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const types = this.props.types;
        return (
            <select>
                {Object.keys(types).map(function (key,i) {
                    return <option>{types[key]}</option>
                })}
            </select>
        )
    }

}

export default IdentifierTypeSelector;