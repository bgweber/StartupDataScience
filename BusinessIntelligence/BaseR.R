library(bigrquery)
library(plotly)
project <- "your_project_id"

sql <- "SELECT  
  substr(cast(pickup_datetime as String), 1, 7) as date
  ,payment_type as type 
  ,sum(total_amount) as amount
FROM `nyc-tlc.yellow.trips`
group by 1, 2"

df <- query_exec(sql, project = project, use_legacy_sql = FALSE)
plot_ly(df, x = ~date, y = ~amount, color = ~type) %>% add_lines() %>%
  layout(margin=list(b=100), xaxis = list(title = ""), yaxis = list(title = "Monthly Amount (USD)"))

total <- aggregate(df$Amount, by=list(Category=df$Date), FUN=sum)
plot_ly(total, x = ~Category, y = ~x) %>% add_lines() %>%
  layout(margin=list(b=100), xaxis = list(title = ""), yaxis = list(title = "Monthly Total (USD)"))
