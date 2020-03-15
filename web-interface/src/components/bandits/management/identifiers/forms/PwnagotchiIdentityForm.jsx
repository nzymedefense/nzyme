import React from 'react';
import Reflux from 'reflux';

class PwnagotchiIdentityForm extends Reflux.Component {

    constructor(props) {
        super(props);

        this.state = {
            identity: "",
            errorMessage: "",
        };

        this._handleUpdate = this._handleUpdate.bind(this);
    }

    _handleUpdate(e) {
        const identity = e.target.value.replace(/ /g,'');
        this.setState({identity: identity, errorMessage: ""});

        if (identity.length !== 64) {
            this.setState({errorMessage: "Invalid Pwnagotchi identity. A valid identity is 64 characters long."});
            this.props.configurationUpdate({ready: false});
            return;
        }

        const explanation = identity ? "a Pwnagotchi with identity \""+ identity +"\" is recorded" : undefined;

        this.props.configurationUpdate({
            configuration: {
                identity: identity
            },
            explanation: explanation,
            ready: true
        });
    }

    render() {
        return (
            <form onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                    <label htmlFor="identity">Pwnagotchi Identity</label>
                    <input type="text" className="form-control" id="identifier" placeholder="Enter the Pwnagotchi identity"
                           value={this.state.identity} onChange={this._handleUpdate} required />

                    <span className="text-danger">{this.state.errorMessage}</span>
                </div>
            </form>
        )
    }

}

export default PwnagotchiIdentityForm;