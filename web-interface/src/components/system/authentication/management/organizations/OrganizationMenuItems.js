import ApiRoutes from "../../../../../util/ApiRoutes";

export const ORGANIZATION_MENU_ITEMS = (organizationId) => [
  {name: "Overview", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organizationId) },
  {name: "Tenants", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(organizationId) },
  {name: "Administrators", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS_PAGE(organizationId) },
  {name: "Events & Actions", href: "" },
  {name: "Database", href: "" },
]