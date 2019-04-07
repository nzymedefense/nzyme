import React from 'react';
import Reflux from 'reflux';

class NavigationLink extends Reflux.Component {

    render() {
        let className = "btn btn-dark";

        if ((window.location.pathname === "/" && this.props.href === "/") || (window.location.pathname !== "/" && this.props.href.startsWith(window.location.pathname))) {
            className += " active";
        }

        return (
            <a href={this.props.href} className={className}>{this.props.title}</a>
        )
    }

}

export default NavigationLink;