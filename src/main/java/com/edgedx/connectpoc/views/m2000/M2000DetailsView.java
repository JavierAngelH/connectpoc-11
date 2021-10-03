package com.edgedx.connectpoc.views.m2000;

import com.edgedx.connectpoc.entity.AbbottM2000;
import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.model.ChartResults;
import com.edgedx.connectpoc.repository.AbbottM2000Repository;
import com.edgedx.connectpoc.repository.CustomRepository;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.dashboard.DashboardView;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.chart.zoom.ZoomType;
import com.github.appreciated.apexcharts.config.fill.builder.GradientBuilder;
import com.github.appreciated.apexcharts.config.legend.HorizontalAlign;
import com.github.appreciated.apexcharts.config.stroke.Curve;
import com.github.appreciated.apexcharts.config.subtitle.Align;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.card.content.IconItem;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_M2000_DETAIL;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_M2000_DETAIL;

@Route(value = PAGE_M2000_DETAIL, layout = MainView.class)
@PageTitle(TITLE_M2000_DETAIL)
@Secured({"ROLE_USER", "ROLE_SYSTEM", "ROLE_VIEWER"})
public class M2000DetailsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomRepository customRepository;

    private final AbbottM2000Repository abbottM2000Repository;

    Label chartCaption = new Label();

    H3 title = new H3();

    Device device = new Device();

    ApexCharts chart;
    Integer lastWeekTests = 0;
    Integer lastWeekErrors = 0;
    Integer totalTests = 0;
    Integer totalErrors = 0;

    public M2000DetailsView(CustomRepository customRepository, AbbottM2000Repository abbottM2000Repository){
        this.customRepository = customRepository;
        this.abbottM2000Repository = abbottM2000Repository;
        Device deviceParam = ComponentUtil.getData(UI.getCurrent(), Device.class);
        if (deviceParam != null) {
            title.setText(deviceParam.getDeviceType().getDescription() + ": " + deviceParam.getSerialNumber());
            device = deviceParam;
            buildLayout();
        } else {
            this.getUI().ifPresent(ui ->
                    ui.navigate(DashboardView.class));
        }
    }

    public void buildLayout() {
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        title.getElement().getStyle().set("text-align", "center");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        Label labelLastEvent = new Label("Last event: " + abbottM2000Repository.findTopByOrderByTestDatetimeDesc().get().getTestDatetime().format(DateTimeFormatter.ISO_DATE));
        labelLastEvent.getStyle().set("font-weight", "bold");
        add(labelLastEvent);

        FormLayout layoutCards = new FormLayout();
        layoutCards.setWidthFull();

        Optional<AbbottM2000> mostRecentResult = abbottM2000Repository.findTopByOrderByTestDatetimeDesc();

        if (mostRecentResult.isPresent()) {
            LocalDateTime endTime = mostRecentResult.get().getTestDatetime();
            LocalDate startDate = endTime.toLocalDate().minusDays(7);
            LocalDateTime start = LocalDateTime.of(startDate, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX);
            lastWeekTests = abbottM2000Repository.countByTestDatetimeIsAfterAndTestDatetimeIsBefore(start, end);
            lastWeekErrors = abbottM2000Repository.countByTestDatetimeIsAfterAndTestDatetimeIsBeforeAndTestErrorIsNotNullAndTestErrorNotIn(start, end, new ArrayList<String>(
                    Arrays.asList("")));
            totalTests = Math.toIntExact(abbottM2000Repository.count());
            totalErrors = abbottM2000Repository.countByTestErrorIsNotNullAndTestErrorIsNotNullAndTestErrorNotIn(new ArrayList<String>(
                    Arrays.asList("")));
        }

        RippleClickableCard lastWeekTestsCard = buildLastWeekTestsCard(lastWeekTests);
        RippleClickableCard lastWeekErrorsCard = buildLastWeekErrorsCard(lastWeekErrors);
        RippleClickableCard totalTestsCard = buildAllTimeTestsCard(totalTests);
        RippleClickableCard totalErrorsCard = buildAllTimeErrorsCard(totalErrors);

        layoutCards.add(lastWeekTestsCard, lastWeekErrorsCard, totalTestsCard, totalErrorsCard);
        layoutCards.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("900px", 4, FormLayout.ResponsiveStep.LabelsPosition.TOP));
        add(layoutCards);

        chartCaption.setText("Last 7 days Tests: " + lastWeekTests);
        chartCaption.setWidth("100%");
        chartCaption.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        chartCaption.getElement().getStyle().set("text-align", "center");
        chartCaption.getStyle().set("font-weight", "bold");

        add(chartCaption);

        chart = buildRecentTestsGraph(device.getSerialNumber());

        add(chart);
    }

    public RippleClickableCard buildLastWeekTestsCard(Integer value) {
        Icon icon = VaadinIcon.CLIPBOARD_CHECK.create();
        icon.setColor("hsl(214deg 90% 52%)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Last 7 days Tests");
        item.setAlignItems(Alignment.CENTER);
        item.setJustifyContentMode(JustifyContentMode.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chart);
                    chart = buildRecentTestsGraph(device.getSerialNumber());
                    add(chart);
                    chartCaption.setText("Last 7 days tests: " + value);
                    chartCaption.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");

                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildLastWeekErrorsCard(Integer value) {
        Icon icon = VaadinIcon.FILE_REMOVE.create();
        icon.setColor("#c7140e");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Last 7 days Errors");
        item.setAlignItems(Alignment.CENTER);
        item.setJustifyContentMode(JustifyContentMode.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chart);
                    chart = buildRecentErrorsGraph(device.getSerialNumber());
                    add(chart);
                    chartCaption.setText("Last 7 days errors: " + value);
                    chartCaption.getElement().getStyle().set("color", "#c7140e");
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildAllTimeTestsCard(Integer value) {
        Icon icon = VaadinIcon.CLIPBOARD_CHECK.create();
        icon.setColor("hsl(214deg 90% 52%)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Total Tests");
        item.setAlignItems(Alignment.CENTER);
        item.setJustifyContentMode(JustifyContentMode.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chart);
                    chart = buildAllTestsGraph(device.getSerialNumber());
                    add(chart);
                    chartCaption.setText("Total tests: " + value);
                    chartCaption.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildAllTimeErrorsCard(Integer value) {
        Icon icon = VaadinIcon.FILE_REMOVE.create();
        icon.setColor("#c7140e");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Total Errors");
        item.setAlignItems(Alignment.CENTER);
        item.setJustifyContentMode(JustifyContentMode.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chart);
                    chart = buildAllErrorsGraph(device.getSerialNumber());
                    add(chart);
                    chartCaption.setText("Total errors: " + value);
                    chartCaption.getElement().getStyle().set("color", "#c7140e");
                }, item);
        card.setWidth("100%");
        return card;
    }

    private Series<Coordinate> getSeries(List<ChartResults> list, String seriesName) {
        Series<Coordinate> series = new Series<>();
        List<Coordinate> coordinates = new ArrayList<>();
        for (ChartResults result : list) {
            coordinates.add(new Coordinate(getISOString(result.getDate()), result.getTests()));
        }
        Coordinate[] array = coordinates.toArray(new Coordinate[list.size()]);
        series.setData(array);
        series.setName(seriesName);
        return series;
    }

    ApexCharts buildRecentTestsGraph(String deviceName) {
        List<ChartResults> list = customRepository.getM2000TestsByDateLast7days(deviceName);
        ApexCharts areaChart = ApexChartsBuilder.get()
                .withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(getSeries(list, "Tests"))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(false).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        areaChart.setWidth("100%");

        return areaChart;
    }

    ApexCharts buildRecentErrorsGraph(String deviceName) {
        List<ChartResults> list = customRepository.getM2000ErrorsByDateLast7days(deviceName);
        ApexCharts areaChart = ApexChartsBuilder.get()
                .withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).withColors("#c7140e").build())
                .withFill(FillBuilder.get().withGradient(GradientBuilder.get().withGradientToColors("#c7140e").build()).build())
                .withSeries(getSeries(list, "Errors"))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(false).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        areaChart.setWidth("100%");

        return areaChart;
    }

    ApexCharts buildAllTestsGraph(String deviceName) {
        List<ChartResults> list = customRepository.getM2000TestsByDate(deviceName);
        ApexCharts areaChart = ApexChartsBuilder.get()
                .withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(getSeries(list, "Tests"))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(false).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        areaChart.setWidth("100%");

        return areaChart;
    }

    ApexCharts buildAllErrorsGraph(String deviceName) {
        List<ChartResults> list = customRepository.getM2000ErrorsByDate(deviceName);
        ApexCharts areaChart = ApexChartsBuilder.get()
                .withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).withColors("#c7140e").build())
                .withFill(FillBuilder.get().withGradient(GradientBuilder.get().withGradientToColors("#c7140e").build()).build())
                .withSeries(getSeries(list, "Errors"))
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime).build())
                .withYaxis(YAxisBuilder.get()
                        .withOpposite(false).build())
                .withTitle(TitleSubtitleBuilder.get()
                        .withAlign(Align.left).build())
                .withLegend(LegendBuilder.get().withHorizontalAlign(HorizontalAlign.left).build())
                .build();
        areaChart.setWidth("100%");

        return areaChart;
    }

    private String getISOString(LocalDate l) {
        return l.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Device deviceParam = ComponentUtil.getData(UI.getCurrent(), Device.class);
        if (deviceParam == null) {
            beforeEnterEvent.forwardTo(DashboardView.class);
        }
    }

}
