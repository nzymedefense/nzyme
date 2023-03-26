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

  deleteNode(nodeUuid, successCallback) {
    RESTClient.delete('/system/cluster/nodes/show/' + nodeUuid, {}, successCallback)
  }

  findGaugeMetricHistogramOfNode(nodeId, metricName, setHistogram) {
    RESTClient.get('/system/cluster/nodes/show/' + nodeId + '/metrics/gauges/' + metricName + '/histogram', {}, function (response) {
      setHistogram(response.data)
    })
  }

  findNodesConfiguration(setNodesConfiguration) {
    RESTClient.get('/system/cluster/nodes/configuration', {}, function (response) {
      setNodesConfiguration(response.data)
    })
  }

  updateNodesConfiguration(newConfig, successCallback, errorCallback) {
    RESTClient.put('/system/cluster/nodes/configuration', { change: newConfig }, successCallback, errorCallback)
  }

  findMessageBusMessages(setMessages, limit, offset) {
    RESTClient.get('/system/cluster/messagebus/messages', {limit: limit, offset: offset}, function (response) {
      setMessages(response.data)
    })
  }

}

export default ClusterService
