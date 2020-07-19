import React from 'react';
import Reflux from 'reflux';

class ActiveContactsWidget extends Reflux.Component {

    render() {
        return (
            <div className={"card text-center overview-card " + (this.props.activeContacts > 0 ? "bg-danger" : "bg-success") }>
                <div className="card-body">
                    <p>Active Contacts</p>
                    <span>{this.props.activeContacts}</span>
                </div>
            </div>
        );
    }

}

export default ActiveContactsWidget;


