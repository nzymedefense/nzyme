import React from 'react';

class TrackerStatusCard extends React.Component {

    render() {
        let bg;
        let statusText;
        switch(this.props.status) {
            case "ONLINE":
                bg = "bg-success";
                statusText = "Online";
                break;
            case "DARK":
                bg = "bg-danger";
                statusText = "Dark";
                break;
            default:
                bg = "bg-info";
                statusText = "Unknown (" + this.props.status + ")";
        }

        return (
            <div className={"card text-white " + bg}>
                <div className="card-body text-center">
                    <h3 className="card-title">Tracker Status</h3>
                    <p className="card-text text-center">
                        <h2>
                            {statusText}
                        </h2>
                    </p>
                </div>
            </div>
        )
    }

}

export default TrackerStatusCard;