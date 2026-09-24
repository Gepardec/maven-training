---
theme: ./
title: Gepardec Theme — Example Deck
info: |
  Example deck demonstrating the Gepardec Slidev theme.
drawings:
  persist: false
layout: cover
---

# Java Enterprise Modernization

## Quarkus & Jakarta EE

Oliver Tod

March 2026

---
layout: agenda
---

# Agenda

- Why modernize now
- Our approach
- The migration path
- Tech stack comparison
- Effort vs. risk
- Implementation and next steps

---
layout: section
---

# Part 1

## Why modernize now?

---
layout: default
---

# Why modernize now?

- NIS2 and DORA compliance pressure is real and dated
- Java EE 7 and JBoss EAP 6 are out of vendor support
- Dependency drift turns every release into a risk event
- Modernization unlocks **velocity**, not just compliance

---
layout: default
---

# Our approach

- Gradual modernization — step-by-step with a clear target architecture
- **Bridge, not endpoint** — JSF stays while Quarkus comes in
- Security integrated, not bolted on
- Continuity through stable Austrian teams

---
layout: default
---

# The migration path

An ordered list keeps its numbers, tinted in Gepardec yellow:

1. Inventory the estate — modules, dependencies, CVEs
2. Establish a target architecture and a strangler boundary
3. Migrate leaf services to Quarkus behind the existing JSF shell
4. Automate dependency updates and wire up CI/CD with rollback
5. Decommission the legacy monolith once traffic has moved

---
layout: quadrants
---

# Modernization pillars

::one::

### Architecture
Strangler boundary around the JSF shell, Quarkus services behind it.
No big-bang cutover — every step ships on its own.

::two::

### Security
Keycloak-backed auth, integrated from the first service instead of
bolted on once the estate is already live.

::three::

### Delivery
CI/CD with automated rollback, Renovate keeping the dependency
tree current so releases stop being risk events.

::four::

### Operations
OpenShift as the target platform, with the observability stack
in place before the first workload moves.

---
layout: two-cols-header
---

# Tech stack comparison

::left::

### Legacy
- Java EE 7
- JBoss EAP 6
- Manual dependency updates
- Monolithic deploys

::right::

### Target
- Jakarta EE 10 + Quarkus
- OpenShift / Kubernetes
- Automated via Renovate
- CI/CD with rollback

---
layout: default
---

# Effort vs. risk at a glance

| Workload            | Effort | Risk   | Priority |
|---------------------|--------|--------|----------|
| Auth & session      | Medium | High   | First    |
| Reporting batch     | Low    | Low    | Later    |
| Order processing    | High   | High   | Phased   |

> Migrate high-risk, high-traffic paths first — that's where support
> gaps hurt most.

---
layout: section
variant: ascii
---

# Part 2

## Implementation

---
layout: default
---

# Code example — CDI events

Line-highlight markers focus the audience on what matters:

```java {4-5,10}
@ApplicationScoped
public class OrderService {

  @Inject
  Event<OrderPlaced> orderEvents;

  @Transactional
  public void placeOrder(Order order) {
    repository.save(order);
    orderEvents.fire(new OrderPlaced(order.id()));
  }
}
```

Use `@ObservesAsync` on the consumer for true async handling.

---
layout: section
---

# Part 3

## How the migration actually went

---
layout: conversation
session: OrderService migration · 8 Sep
---

# Getting the build green

::turns::

<ChatTurn role="user">

The `order-service` module still won't build after the Jakarta switch. Sort it out — I need a green `mvn verify` before standup.

</ChatTurn>

<ChatTurn role="agent" v-click>

Reading the reactor first. `order-service` declares the `jakarta.*` BOM in its POM, but three classes still import `javax.persistence`, so the entity manager never resolves at compile time.

</ChatTurn>

<ChatTurn role="tool" meta="grep" v-click>

```
$ grep -rl "javax\.persistence" order-service/src/main/java
OrderEntity.java
OrderLineEntity.java
OrderRepository.java
```

</ChatTurn>

<ChatTurn role="agent" v-click>

Two ways forward. Hand-edit the three files, or wire the Eclipse Transformer into the build so the other fourteen modules get the same treatment when their turn comes. The transformer costs more today and less every time after.

</ChatTurn>

<ChatTurn role="user" v-click>

Transformer. Don't hand-edit — I am not doing this fifteen more times.

</ChatTurn>

<ChatTurn role="agent" v-click>

Added `transformer-maven-plugin` to the `process-sources` phase, rewriting `javax.*` to `jakarta.*`. Compilation passes now. The build stops in the test phase instead.

</ChatTurn>

<ChatTurn role="tool" meta="mvn -q verify" v-click>

```
OrderRepositoryTest » PersistenceException:
  Unknown schema version [2.2] for persistence.xml
BUILD FAILURE — Tests run: 41, Failures: 0, Errors: 12
```

</ChatTurn>

<ChatTurn role="agent" v-click>

`persistence.xml` is still on the 2.2 schema. The transformer rewrites Java sources, not resources, so the descriptor has to move to 3.0 separately — namespace, `xsi:schemaLocation` and the `version` attribute.

</ChatTurn>

<ChatTurn role="user" v-click>

That is the one I forget every single time.

</ChatTurn>

<ChatTurn role="agent" v-click>

Bumped the descriptor to the Jakarta Persistence 3.0 namespace and schema. Re-running the full verify.

</ChatTurn>

<ChatTurn role="tool" meta="mvn -q verify" v-click>

```
BUILD SUCCESS
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
```

</ChatTurn>

<ChatTurn role="agent" v-click>

Green. One thing left that is yours to decide: `CdiProducers` still imports `javax.annotation.PostConstruct`. The transformer skipped it because it comes in on `provided` scope. It compiles today. It will not survive the next JDK bump.

</ChatTurn>

---
layout: statement
---

# We only recommend **what we can technically justify.**

---
layout: contact
name: Günter Pirklbauer
role: CEO
email: guenter.pirklbauer@gepardec.com
phone: +43 664 1167 681
---
