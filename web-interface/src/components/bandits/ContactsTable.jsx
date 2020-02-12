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
                            <th>ID</th>
                            <th>Active</th>
                            <th>Frame Count</th>
                            <th>First Seen</th>
                            <th>Last Seen</th>
                        </tr>
                        </thead>
                        {Object.keys(contacts).map(function (key,i) {
                            return <ContactsTableRow contact={contacts[key]}/>
                        })}
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }

}

export default ContactsTable;