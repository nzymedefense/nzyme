import React, {useEffect, useState} from "react";
import RetroService from "../../services/RetroService";
import LoadingSpinner from "../misc/LoadingSpinner";

const retroService = new RetroService();

function ServiceSummary() {

    const [summary, setSummary] = useState(null);

    useEffect(() => {
        retroService.getServiceSummary(setSummary);
    }, [setSummary]);

    if (!summary) {
        return <LoadingSpinner />;
    }

    return (
        <span>summary</span>
    )

}

export default ServiceSummary;