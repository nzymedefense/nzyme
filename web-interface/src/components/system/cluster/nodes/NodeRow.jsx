import React from "react";

import numeral from "numeral";

function NodeRow(props) {

  const node = props.node

  return (
      <tr>
        <td><a href="#">{node.name}</a></td>
        <td>{numeral(node.cpu_system_load).format("0.0")}%</td>
        <td>
          {numeral(node.memory_bytes_used).format("0.0b")} of {numeral(node.memory_bytes_total).format("0.0b")}{' '}
          ({numeral((node.memory_bytes_used*100.0)/node.memory_bytes_total).format("0.0")}%)
        </td>        <td>
          {numeral(node.heap_bytes_used).format("0.0b")} of {numeral(node.heap_bytes_total).format("0.0b")}{' '}
          ({numeral((node.heap_bytes_used*100.0)/node.heap_bytes_total).format("0.0")}%)
        </td>
        <td>{numeral(node.process_virtual_size).format("0.0b")}</td>
        <td>{node.version}</td>
      </tr>
  )

}

export default NodeRow;