import React from 'react';
import BanditForm from "./BanditForm";
import Routes from "../../../util/Routes";
import LoadingSpinner from "../../misc/LoadingSpinner";
import {notify} from "react-notify-toast";
import BanditsService from "../../../services/BanditsService";

class EditBanditPage extends React.Component {

    constructor(props) {
        super(props);

        this.banditId = decodeURIComponent(props.match.params.id);

        this.state = {
            bandit: undefined
        }

        this.banditsService = new BanditsService();
        this.banditsService.findOne = this.banditsService.findOne.bind(this);
    }

    componentDidMount() {
        this.banditsService.findOne(this.banditId);
    }

    _editBandit(e) {
        e.preventDefault();

        const self = this;
        this.setState({submitting: true});

        this.banditsService.updateBandit(
            self.state.banditId, self.nameInput.current.value, self.descriptionInput.current.value,
            function () {
                self.setState({submitting: false, submitted: true});
                notify.show("Bandit updated.", "success");
            },
            function () {
                self.setState({submitting: false});
                notify.show("Could not update bandit. Please check nzyme log file.", "error");
            }
        );
    }

    render() {
        if (!this.state.bandit) {
            return <LoadingSpinner />
        }

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item">
                                    <a href={Routes.BANDITS.INDEX}>Bandits</a>
                                </li>
                                <li className="breadcrumb-item" aria-current="page">
                                    <a href={Routes.BANDITS.SHOW(this.state.bandit.uuid)}>{this.state.bandit.name}</a>
                                </li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    Edit
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>Edit Bandit <em>{this.state.bandit.name}</em></h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <BanditForm formHandler={this._editBandit}
                                    backLink={Routes.BANDITS.SHOW(this.banditId)}
                                    bandit={this.state.bandit}
                                    submitName="Edit Bandit" />
                    </div>
                </div>
            </div>
        )
    }

}

export default EditBanditPage;