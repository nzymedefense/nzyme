import React from 'react';
import Reflux from 'reflux';
import FingerprintIdentifierForm from "./FingerprintIdentifierForm";

class IdentifierFormProxy extends Reflux.Component {

    render() {
        const formType = this.props.formType;
        if (formType === "FINGERPRINT") {
            return (
                <FingerprintIdentifierForm configurationUpdate={this.props.configurationUpdate} />
            )
        }

        if (formType === "SIGNAL_STRENGTH") {
            return (
                <span>strength</span>
            )
        }

        return (
            <div className="alert alert-danger">UNKNOWN FORM TYPE. NOT IMPLEMENTED.</div>
        )
    }

}

export default IdentifierFormProxy;