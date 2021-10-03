package com.edgedx.connectpoc.views.dashboard;

import com.edgedx.connectpoc.entity.Device;
import com.edgedx.connectpoc.entity.NodeConfiguration;
import com.edgedx.connectpoc.entity.NodeHealthStatistics;
import com.edgedx.connectpoc.model.HealthStatisticsChartResults;
import com.edgedx.connectpoc.repository.CustomRepository;
import com.edgedx.connectpoc.repository.DeviceRepository;
import com.edgedx.connectpoc.repository.NodeConfigurationRepository;
import com.edgedx.connectpoc.repository.NodeHealthStatisticsRepository;
import com.edgedx.connectpoc.views.MainView;
import com.edgedx.connectpoc.views.cartridges.CartridgeInventoryView;
import com.edgedx.connectpoc.views.device.DeviceDetailView;
import com.edgedx.connectpoc.views.genexpert.GeneXpertDetailsView;
import com.edgedx.connectpoc.views.m2000.M2000DetailsView;
import com.edgedx.connectpoc.views.nodeconfiguration.NodeConfigurationDetailView;
import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.chart.builder.ToolbarBuilder;
import com.github.appreciated.apexcharts.config.chart.builder.ZoomBuilder;
import com.github.appreciated.apexcharts.config.chart.zoom.ZoomType;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_DASHBOARD;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_DASHBOARD;


@Route(value = PAGE_DASHBOARD, layout = MainView.class)
@PageTitle(TITLE_DASHBOARD)
@Secured({"ROLE_USER", "ROLE_SYSTEM", "ROLE_VIEWER"})
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final DeviceRepository deviceRepository;

    private final NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    private final CustomRepository customRepository;

    private final NodeConfigurationRepository nodeConfigurationRepository;

    ApexCharts chart = new ApexCharts();

    Label chartCaption = new Label();


    public DashboardView(DeviceRepository deviceRepository, NodeHealthStatisticsRepository nodeHealthStatisticsRepository,
                         CustomRepository customRepository,NodeConfigurationRepository nodeConfigurationRepository) {
        setSizeFull();
        this.deviceRepository = deviceRepository;
        this.customRepository = customRepository;
        this.nodeConfigurationRepository = nodeConfigurationRepository;
        this.nodeHealthStatisticsRepository = nodeHealthStatisticsRepository;
        H3 title = new H3();
        title.setText("Connected Devices: " + deviceRepository.count());
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        Optional<NodeHealthStatistics> healthStats = nodeHealthStatisticsRepository.findTopByOrderByLogDateDesc();

        Integer signalStrength = 0;
        Integer battery = 0;

        if (healthStats.isPresent()) {
            if (healthStats.get().getNetworkSignalStrength() != null)
                signalStrength = healthStats.get().getNetworkSignalStrength();
            if (healthStats.get().getBatteryStatus() != null)
                battery = healthStats.get().getBatteryStatus();
        }

        FormLayout layoutCards = new FormLayout();
        layoutCards.setWidthFull();

        RippleClickableCard signalCard = buildNetworkSignalCard(signalStrength);

        RippleClickableCard batteryCard = buildBatteryCard(battery);

        RippleClickableCard inventoryCard = buildCartridgeInventoryCard();


        layoutCards.add(signalCard, batteryCard, inventoryCard);

        for (Device device : deviceRepository.findAll()) {
            RippleClickableCard deviceCard = buildDeviceCard(device);
            layoutCards.add(deviceCard);
        }

        layoutCards.add(buildNewDeviceCard());

        layoutCards.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("900px", 4, FormLayout.ResponsiveStep.LabelsPosition.TOP));
        add(layoutCards);

        chartCaption.setWidth("100%");
        chartCaption.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        chartCaption.getElement().getStyle().set("text-align", "center");
        chartCaption.getStyle().set("font-weight", "bold");


    }


    public RippleClickableCard buildNetworkSignalCard(Integer value) {
        String color = "#f5c542";
        if (value >= 3)
            color = "#10c437";
        else if (value <= 2)
            color = "#c7140e";

        Icon icon = VaadinIcon.SIGNAL.create();
        icon.setColor(color);
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Signal Strength");
        item.setAlignItems(Alignment.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chartCaption);
                    remove(chart);
                    chart = buildNetworkSignalChart();
                    chartCaption.setText("Signal Strength");
                    add(chartCaption);
                    add(chart);
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildCartridgeInventoryCard() {
        Icon icon = VaadinIcon.ARCHIVES.create();
        icon.setColor("rgb(89 91 94)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Cartridge Inventory");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    this.getUI().ifPresent(ui ->
                            ui.navigate(CartridgeInventoryView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildNewDeviceCard() {
        Icon icon = VaadinIcon.PLUS_CIRCLE.create();
        icon.setColor("rgb(3 88 21)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, "Add Device");
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    ComponentUtil.setData(UI.getCurrent(), Device.class, null);
                    this.getUI().ifPresent(ui ->
                            ui.navigate(DeviceDetailView.class));
                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildBatteryCard(Integer value) {
        String color = "#f5c542";
        if (value >= 75)
            color = "#10c437";
        else if (value <= 25)
            color = "#c7140e";

        Icon icon = VaadinIcon.ALIGN_JUSTIFY.create();
        icon.setColor(color);
        icon.setSize("50px");
        IconItem item = new IconItem(icon, value.toString(), "Battery Status");
        item.setAlignItems(Alignment.CENTER);
        item.setJustifyContentMode(JustifyContentMode.CENTER);
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    remove(chartCaption);
                    remove(chart);
                    chart = buildBatteryChart();
                    chartCaption.setText("Battery status");
                    add(chartCaption);
                    add(chart);

                }, item);
        card.setWidth("100%");
        return card;
    }

    public RippleClickableCard buildDeviceCard(Device device) {
        Icon icon = VaadinIcon.SERVER.create();
        icon.setColor("hsl(214deg 90% 52%)");
        icon.setSize("50px");
        IconItem item = new IconItem(icon, device.getDeviceType().getScreenName(), device.getSerialNumber());
        RippleClickableCard card = new RippleClickableCard(
                componentEvent -> {
                    ComponentUtil.setData(UI.getCurrent(), Device.class, device);
                    switch (device.getDeviceType()) {
                        case CEPHEID_GENEXPERT:
                            this.getUI().ifPresent(ui ->
                                    ui.navigate(GeneXpertDetailsView.class));
                            break;
                        case ABBOTT_M2000:
                            this.getUI().ifPresent(ui ->
                                    ui.navigate(M2000DetailsView.class));
                            break;
                        default:

                            break;
                    }
                }, item);
        card.setWidth("100%");
        return card;
    }


    private Series<Coordinate> getSeries(List<HealthStatisticsChartResults> list, String seriesName) {
        Series<Coordinate> series = new Series<>();
        List<Coordinate> coordinates = new ArrayList<>();
        for (HealthStatisticsChartResults result : list) {
            coordinates.add(new Coordinate(getISOString(result.getTime()), result.getValue()));
        }
        Coordinate[] array = coordinates.toArray(new Coordinate[list.size()]);
        series.setData(array);
        series.setName(seriesName);
        return series;
    }

    private String getISOString(LocalDateTime l) {
        return l.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private ApexCharts buildBatteryChart() {
        List<HealthStatisticsChartResults> list = customRepository.getBatteryResultsByDateLast7days();
        ApexCharts areaChart = ApexChartsBuilder.get().
                withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(getSeries(list, "Value"))
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

    private ApexCharts buildNetworkSignalChart() {
        List<HealthStatisticsChartResults> list = customRepository.getNetworkSignalResultsByDateLast7days();
        ApexCharts areaChart = ApexChartsBuilder.get().
                withNoData(NoDataBuilder.get().withText("There's no data available").withAlign(com.github.appreciated.apexcharts.config.nodata.Align.center).build())
                .withChart(ChartBuilder.get().withZoom(ZoomBuilder.get().withEnabled(true).withType(ZoomType.x).build())
                        .withType(Type.area).withToolbar(ToolbarBuilder.get().withShow(false).build())
                        .build())
                .withDataLabels(DataLabelsBuilder.get()
                        .withEnabled(false)
                        .build())
                .withStroke(StrokeBuilder.get().withCurve(Curve.straight).build())
                .withSeries(getSeries(list, "Value"))
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


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Optional<NodeConfiguration> nodeConfiguration = nodeConfigurationRepository.findTopByOrderByFacilityDesc();
        if(!nodeConfiguration.isPresent())
            beforeEnterEvent.forwardTo(NodeConfigurationDetailView.class);
    }

}

