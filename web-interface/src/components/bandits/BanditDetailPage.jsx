import React from 'react';
import Reflux from 'reflux';
import BanditsActions from "../../actions/BanditsActions";
import BanditsStore from "../../stores/BanditsStore";
import LoadingSpinner from "../misc/LoadingSpinner";
import moment from "moment";
import Routes from "../../util/Routes";
import BanditIdentifiersTable from "./BanditIdentifiersTable";
import ContactsTable from "./ContactsTable";
import {Redirect} from "react-router-dom";
import TrackersStore from "../../stores/TrackersStore";
import TrackersActions from "../../actions/TrackersActions";
import BanditTracking from "./BanditTracking";

class BanditDetailPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.banditId = decodeURIComponent(props.match.params.id);

        this.stores = [BanditsStore, TrackersStore];

        this.state = {
            bandit: undefined,
            trackers: undefined,
            groundstationEnabled: undefined
        };

        this._deleteBandit = this._deleteBandit.bind(this);
        this._editBandit = this._editBandit.bind(this);
        this._loadBandit = this._loadBandit.bind(this);
        this._invalidateIdentifiers = this._invalidateIdentifiers.bind(this);
        this._createIdentifier = this._createIdentifier.bind(this);
    }

    componentDidMount() {
        const self = this;
        self._loadBandit();
        self._loadTrackers();

        setInterval(function () {
            self._loadBandit();
            self._loadTrackers();
        }, 15000);
    }

    _invalidateIdentifiers() {
        this.setState({bandit: undefined});
        this._loadBandit();
    }

    _editBandit(e) {
        if (this.state.bandit.read_only) {
            alert("Cannot edit a built-in bandit.");
            e.preventDefault();
        }
    }

    _deleteBandit() {
        if (this.state.bandit.read_only) {
            alert("Cannot delete a built-in bandit.");
            return;
        }

        if (!window.confirm("Delete bandit?")) {
            return;
        }

        const self = this;
        BanditsActions.deleteBandit(this.banditId, function () {
            self.setState({deleted:true})
        })
    }

    _createIdentifier(e) {
        if (this.state.bandit.read_only) {
            alert("Cannot create identifier for built-in bandit.");
            e.preventDefault();
        }
    }

    _loadBandit() {
        BanditsActions.findOne(this.banditId);
    }

    _loadTrackers() {
        TrackersActions.findAll();
    }

    render() {
        if (this.state.deleted) {
            return <Redirect to={Routes.BANDITS.INDEX} />;
        }

        if (!this.state.bandit || !this.state.trackers) {
            return <LoadingSpinner />
        }

        const bandit = this.state.bandit;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.BANDITS.INDEX}>Bandits</a></li>
                                <li className="breadcrumb-item active" aria-current="page">{bandit.name}</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>Bandit <em>{bandit.name}</em> {bandit.read_only && <i className="fas fa-shield-alt built-in-bandit" title="Built-in bandit"/>}</h1>
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
                            <button className="btn btn-danger" onClick={this._deleteBandit}>Delete Bandit</button>&nbsp;
                            <a href={Routes.BANDITS.EDIT(this.banditId)} className="btn btn-primary" onClick={this._editBandit}>Edit Bandit</a>
                        </span>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />

                        <h3>Description</h3>

                        <div className="alert alert-primary">
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
                        <a href={Routes.BANDITS.NEW_IDENTIFIER(bandit.uuid)} className="btn btn-success float-right" onClick={this._createIdentifier}>
                            Create Identifier
                        </a>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <BanditIdentifiersTable bandit={bandit} onInvalidateIdentifiers={this._invalidateIdentifiers} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-9">
                        <h3>Tracking / Physical Location</h3>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <BanditTracking groundstationEnabled={this.state.groundstationEnabled} bandit={bandit} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h3>Contacts <small>last 50</small></h3>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <ContactsTable contacts={bandit.contacts} />
                    </div>
                </div>
            </div>
        )
    }

}

export default BanditDetailPage;