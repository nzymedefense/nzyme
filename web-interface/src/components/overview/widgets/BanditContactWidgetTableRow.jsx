import React from 'react';
import moment from "moment";
import Timespan from "../../misc/Timespan";
import Routes from "../../../util/Routes";

class BanditContactWidgetTableRow extends React.Component {

    render() {
        const contact = this.props.contact;

        return (
            <tr>
                <td>{contact.bandit_name}</td>
                <td>{contact.is_active ? <span className="badge badge-success">active</span> : <span className='badge badge-primary'>not active</span>}</td>
                <td>
                    <Timespan from={contact.first_seen} to={contact.last_seen} />
                </td>
                <td title={moment(contact.first_seen).format()}>
                    {moment(contact.first_seen).format("M/D/YY hh:mm a")}
                </td>
                <td title={moment(contact.last_seen).format()}>
                    {moment(contact.last_seen).format("M/D/YY hh:mm a")}
                </td>
                <td>
                    <a href={Routes.BANDITS.SHOW(contact.bandit_uuid)}>Details</a>
                </td>
            </tr>
        );
    }

}

export default BanditContactWidgetTableRow;