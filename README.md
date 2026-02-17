# SpaceStacker_OldCode

Legacy SpaceStacker plugin with modernization updates for current toolchains.

## Highlights

- Java upgraded to **17**
- API targets updated to **Spigot/Paper 1.20.4**
- Unresolvable SkyBlock dependency removed
- Added automated CI build (`.github/workflows/ci.yml`)
- Added initial unit tests (JUnit 5)

## Build

Requirements:

- Java 17
- Maven 3.8+

```bash
git clone git@github.com:Joeyoey1/SpaceStacker_OldCode.git
cd SpaceStacker_OldCode
mvn clean package
```

Output JAR is produced in `target/`.

## Commands

- `/spacespawners give <player> <entitytype> <material> [amount]`
- `/spacespawners reload`

## Notes

- Runtime dependencies are mostly `provided`; install required server plugins (e.g., Vault, HolographicDisplays) on your server.
- This project is still legacy-oriented, but now includes CI/testing foundations for safer future refactors.
