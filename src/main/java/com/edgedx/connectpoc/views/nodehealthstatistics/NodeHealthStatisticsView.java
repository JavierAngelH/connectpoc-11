package com.edgedx.connectpoc.views.nodehealthstatistics;

import com.edgedx.connectpoc.entity.NodeHealthStatistics;
import com.edgedx.connectpoc.repository.NodeHealthStatisticsRepository;
import com.edgedx.connectpoc.views.MainView;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import static com.edgedx.connectpoc.views.ViewsConstants.PAGE_HEALTH_STATISTICS;
import static com.edgedx.connectpoc.views.ViewsConstants.TITLE_HEALTH_STATISTICS;

@Route(value = PAGE_HEALTH_STATISTICS, layout = MainView.class)
@PageTitle(TITLE_HEALTH_STATISTICS)
@Secured({"ROLE_USER", "ROLE_SYSTEM"})
public class NodeHealthStatisticsView extends VerticalLayout {

    private final NodeHealthStatisticsRepository nodeHealthStatisticsRepository;

    Grid<NodeHealthStatistics> grid = new Grid<>(NodeHealthStatistics.class);

    public NodeHealthStatisticsView(NodeHealthStatisticsRepository nodeHealthStatisticsRepository){
        this.nodeHealthStatisticsRepository = nodeHealthStatisticsRepository;
        setSizeFull();

        H3 title = new H3();
        title.setText("Node Health Statistics");
        title.getElement().getStyle().set("color", "hsl(214deg 90% 52%)");
        add(title);
        setHorizontalComponentAlignment(Alignment.CENTER, title);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setColumns("logDate", "latitude", "longitude", "batteryStatus", "networkSignalStrength");
        grid.getColumnByKey("logDate").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("latitude").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("longitude").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("batteryStatus").setTextAlign(ColumnTextAlign.CENTER);
        grid.getColumnByKey("networkSignalStrength").setTextAlign(ColumnTextAlign.CENTER);
        grid.setItems(nodeHealthStatisticsRepository.findAll());
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        add(grid);

    }
}
