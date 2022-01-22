import React from 'react'
import numeral from 'numeral'
import moment from 'moment'
import Timespan from '../misc/Timespan'
import RSSI from '../misc/RSSI'
import Routes from '../../util/ApiRoutes'

class ContactsTableRow extends React.Component {
  render () {
    const contact = this.props.contact

    return (
            <tr>
                <td>
                    {
                        contact.source_role === 'LEADER'
                          ? <a href={Routes.BANDITS.CONTACT_DETAILS(contact.bandit_uuid, contact.uuid)}>{contact.uuid.substr(0, 8)}</a>
                          : <span title="Only LEADER contacts produce track details.">{contact.uuid.substr(0, 8)}</span>
                    }
                </td>
                <td>{contact.source_name} ({contact.source_role})</td>
                <td>{contact.is_active ? <span className="badge badge-success">active</span> : <span className='badge badge-primary'>not active</span>}</td>
                <td>{contact.is_active ? <RSSI rssi={contact.last_signal} /> : 'n/a'}</td>
                <td>{numeral(contact.frame_count).format('0,0')}</td>
                <td>
                    <Timespan from={contact.first_seen} to={contact.last_seen} />
                </td>
                <td>{contact.ssids.length}</td>
                <td title={moment(contact.first_seen).format()}>
                    {moment(contact.first_seen).format('M/D/YY hh:mm a')}
                </td>
                <td title={moment(contact.last_seen).format()}>
                    {moment(contact.last_seen).format('M/D/YY hh:mm a')}
                </td>
            </tr>
    )
  }
}

export default ContactsTableRow
