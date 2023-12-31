# SonarQube Analysis

---

## Project [ [(${project_name})] ]([(${project_dashboard_url})])

### Gate Status:  [(${gate_status_bool} ? '🟩' : '🟥')] [(${gate_status})] [(${gate_status_bool} ? '🟩' : '🟥')]

### Metrics

[# th:each="metric : ${metrics}" ]

- **[(${metric.name})]** :[# th:if="${metric.newValue.isPresent}"] New Code **[(${metric.newValue.get})]**, Overall[/] **[(${metric.value} ? ${metric.value} : 'N/A')]** [/]
