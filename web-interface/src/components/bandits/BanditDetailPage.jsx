import React from 'react';
import Reflux from 'reflux';
import BanditsActions from "../../actions/BanditsActions";
import BanditsStore from "../../stores/BanditsStore";
import LoadingSpinner from "../misc/LoadingSpinner";
import BeaconRate from "../networks/details/BeaconRate";
import moment from "moment";
import Routes from "../../util/Routes";
import BanditIdentifiersTable from "./BanditIdentifiersTable";

class BanditDetailPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.banditId = decodeURIComponent(props.match.params.id);

        this.store = BanditsStore;

        this.state = {
            bandit: undefined
        }
    }

    componentDidMount() {
        BanditsActions.findOne(this.banditId);
    }

    render() {
        if (!this.state.bandit) {
            return <LoadingSpinner />
        }

        const bandit = this.state.bandit;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Bandit <em>{bandit.name}</em></h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-3">
                        <dl>
                            <dt>Created at:</dt>
                            <dd>{moment(bandit.created_at).format()}</dd>
                        </dl>
                    </div>

                    <div className="col-md-3">
                        <dl>
                            <dt>Updated at:</dt>
                            <dd>{moment(bandit.updated_at).format()}</dd>
                        </dl>
                    </div>

                    <div className="col-md-6">
                        <span className="float-right">
                            <a href={Routes.BANDITS.INDEX} className="btn btn-dark">Back</a>&nbsp;
                            <a href={Routes.BANDITS.EDIT(this.banditId)} className="btn btn-primary">Edit Bandit</a>
                        </span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />

                        <h3>Description</h3>

                        <div className="alert alert-dark">
                            {bandit.description.split('\n').map((item, key) => {
                                return <span key={key}>{item}<br/></span>
                            })}
                        </div>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <h3>Identifiers</h3>
                    </div>

                    <div className="col-md-3">
                        <a href={Routes.BANDITS.NEW_IDENTIFIER(bandit.uuid)} className="btn btn-success float-right">Create Identifier</a>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <BanditIdentifiersTable bandit={bandit}/>
                    </div>
                </div>
            </div>
        )
    }

}

export default BanditDetailPage;