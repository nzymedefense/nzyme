import React from "react";

function ThreatLevelMidnight(props) {

    if (!props.enabled) {
        return null;
    }

    return (
        <div className="alert bg-threatlevelmidnight">
            <div className="text-overlay">
                Changing this configuration value can lead to potentially irreversible consequences. Please
                make sure you have read and understood the{' '}
                <a href={props.help_tag ? "https://go.nzyme.org/" + props.help_tag : "https://go.nzyme.org/documentation"}
                   target="_blank" >
                    documentation
                </a> before continuing.

                <div className="form-check form-switch">
                    <input className="form-check-input" type="checkbox" id={"danger-switch-" + props.configKey}
                           checked={props.changeWarningAck}
                           onChange={(e) => props.setChangeWarningAck(e.target.checked) }
                    />
                    <label className="form-check-label" htmlFor={"danger-switch-" + props.configKey}>
                        I understand and want to continue
                    </label>
                </div>
            </div>
        </div>
    )

}

export default ThreatLevelMidnight;