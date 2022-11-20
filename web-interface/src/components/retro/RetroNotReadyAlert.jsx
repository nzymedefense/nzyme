import React, {useEffect, useState} from "react";
import RetroService from "../../services/RetroService";
import ApiRoutes from "../../util/ApiRoutes";

const retroService = new RetroService();

function RetroNotReadyAlert() {

    const [summary, setSummary] = useState(null);

    useEffect(() => {
        retroService.getServiceSummary(setSummary);
    }, [setSummary]);

    if (!summary) {
        return null;
    } else {
        if (!summary.is_configured) {
            return (
                <div className="alert alert-danger">
                    Retrospective is not fully configured yet and will not run or store any data. Please set all
                    required <a href={ApiRoutes.RETRO.CONFIGURATION}>configuration variables</a> to continue.
                </div>
            )
        } else {
            return null;
        }
    }

}

export default RetroNotReadyAlert;