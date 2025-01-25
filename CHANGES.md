# [Version 2.1.0 to 2.2.0](./docs/changes/2.2.0.md)

# [Version 2.2.0 to 2.3.0](./docs/changes/2.3.0.md)

# [Version 2.3.0 to 2.4.0](./docs/changes/2.4.0.md)

# [Version 2.4.0 to 2.5.0](./docs/changes/2.5.0.md)

# [Version 2.5.0 to 2.5.1](./docs/changes/2.5.1.md)

# [Version 2.5.1 to 2.6.0](./docs/changes/2.6.0.md)

# [Version 2.6.0 to 2.7.0](./docs/changes/2.7.0.md)

# [Version 2.7.0 to 2.8.0](./docs/changes/2.8.0.md)

# [Version 2.8.0 to 2.9.0](./docs/changes/2.9.0.md)

# [Version 2.9.0 to 2.9.1](./docs/changes/2.9.1.md)

# [Version 2.9.1 to 2.9.2](./docs/changes/2.9.2.md)

# [Version 2.9.2 to 2.10.0](./docs/changes/2.10.0.md)

# [Version 2.10.0 to 2.11.0](./docs/changes/2.11.0.md)

# [Version 2.11.0 to 2.12.0](./docs/changes/2.12.0.md)

# [Version 2.12.0 to 2.12.1](./docs/changes/2.12.1.md)

# [Version 2.12.1 to 2.13.0](./docs/changes/2.13.0.md)

# [Version 2.13.0 to 2.13.1](./docs/changes/2.13.1.md)

# [Version 2.13.1 to 2.13.2](./docs/changes/2.13.2.md)

# [Version 2.13.2 to 2.14.0](./docs/changes/2.14.0.md)

# Planned for next version

## Bug Fixes

* [GH-618](https://github.com/apache/mina-sshd/issues/618) Fix reading an `OpenSshCertificate` from a `Buffer`
* [GH-626](https://github.com/apache/mina-sshd/issues/626) Enable `Streaming.Async` for `ChannelDirectTcpip`
* [GH-628](https://github.com/apache/mina-sshd/issues/628) SFTP: fix reading directories with trailing blanks in the name
* [GH-636](https://github.com/apache/mina-sshd/issues/636) Fix handling of unsupported key types in `known_hosts` file
* [GH-642](https://github.com/apache/mina-sshd/issues/642) Do not use `SecureRandom.getInstanceString()` due to possible entropy starvation (regression in 2.14.0)
* [GH-654](https://github.com/apache/mina-sshd/issues/654) Fix test dependency for assertj in `sshd-contrib`

## New Features

* [GH-606](https://github.com/apache/mina-sshd/issues/606) Support ML-KEM PQC hybrid key exchanges
* [GH-652](https://github.com/apache/mina-sshd/issues/652) New method `KnownHostsServerKeyVerifier.handleRevokedKey()`

* [SSHD-988](https://issues.apache.org/jira/projects/SSHD/issues/SSHD-988) Support ed25519 keys via the Bouncy Castle library

## Potential compatibility issues

## Major Code Re-factoring

