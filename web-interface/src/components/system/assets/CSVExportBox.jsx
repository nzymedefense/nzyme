import React from 'react';
import LoadingSpinner from "../../misc/LoadingSpinner";

class CSVExportBox extends React.Component {

    render() {
        return (
            <div>
                <div className="row">
                    <div className="col-md-6">
                        <h5>{this.props.title}</h5>
                    </div>

                    <div className="col-md-6 text-right">
                        <button className="btn btn-sm btn-info" onClick={() => {navigator.clipboard.writeText(this.props.content)}}>
                            Copy to Clipboard
                        </button>
                    </div>
                </div>

                <textarea style={{width: "100%", height: 250}} wrap="soft">{this.props.content}</textarea>
            </div>
        )
    }

}

export default CSVExportBox;