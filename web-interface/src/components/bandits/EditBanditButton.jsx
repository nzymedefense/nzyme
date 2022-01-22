import React, { useCallback }  from 'react';

import Routes from '../../util/ApiRoutes'

function EditBanditButton(props) {

    const editBandit = useCallback((e) => {
        if (props.bandit.read_only) {
            alert('Cannot edit a built-in bandit.');
            e.preventDefault();
          }
    }, [props.bandit]);

    return (
        <a href={Routes.BANDITS.EDIT(props.bandit.uuid)} className="btn btn-primary" onClick={editBandit}>Edit Bandit</a>
    );

}

export default EditBanditButton;