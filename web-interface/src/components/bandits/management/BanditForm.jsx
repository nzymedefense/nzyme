import React from 'react';
import Redirect from "react-router-dom/Redirect";
import BanditsService from "../../../services/BanditsService";

class BanditForm extends React.Component {

    constructor(props) {
        super(props);

        this.formHandler = this.props.formHandler.bind(this);

        this.nameInput = React.createRef();
        this.descriptionInput = React.createRef();

        this.state = {
            submitting: false,
            submitted: false,
            banditId: props.bandit ? props.bandit.uuid : undefined,
            banditName: props.bandit ? props.bandit.name : "",
            banditDescription: props.bandit ? props.bandit.description : ""
        };

        this._handleNameInput = this._handleNameInput.bind(this);
        this._handleDescriptionInput = this._handleDescriptionInput.bind(this);

        this.banditsService = new BanditsService();
        this.banditsService.createBandit = this.banditsService.createBandit.bind(this);
        this.banditsService.updateBandit = this.banditsService.updateBandit.bind(this);
    }

    _handleNameInput(e) {
        this.setState({banditName: e.target.value});
    }

    _handleDescriptionInput(e) {
        this.setState({banditDescription: e.target.value});
    }

    render() {
        if (this.state.submitted) {
            return ( <Redirect to={this.props.backLink} /> );
        }

        return (
            <form onSubmit={this.formHandler}>
                <div className="form-group">
                    <label htmlFor="name">Name</label>
                    <input type="text" className="form-control" id="name" placeholder="Enter the name of this bandit"
                           ref={this.nameInput} value={this.state.banditName} onChange={this._handleNameInput} maxLength={75} required />
                </div>
                <div className="form-group">
                    <label htmlFor="description">Description</label>
                    <textarea className="form-control" id="description" placeholder="Enter the description of this bandit"
                              ref={this.descriptionInput} value={this.state.banditDescription} onChange={this._handleDescriptionInput} required />
                </div>
                <button type="submit" className="btn btn-success" disabled={this.state.submitting}>{this.props.submitName}</button>&nbsp;
                <a href={this.props.backLink} className="btn btn-dark">Back</a>
            </form>
        )
    }

}

export default BanditForm;