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

server <- function(input, output) {
  output$plot <-  renderPlotly({
    plot_ly(df[df$date >= input$year, ], x = ~date, y = ~amount, color = ~type) %>% add_lines() %>%
      layout(margin=list(b=100), xaxis = list(title = ""), yaxis = list(title = "Monthly Amount (USD)"))   
  })
}

ui <- shinyUI(fluidPage(
  sidebarLayout(
    sidebarPanel(
      sliderInput("year", "Start Year:", min = 2009, max = 2015, value = 2012)
    ),
    mainPanel(plotlyOutput("plot"))
  )
))

shinyApp(ui = ui, server = server)
