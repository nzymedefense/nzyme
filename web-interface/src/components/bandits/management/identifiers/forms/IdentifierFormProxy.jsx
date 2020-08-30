import React from 'react';
import FingerprintIdentifierForm from "./FingerprintIdentifierForm";
import SignalStrengthIdentifierForm from "./SignalStrengthIdentifierForm";
import SSIDIdentifierForm from "./SSIDIdentifierForm";
import PwnagotchiIdentityForm from "./PwnagotchiIdentityForm";

class IdentifierFormProxy extends React.Component {

    render() {
        const formType = this.props.formType;
        if (formType === "FINGERPRINT") {
            return (
                <FingerprintIdentifierForm configurationUpdate={this.props.configurationUpdate} />
            )
        }

        if (formType === "SIGNAL_STRENGTH") {
            return (
                <SignalStrengthIdentifierForm configurationUpdate={this.props.configurationUpdate} />
            )
        }

        if (formType === "SSID") {
            return (
                <SSIDIdentifierForm configurationUpdate={this.props.configurationUpdate} />
            )
        }

        if (formType === "PWNAGOTCHI_IDENTITY") {
            return (
                <PwnagotchiIdentityForm configurationUpdate={this.props.configurationUpdate} />
            )
        }

        return (
            <div className="alert alert-danger">UNKNOWN FORM TYPE. NOT IMPLEMENTED.</div>
        )
    }

}

export default IdentifierFormProxy;