import ApiRoutes from "../../../../../util/ApiRoutes";

export const ORGANIZATION_MENU_ITEMS = (organizationId) => [
  {name: "Overview", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DETAILS(organizationId) },
  {name: "Tenants", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.TENANTS_PAGE(organizationId) },
  {name: "Administrators", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.ADMINS_PAGE(organizationId) },
  {name: "Events & Actions", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.EVENTS_PAGE(organizationId) },
  {name: "Database", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.DATABASE_PAGE(organizationId) },
  {name: "Quotas", href: ApiRoutes.SYSTEM.AUTHENTICATION.MANAGEMENT.ORGANIZATIONS.QUOTAS_PAGE(organizationId) },
]