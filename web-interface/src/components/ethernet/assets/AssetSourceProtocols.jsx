export default function AssetSourceProtocols(props) {

  const asset = props.asset;

  const protocols = [];

  if (asset.seen_dhcp) {
    protocols.push("DHCP");
  }

  if (asset.seen_tcp) {
    protocols.push("TCP");
  }

  return protocols.join(", ")

}