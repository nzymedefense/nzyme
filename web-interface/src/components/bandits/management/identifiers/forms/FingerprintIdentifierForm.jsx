import React from 'react';
import Reflux from 'reflux';

class FingerprintIdentifierForm extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            fingerprint: undefined,
            configuration: {},
            errorMessage: ""
       };

        this._handleUpdate = this._handleUpdate.bind(this);
    }


    _handleUpdate(e) {
        const fingerprint = e.target.value;
        this.setState({fingerprint: fingerprint, errorMessage: ""});

        if (fingerprint.length !== 0 && fingerprint.length !== 64) {
            this.setState({errorMessage: "Invalid fingerprint. A valid fingerprint is 64 characters long."})
            return;
        }

        const explanation = fingerprint ? "a frame with fingerprint \""+ fingerprint +"\" is recorded" : undefined;

        this.props.configurationUpdate({
            configuration: {
                type: "FINGERPRINT",
                fingerprint: fingerprint
            },
            explanation: explanation
        });
    }

    render() {
        return (
            <form>
                <div className="form-group">
                    <label htmlFor="fingerprint">Fingerprint</label>
                    <input type="text" className="form-control" id="fingerprint" placeholder="Enter the fingerprint of the bandit"
                           ref={this.fingerprint} value={this.state.fingerprint} maxLength={64} minLength={64} onChange={this._handleUpdate} required />

                    <span>{this.state.errorMessage}</span>
                </div>
            </form>
        )
    }

}

export default FingerprintIdentifierForm;