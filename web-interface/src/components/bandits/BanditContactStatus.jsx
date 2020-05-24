import React from 'react';

class BanditContactStatus extends React.Component {

    render() {
        const bandit = this.props.bandit;

        if (bandit.is_active) {
            return <span className="badge badge-warning">active</span>;
        } else {
            return <span className='badge badge-primary'>not active</span>;
        }
    }

}

export default BanditContactStatus;