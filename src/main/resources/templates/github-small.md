# SonarQube Analysis

---

## Project [ [(${project_name})] ]([(${project_dashboard_url})])

### Gate Status: ${\textsf{\color{[(${gate_status_color})]}[(${gate_status})]}}$

### Metrics

[# th:each="metric : ${metrics}" ]

- **[(${metric.name})]** :[# th:if="${metric.newValue.isPresent}"] New Code **[(${metric.newValue.get})]**, Overall[/]
  **[(${metric.value})]**
  [/]
