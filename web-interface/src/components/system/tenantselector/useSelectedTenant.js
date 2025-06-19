import Store from "../../../util/Store";

export default function useSelectedTenant() {
  return [Store.get("selected_organization"), Store.get("selected_tenant")]
}