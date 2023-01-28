import RESTClient from '../util/RESTClient'

class ClusterService {

  findAllNodes(setNodes) {
    RESTClient.get('/system/cluster/nodes', {}, function (response) {
      setNodes(response.data.nodes)
    })
  }

  findNode(nodeUuid, setNode) {
    RESTClient.get('/system/cluster/nodes/show/' + nodeUuid, {}, function (response) {
      setNode(response.data)
    })
  }

}

export default ClusterService
