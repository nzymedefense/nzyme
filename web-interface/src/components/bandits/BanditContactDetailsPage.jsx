import React, { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import LoadingSpinner from '../misc/LoadingSpinner'
import BanditsService from '../../services/BanditsService'
import Routes from '../../util/ApiRoutes'
import moment from 'moment'
import Timespan from '../misc/Timespan'
import RSSI from '../misc/RSSI'
import numeral from 'numeral'
import AdvertisedSSIDTable from './AdvertisedSSIDTable'
import AdvertisedBSSIDTable from './AdvertisedBSSIDTable'
import SimpleBarChart from '../charts/SimpleBarChart'

const banditsService = new BanditsService();

function fetchData(banditUUID, contactUUID, detailedSSIDs, detailedBSSIDs, setContact) {
    banditsService.findContactOfBandit(
        banditUUID,
        contactUUID,
        detailedSSIDs.join(),
        detailedBSSIDs.join(),
        setContact
    );
}

function formatHistogram(counts) {
    const result = []

    Object.keys(counts).forEach(function (key) {
      const x = []
      const y = []

      Object.keys(counts[key]).sort().forEach(function (countKey) {
        x.push(new Date(countKey))
        y.push(counts[key][countKey])
      })

      result.push({
        x: x,
        y: y,
        type: 'line',
        name: key,
        line: { width: 1, shape: 'linear' }
      })
    })

    return result
}

function BanditContactDetailsPage() {

    const { banditUUID, contactUUID } = useParams();

    const [contact, setContact] = useState(null);
    const [detailedSSIDs, setDetailedSSIDs] = useState([]);
    const [detailedBSSIDs, setDetailedBSSIDs] = useState([]);

    useEffect(() => {
        fetchData(banditUUID, contactUUID, detailedSSIDs, detailedBSSIDs, setContact);
        const id = setInterval(() => fetchData(banditUUID, contactUUID, detailedSSIDs, detailedBSSIDs, setContact), 5000);
        return () => clearInterval(id);
      }, [banditUUID, contactUUID, detailedSSIDs, detailedBSSIDs]);

    if (!contact) {
      return <LoadingSpinner />
    }

    function onNewSSIDSelection(ssids) {
        setDetailedSSIDs([...ssids]);
    }

    function onNewBSSIDSelection(bssids) {
        setDetailedBSSIDs([...bssids]);
    }

    return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <nav aria-label="breadcrumb">
                            <ol className="breadcrumb">
                                <li className="breadcrumb-item"><a href={Routes.BANDITS.INDEX}>Bandits</a></li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    <a href={Routes.BANDITS.SHOW(contact.bandit_uuid)}>{contact.bandit_name}</a>
                                </li>
                                <li className="breadcrumb-item active" aria-current="page">Contact Details</li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <h1>
                            Bandit Contact <em>{contact.uuid.substr(0, 8)}</em>&nbsp;
                            <small>{contact.is_active ? <span className="badge badge-success pull-right contact-status">active</span> : <span className="badge badge-primary pull-right contact-status">not active</span>}</small>
                        </h1>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-7">
                        <dl>
                            <dt>Bandit:</dt>
                            <dd>{contact.bandit_name}</dd>
                        </dl>
                    </div>
                    <div className="col-md-4">
                        <dl>
                            <dt>Contact ID:</dt>
                            <dd>{contact.uuid}</dd>
                        </dl>
                    </div>
                    <div className="col-md-1">
                        <a href={Routes.BANDITS.SHOW(contact.bandit_uuid)} className="btn btn-dark">Back</a>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-12">
                        <hr />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-2">
                        <dl>
                            <dt>First Seen:</dt>
                            <dd title={moment(contact.first_seen).format()}>
                                {moment(contact.first_seen).format('M/D/YY hh:mm a')}
                            </dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Last Seen:</dt>
                            <dd title={moment(contact.last_seen).format()}>
                                {moment(contact.last_seen).format('M/D/YY hh:mm a')}
                            </dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Duration:</dt>
                            <dd><Timespan from={contact.first_seen} to={contact.last_seen} /></dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Frames:</dt>
                            <dd>{numeral(contact.frame_count).format('0,0')}</dd>
                        </dl>
                    </div>

                    <div className="col-md-2">
                        <dl>
                            <dt>Signal:</dt>
                            <dd>{contact.is_active ? <RSSI rssi={contact.last_signal} /> : 'n/a'}</dd>
                        </dl>
                    </div>
                </div>

                <div className="row mt-3">
                    <div className="col-md-6">
                        <h2>Advertised SSIDs</h2>
                        <AdvertisedSSIDTable ssids={contact.ssids} onNewSelection={onNewSSIDSelection} />
                    </div>
                    <div className="col-md-6">
                        <h2>Advertised BSSIDs</h2>
                        <AdvertisedBSSIDTable bssids={contact.bssids} onNewSelection={onNewBSSIDSelection} />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Frame Count (by SSID, most recent 24h max)"
                            showLegend={true}
                            width={540}
                            height={150}
                            customMarginTop={30}
                            finalData={formatHistogram(contact.ssid_frame_count_histograms)}/>
                    </div>

                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Frame Count (by BSSID, most recent 24h max)"
                            showLegend={true}
                            width={545}
                            height={150}
                            customMarginTop={30}
                            finalData={formatHistogram(contact.bssid_frame_count_histograms)}/>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Signal Strength (by SSID, most recent 24h max)"
                            showLegend={true}
                            width={540}
                            height={150}
                            customMarginTop={30}
                            finalData={formatHistogram(contact.ssid_signal_strength_histograms)}/>
                    </div>

                    <div className="col-md-6">
                        <SimpleBarChart
                            title="Signal Strength (by BSSID, most recent 24h max)"
                            showLegend={true}
                            width={545}
                            height={150}
                            customMarginTop={30}
                            finalData={formatHistogram(contact.bssid_signal_strength_histograms)}/>
                    </div>
                </div>

            </div>
    )
  
}

export default BanditContactDetailsPage
