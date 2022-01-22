import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import LoadingSpinner from '../misc/LoadingSpinner'
import moment from 'moment'
import Routes from '../../util/ApiRoutes'
import BanditIdentifiersTable from './BanditIdentifiersTable'
import ContactsTable from './ContactsTable'
import { Navigate } from 'react-router-dom'
import BanditTracking from './BanditTracking'
import BanditsService from '../../services/BanditsService'
import TrackersService from '../../services/TrackersService'
import DeleteBanditButton from './DeleteBanditButton';
import EditBanditButton from './EditBanditButton'
import CreateIdentifierButton from './CreateIdentifierButton'

const banditsService = new BanditsService();
const trackersService = new TrackersService();

function fetchData(banditId, setBandit, setTrackers, setGroundstationEnabled) {
  banditsService.findOne(banditId, setBandit);
  trackersService.findAll(setTrackers, setGroundstationEnabled);
}

function BanditDetailPage() {

  const { banditId } = useParams();

  const [bandit, setBandit] = useState(null);
  const [trackers, setTrackers] = useState(null);
  const [deleted, setDeleted] = useState(false);
  const [groundstationEnabled, setGroundstationEnabled] = useState(null);

  useEffect(() => {
    fetchData(banditId, setBandit, setTrackers, setGroundstationEnabled);
    const id = setInterval(() => fetchData(banditId, setBandit, setTrackers, setGroundstationEnabled), 5000);
    return () => clearInterval(id);
  }, [banditId, bandit]);

  if (deleted) {
    return <Navigate to={Routes.BANDITS.INDEX} />
  }

  if (!bandit || !trackers) {
    return <LoadingSpinner />
  }

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
                    <DeleteBanditButton bandit={bandit} trackers={trackers} setDeleted={setDeleted} banditsService={banditsService} />&nbsp;
                    <EditBanditButton bandit={bandit} />
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
            <div className="col-md-12">
                <h3>Contacts <small>last 50</small></h3>
            </div>
        </div>

        <div className="row">
            <div className="col-md-12">
                <ContactsTable contacts={bandit.contacts} />
            </div>
        </div>

        <div className="row">
            <div className="col-md-12">
                <hr />
            </div>
        </div>

        <div className="row mt-3">
            <div className="col-md-9">
                <h3>Identifiers</h3>
            </div>

            <div className="col-md-3">
                <CreateIdentifierButton bandit={bandit} trackers={trackers} />
            </div>
        </div>

        <div className="row">
            <div className="col-md-12">
                <BanditIdentifiersTable bandit={bandit} trackers={trackers} />
            </div>
        </div>

        <div className="row">
            <div className="col-md-9">
                <h3>Tracking / Physical Location</h3>
            </div>
        </div>

        <div className="row">
            <div className="col-md-12">
                <BanditTracking groundstationEnabled={groundstationEnabled} bandit={bandit} setBandit={setBandit} />
            </div>
        </div>
    </div>
  )

}

export default BanditDetailPage
