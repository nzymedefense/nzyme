import React from 'react';
import Reflux from 'reflux';
import ContactsTableRow from "./ContactsTableRow";

class ContactsTable extends Reflux.Component {

    render() {
        const bandit = this.props.bandit;
        const contacts = bandit.contacts;

        if (!contacts || contacts.length === 0) {
            return (
                <div className="alert alert-warning">
                    This bandit has had no contacts yet.
                </div>
            );
        }

        return (
            <div className="row">
                <div className="col-md-12">
                    <table className="table table-sm table-hover table-striped">
                        <thead>
                        <tr>
                            <th>Track</th>
                            <th>By</th>
                            <th>Active</th>
                            <th>Frame Count</th>
                            <th>Duration</th>
                            <th>First Seen</th>
                            <th>Last Seen</th>
                        </tr>
                        </thead>
                        <tbody>
                        {Object.keys(contacts).map(function (key,i) {
                            return <ContactsTableRow key={contacts[key].uuid} contact={contacts[key]}/>
                        })}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default ContactsTable;