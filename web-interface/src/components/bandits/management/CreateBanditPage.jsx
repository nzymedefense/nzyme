import React from 'react';
import Reflux from 'reflux';
import BanditForm from "./BanditForm";
import BanditsStore from "../../../stores/BanditsStore";
import BanditsActions from "../../../actions/BanditsActions";
import {notify} from "react-notify-toast";

class CreateBanditPage extends Reflux.Component {

    constructor() {
        super();

        this.store = BanditsStore;
    }

    _createBandit(e) {
        e.preventDefault();

        const self = this;
        this.setState({submitting: true});

        BanditsActions.createBandit(
            self.nameInput.current.value, self.descriptionInput.current.value,
            function () {
                self.setState({submitting: false, submitted: true});
                notify.show("Bandit created.", "success");
            },
            function () {
                self.setState({submitting: false});
                notify.show("Could not create bandit. Please check nzyme log file.", "error");
            }
        );
    }

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Create Bandit</h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <BanditForm formHandler={this._createBandit}/>
                    </div>
                </div>
            </div>
        )
    }

}

export default CreateBanditPage;