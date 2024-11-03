---
name: Meta Release Ticket
about: A meta ticket that holds all information related to an nzyme release.
title: 'Version [version] Meta Release Ticket'
labels: ''
assignees: ''

---
## Version [version] Meta Release Ticket

* *Requires all nodes to be shut down during upgrade*: 🟢 No / ⚠️ Yes (Update `nzyme-node` upgrade procedure below)

### Breaking Changes

*

### New Optional Configuration Parameters

*

### Upgrade Procedure

#### `nzyme-node`

If you are upgrading from a previous version, you can simply run `dpkg -i` on the new release 
package and restart the `nzyme-node` service. Always upgrade `nzyme-node` before `nzyme-tap` 
unless instructed otherwise.

#### `nzyme-tap`

If you are upgrading from a previous version, you can simply run `dpkg -i` on the new release
package and restart the `nzyme-tap` service. Always upgrade `nzyme-node` before `nzyme-tap` 
unless instructed otherwise.
