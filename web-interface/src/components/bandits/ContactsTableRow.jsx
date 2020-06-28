import React from 'react';
import Reflux from 'reflux';
import numeral from "numeral";
import moment from "moment";
import Timespan from "../misc/Timespan";
import RSSI from "../misc/RSSI";

class ContactsTableRow extends Reflux.Component {

    render() {
        const contact = this.props.contact;

        return (
            <tr>
                <td>{contact.uuid.substr(0, 8)}</td>
                <td>{contact.source_name} ({contact.source_role})</td>
                <td>{contact.is_active ? <span className="badge badge-success">active</span> : <span className='badge badge-primary'>not active</span>}</td>
                <td>{contact.is_active ? <RSSI rssi={contact.last_signal} /> : "n/a"}</td>
                <td>{numeral(contact.frame_count).format('0,0')}</td>
                <td>
                    <Timespan from={contact.first_seen} to={contact.last_seen} />
                </td>
                <td title={moment(contact.first_seen).format()}>
                    {moment(contact.first_seen).format("M/D/YY hh:mm a")}
                </td>
                <td title={moment(contact.last_seen).format()}>
                    {moment(contact.last_seen).format("M/D/YY hh:mm a")}
                </td>
            </tr>
        );
    }

}

export default ContactsTableRow;