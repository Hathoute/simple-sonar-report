# SonarQube Analysis

---

### Project [ [(${project_name})] ]([(${project_dashboard_url})])

### Gate Status: <span style='color:[(${gate_status_color})]'>[(${gate_status})]</span>

### Metrics

[# th:each="metric : ${metrics}" ]

- [(${metric.name})] : **[(${metric.value})]**
  [/]
