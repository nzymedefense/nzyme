import React, {useCallback, useState, useEffect} from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";

const STARS = "****************************************************************";

function TapSecret(props) {

    const [secret, setSecret] = useState(STARS);
    const [buttonText, setButtonText] = useState("Show Secret");
    const [toggled, setToggled] = useState(false);

    const toggle = useCallback(() => {
        if (toggled) {
            setSecret(STARS);
            setToggled(false);
            setButtonText("Show Secret");
        } else {
            setSecret(props.secret);
            setToggled(true);
            setButtonText("Hide Secret");
        }
    }, [setSecret, toggled, setToggled, props.secret]);

    useEffect(() => {
        if (toggled) {
            setSecret(props.secret);
        }
    }, [props.secret]);

    if (!props.secret) {
        return <LoadingSpinner />
    }

    return (
        <div className="row">
            <div className="col-md-12">
                <p className="help-text">
                    Every nzyme tap is using this secret to authenticate against the nzyme leader. If you cycle the
                    secret, you have to update it in the configuration of each of your nzyme taps. A secret is
                    automatically generated when nzyme starts up for the first time and does not find an existing
                    secret in the database.
                </p>

                <div className="tap-secret">
                    <form className="form-floating">
                        <input type="text" className="form-control" placeholder={secret} value={secret} readOnly />
                        <label htmlFor="floatingInputInvalid">Secret
                        </label>
                    </form>

                    <div className="mt-2">
                    <button className="btn btn-sm btn-primary" onClick={toggle}>{buttonText}</button>&nbsp;
                    <button className="btn btn-sm btn-danger" onClick={props.onCycle}>Cycle Secret</button>
                    </div>
                </div>
            </div>
        </div>
    )

}

export default TapSecret;