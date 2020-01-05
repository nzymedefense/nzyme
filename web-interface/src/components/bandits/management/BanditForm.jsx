import React from 'react';
import Reflux from 'reflux';
import Redirect from "react-router-dom/Redirect";
import Routes from "../../../util/Routes";

class BanditForm extends Reflux.Component {

    constructor(props) {
        super(props);

        this.formHandler = this.props.formHandler.bind(this);

        this.nameInput = React.createRef();
        this.descriptionInput = React.createRef();

        this.state = {
            submitting: false,
            submitted: false
        };
    }

    render() {
        if (this.state.submitted) {
            return ( <Redirect to={Routes.BANDITS.INDEX} /> );
        }

        return (
            <div>
                <form onSubmit={this.formHandler}>
                    <div className="form-group">
                        <label htmlFor="name">Name</label>
                        <input type="text" className="form-control" id="name" placeholder="Enter the name of this bandit" ref={this.nameInput} maxLength={75} required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <textarea className="form-control" id="description" placeholder="Enter the description of this bandit" ref={this.descriptionInput} required />
                    </div>
                    <button type="submit" className="btn btn-success" disabled={this.state.submitting}>Create Bandit</button>&nbsp;
                    <a href={this.props.backLink} className="btn btn-dark">Back</a>
                </form>
            </div>
        )
    }

}

export default BanditForm;