import React from 'react';
import SimpleBarChart from "../../charts/SimpleBarChart";

class DeauthFramesWidget extends React.Component {

    render() {
        return (
            <SimpleBarChart
                width={1100}
                height={150}
                customMarginRight={0}
                data={this.props.deauthFrameHistogram}/>
        );
    }

}

export default DeauthFramesWidget;



