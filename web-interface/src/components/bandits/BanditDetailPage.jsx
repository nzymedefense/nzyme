import React from 'react';
import Reflux from 'reflux';
import BanditsActions from "../../actions/BanditsActions";
import BanditsStore from "../../stores/BanditsStore";
import LoadingSpinner from "../misc/LoadingSpinner";

class BanditDetailPage extends Reflux.Component {

    constructor(props) {
        super(props);

        this.banditId = decodeURIComponent(props.match.params.id);

        this.store = BanditsStore;

        this.state = {
            bandit: undefined
        }
    }

    componentDidMount() {
        BanditsActions.findOne(this.banditId);
    }

    render() {
        if (!this.state.bandit) {
            return <LoadingSpinner />
        }

        const bandit = this.state.bandit;

        return (
            <div>
                <div className="row">
                    <div className="col-md-12">
                        <h1>Bandit <em>{bandit.name}</em></h1>
                    </div>
                </div>
            </div>
        )
    }

}

export default BanditDetailPage;