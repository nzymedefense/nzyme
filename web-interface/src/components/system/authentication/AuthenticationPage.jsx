import React, {useCallback, useEffect, useState} from 'react';
import TapSecret from "./TapSecret";
import TapsService from "../../../services/TapsService";

const tapsService = new TapsService();

function AuthenticationPage() {

    const [tapSecret, setTapSecret] = useState(null);

    useEffect(() => {
        tapsService.getTapSecret(setTapSecret);
    }, [setTapSecret]);

    const onCycle = useCallback(() => {
        if (!confirm("Do you really want to cycle the tap secret? You will have to change it in the config of all nzyme taps.")) {
            return;
        }

        tapsService.cycleTapSecret(setTapSecret);
    }, [setTapSecret]);

    return (
        <div>
            <div className="row">
                <div className="col-md-12">
                    <h1>Authentication</h1>
                </div>
            </div>

            <div className="row mt-3">
                <div className="col-md-12">
                    <div className="card">
                        <div className="card-body">
                            <h3>Tap Secret</h3>
                            <TapSecret secret={tapSecret} onCycle={onCycle} />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );

}

export default AuthenticationPage;
