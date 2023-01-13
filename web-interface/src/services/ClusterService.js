import RESTClient from '../util/RESTClient'

class ClusterService {

  findAllNodes(setNodes) {
    RESTClient.get('/system/cluster/nodes', {}, function (response) {
      setNodes(response.data.nodes)
    })
  }

}

export default ClusterService
