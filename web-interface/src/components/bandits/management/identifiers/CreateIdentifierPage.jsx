import React from 'react';
import Reflux from 'reflux';
import BanditsStore from "../../../../stores/BanditsStore";
import BanditsActions from "../../../../actions/BanditsActions";
import LoadingSpinner from "../../../misc/LoadingSpinner";
import IdentifierTypeSelector from "./IdentifierTypeSelector";
import IdentifierFormProxy from "./forms/IdentifierFormProxy";
import IdentifierExplanation from "./IdentifierExplanation";
import Routes from "../../../../util/Routes";
import Redirect from "react-router-dom/Redirect";

class CreateIdentifierPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.banditId = decodeURIComponent(props.match.params.banditUUID);

        this.store = BanditsStore;

        this.state = {
            banditIdentifierTypes: undefined,
            selectedType: undefined,
            configuration: undefined,
            explanation: undefined,
            submitting: false,
            submitted: false
        };

        this._selectType = this._selectType.bind(this);
        this._configurationUpdate = this._configurationUpdate.bind(this);
        this._submitForm = this._submitForm.bind(this);
    }

    componentDidMount() {
        BanditsActions.findAllIdentifierTypes();
    }

    _selectType(value) {
        this.setState({selectedType: value, explanation: "", configuration: {}});
    }

    _configurationUpdate(obj) {
        this.setState({configuration: obj.configuration, explanation: obj.explanation, formReady: obj.ready});
    }

    _submitForm(e) {
        e.preventDefault();
        BanditsActions.createIdentifier(this.banditId, {type: this.state.selectedType, configuration: this.state.configuration});
    }

    render() {
        if (this.state.submitted) {
            return ( <Redirect to={Routes.BANDITS.SHOW(this.banditId)} /> );
        }

        if (!this.state.banditIdentifierTypes) {
            return <LoadingSpinner />;
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Create Identifier</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <div className="row">
                            <div className="col-md-12">
                                <strong>Step 1)</strong> Select an identifier type:&nbsp;
                                <IdentifierTypeSelector types={this.state.banditIdentifierTypes} onChange={this._selectType} />
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{"display": this.state.selectedType ? "block" : "none"}}>
                            <div className="col-md-12">
                                <strong>Step 2)</strong> Configure identifier details:

                                <div className="row mt-md-3">
                                    <div className="col-md-1" />
                                    <div className="col-md-8">
                                        <IdentifierFormProxy
                                            formType={this.state.selectedType}
                                            configurationUpdate={this._configurationUpdate}/>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{"display": this.state.selectedType ? "block" : "none"}}>
                            <div className="col-md-12">
                                <strong>Step 3)</strong> Confirm configuration:

                                <div className="row mt-md-3">
                                    <div className="col-md-12">
                                        <IdentifierExplanation explanation={this.state.explanation} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{"display": this.state.selectedType ? "block" : "none"}}>
                            <div className="col-md-12">
                                <button className="btn btn-success"
                                        onClick={this._submitForm}
                                        disabled={!this.state.formReady || (this.state.formReady && this.state.submitting)}>Create Identifier</button>&nbsp;
                                <a href={Routes.BANDITS.SHOW(this.banditId)} className="btn btn-dark">Back</a>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="alert alert-info">
                            <h3>Help</h3>
                            <p>
                                Identifiers are attributes that describe a bandit. For example, you could use identifiers
                                to describe a bandit that probes for two specific SSIDs, is always detected using a weak
                                signal strength and also only uses specific channels.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

}

export default CreateIdentifierPage;