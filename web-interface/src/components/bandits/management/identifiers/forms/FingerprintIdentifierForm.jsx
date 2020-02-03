import React from 'react';
import Reflux from 'reflux';

class FingerprintIdentifierForm extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            fingerprint: "",
            errorMessage: "",
       };

        this._handleUpdate = this._handleUpdate.bind(this);
    }

    _handleUpdate(e) {
        const fingerprint = e.target.value.replace(/ /g,'');
        this.setState({fingerprint: fingerprint, errorMessage: ""});

        if (fingerprint.length !== 64) {
            this.setState({errorMessage: "Invalid fingerprint. A valid fingerprint is 64 characters long."});
            this.props.configurationUpdate({ready: false});
            return;
        }

        const explanation = fingerprint ? "a frame with fingerprint \""+ fingerprint +"\" is recorded" : undefined;

        this.props.configurationUpdate({
            configuration: {
                fingerprint: fingerprint
            },
            explanation: explanation,
            ready: true
        });
    }

    render() {
        return (
            <form>
                <div className="form-group">
                    <label htmlFor="fingerprint">Fingerprint</label>
                    <input type="text" className="form-control" id="fingerprint" placeholder="Enter the fingerprint of the bandit"
                           value={this.state.fingerprint} onChange={this._handleUpdate} required />

                    <span className="text-danger">{this.state.errorMessage}</span>
                </div>
            </form>
        )
    }

}

export default FingerprintIdentifierForm;