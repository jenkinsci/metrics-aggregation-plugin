/* global jQuery, view */
(function ($) {
    $(document).ready(function () {

        /* ------------------------------------------------------------------------------
                                            table
         ------------------------------------------------------------------------------ */

        if (view.getSupportedMetrics && $('#metrics-table').length) {
            view.getSupportedMetrics(function (res) {
                $('#metrics-table').renderMetricsTable(res.responseJSON);
                $('#table-filter').filterTable(res.responseJSON || [], $('#metrics-table'));
            });
        }

        /* ------------------------------------------------------------------------------
                                            charts
         ------------------------------------------------------------------------------ */

        function drawCharts() {
            var metric = $('#treechart-picker').val();            
            var metricId = $('#treechart-picker :selected').text();

            if (view.getMetricsTree && $('#treechart').length) {
                view.getMetricsTree(metric, function (res) {
                    $('#treechart').renderTreeChart(res.responseJSON, metricId);
                });
            }

            if (view.getHistogram && $('#histogram').length) {
                view.getHistogram(metric, function (res) {
                    $('#histogram').renderHistogram(res.responseJSON, metricId, '#histogram-checkbox-log');
                });
            }

            var secondMetric = $('#scatterplot-picker').val();
            var secondMetricId = $('#scatterplot-picker :selected').text();
            if (view.getScatterPlot && $('#scatterplot').length) {
                view.getScatterPlot(metric, secondMetric, function (res) {
                    $('#scatterplot').renderScatterPlot(res.responseJSON, metricId, secondMetricId,
                        '#scatterplot-checkbox-log-x', '#scatterplot-checkbox-log-y');
                });
            }
        }

        drawCharts();

        $('#treechart-picker').on('changed.bs.select', function () {
            drawCharts();
        });

        $('#scatterplot-picker').on('changed.bs.select', function () {
            drawCharts();
        });

        $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            const echartId = e.target.dataset.echartId;
            const echartDomElement = document.getElementById(echartId);
            if (echartDomElement) {
                echarts.getInstanceByDom(echartDomElement).resize()
            }
        });
    });
})(jQuery);
