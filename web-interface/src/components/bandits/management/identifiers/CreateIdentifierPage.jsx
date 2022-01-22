import React, { useState, useEffect, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { Navigate } from 'react-router-dom'
import { notify } from 'react-notify-toast'

import LoadingSpinner from '../../../misc/LoadingSpinner'
import IdentifierTypeSelector from './IdentifierTypeSelector'
import IdentifierFormProxy from './forms/IdentifierFormProxy'
import IdentifierExplanation from './IdentifierExplanation'
import Routes from '../../../../util/ApiRoutes'

import BanditsService from '../../../../services/BanditsService';

const banditsService = new BanditsService();

function CreateIdentifierPage(props) {

    const { banditId } = useParams();

    const typeSelector = React.createRef();

    const [bandit, setBandit] = useState(null);
    const [banditIdentifierTypes, setBanditIdentifierTypes] = useState(null);
    const [selectedType, setSelectedType] = useState(null);
    const [configuration, setConfiguration] = useState(null);
    const [explanation, setExplanation] = useState(null);
    const [formReady, setFormReady] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    
    useEffect(() => {
        banditsService.findOne(banditId, setBandit);
        banditsService.findAllIdentifierTypes(setBanditIdentifierTypes);
    }, [banditId]);

    const selectType = useCallback(() => {
        setSelectedType(typeSelector.current.value);
    }, [typeSelector]);

    const submitForm = useCallback((e) => {
        e.preventDefault();
        setSubmitting(true);

        banditsService.createIdentifier(
            bandit.uuid,
            {
                type: selectedType,
                configuration: configuration
            },
            function() {
                notify.show('Identifier created.', 'success');
                setSubmitting(false);
                setSubmitted(true);
            }, function() {
                notify.show('Could not create identifier. Please check nzyme log file.', 'error');
                setSubmitting(false);
                setSubmitted(false);
            }
        );

    }, [bandit, selectedType, configuration])

    if (submitted) {
        return (<Navigate to={Routes.BANDITS.SHOW(bandit.uuid)} />)
    }

    if (!banditIdentifierTypes || !bandit) {
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
                                    <a href={Routes.BANDITS.SHOW(bandit.uuid)}>{bandit.name}</a>
                                </li>
                                <li className="breadcrumb-item active" aria-current="page">
                                    Create Identifier
                                </li>
                            </ol>
                        </nav>
                    </div>
                </div>

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
                                <IdentifierTypeSelector types={banditIdentifierTypes} onChange={selectType} selector={typeSelector} />
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{ display: selectedType ? 'block' : 'none' }}>
                            <div className="col-md-12">
                                <strong>Step 2)</strong> Configure identifier details:

                                <div className="row mt-md-3">
                                    <div className="col-md-1" />
                                    <div className="col-md-8">
                                        <IdentifierFormProxy
                                            formType={selectedType}
                                            setConfiguration={setConfiguration}
                                            setExplanation={setExplanation}
                                            setFormReady={setFormReady} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{ display: selectedType ? 'block' : 'none' }}>
                            <div className="col-md-12">
                                <strong>Step 3)</strong> Confirm configuration:

                                <div className="row mt-md-3">
                                    <div className="col-md-12">
                                        <IdentifierExplanation explanation={explanation} />
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="row mt-md-3" style={{ display: selectedType ? 'block' : 'none' }}>
                            <div className="col-md-12">
                                <button className="btn btn-success"
                                        onClick={submitForm}
                                        disabled={!formReady || (formReady && submitting)}>Create Identifier</button>&nbsp;
                                <a href={Routes.BANDITS.SHOW(bandit.uuid)} className="btn btn-dark">Back</a>
                            </div>
                        </div>
                    </div>

                    <div className="col-md-3">
                        <div className="alert alert-info">
                            <h3>Help</h3>
                            <p>
                                Identifiers are attributes that describe a bandit. For example, you could use identifiers
                                to describe a bandit that probes for two specific SSIDs and is always detected using a weak
                                signal strength.
                            </p>
                        </div>
                    </div>
                </div>
            </div>
    )
  
}

export default CreateIdentifierPage
